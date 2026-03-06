package net.alienminds.ethnogram.ui.screens.session.messages.chat

import android.app.DownloadManager
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.alienminds.ethnogram.data.model.chat.Chat
import net.alienminds.ethnogram.data.model.chat.Message
import net.alienminds.ethnogram.data.model.chat.MessageInput
import net.alienminds.ethnogram.data.model.chat.toMessage
import net.alienminds.ethnogram.data.model.common.ID
import net.alienminds.ethnogram.data.model.common.PagingInput
import net.alienminds.ethnogram.data.model.common.PagingMeta
import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.repository.MessageRepository
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import androidx.core.graphics.scale
import okhttp3.OkHttpClient
import okhttp3.Request
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.datetime.LocalDateTime
import net.alienminds.ethnogram.data.model.chat.FileMeta

class ChatModel(
    private val chatID: ID,
    private val contentResolver: ContentResolver
): AppScreenModel() {

    private val messagesRepository by inject<MessageRepository>()
    private val httpClient = OkHttpClient()

    private val _goBackEvent = MutableSharedFlow<String>()// Reason
    val goBackEvent = _goBackEvent.asSharedFlow()

    private val _scrollTopEvent = MutableSharedFlow<Unit>()
    val scrollTopEvent = _scrollTopEvent.asSharedFlow()


    var isLoadingMessages by mutableStateOf(false)
        private set

    var chat by mutableStateOf<Chat?>(null)

    var pagingMeta: PagingMeta? by mutableStateOf(null)
        private set

    private val _messages = mutableStateListOf<Message>()
    val messages by derivedStateOf { _messages.toList()
        .sortedByDescending { it.sentAt }
        .distinctBy { it.id } }

    private val _sendingMessageIds = mutableStateListOf<ID>()
    val sendingMessageIds get() = _sendingMessageIds.toList()

    private val _downloadingFileIds = mutableStateListOf<ID>()
    val downloadingFileIds get() = _downloadingFileIds.toList()

    // Map: MessageID -> LocalFileUri
    private val _cachedFiles = mutableStateMapOf<ID, Uri>()
    val cachedFiles: Map<ID, Uri> get() = _cachedFiles

    val messageField = TextFieldState()


    init {
        loadChatData()
        loadNextMessages()
        waitNewMessages()
    }



    fun requireNext(){
        if (isLoadingMessages) return
        if (pagingMeta?.hasNext != true) return
        loadNextMessages()
    }

    fun sendTextMessage(){
        val trimmedMessage = messageField.text.toString().trim()
        if (trimmedMessage.isEmpty()) return
        messageField.clearText()
        screenModelScope.launch {
            val input = MessageInput.Text(trimmedMessage)
            val tmpMsgId = UUID.randomUUID().toString()
            _sendingMessageIds.add(tmpMsgId)
            val tmpMsg = input.toMessage(
                messageId = tmpMsgId,
                senderId = chat?.participants?.first { it != chat?.interlocutorId }.orEmpty()
            )
            _messages.add(tmpMsg)
            _scrollTopEvent.emit(Unit)

            messagesRepository.sendMessage(
                chatID = chatID,
                message = MessageInput.Text(trimmedMessage)
            ).execute()
                .onSuccess {
                    _sendingMessageIds.remove(tmpMsgId)
                    _messages.remove(tmpMsg)
                    _messages.add(it)
                }
                .onError {
                    _sendingMessageIds.remove(tmpMsgId)
                    _messages.remove(tmpMsg)
                    Log.e("ChatModel", "Error sending message", it)
                }
        }
    }

    fun sendPhotoMessage(uri: Uri){
        screenModelScope.launch {
            val processedUri = withContext(Dispatchers.IO) {
                processImage(uri)
            } ?: uri

            val input = MessageInput.Image(imageUri = processedUri)
            val tmpMsgId = UUID.randomUUID().toString()

            val tmpMsg = input.toMessage(
                messageId = tmpMsgId,
                senderId = chat?.participants?.first { it != chat?.interlocutorId }.orEmpty()
            )
            _sendingMessageIds.add(tmpMsgId)
            _messages.add(tmpMsg)
            _scrollTopEvent.emit(Unit)

            messagesRepository.sendMessage(
                chatID = chatID,
                message = input
            ).execute()
                .onSuccess {
                    _sendingMessageIds.remove(tmpMsgId)
                    _messages.remove(tmpMsg)
                    _messages.add(it)
                }
                .onError {
                    _sendingMessageIds.remove(tmpMsgId)
                    _messages.remove(tmpMsg)
                    Log.e("ChatModel", "Error sending message", it)
                }
        }
    }

    private suspend fun processImage(uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

            val width = options.outWidth
            val height = options.outHeight

            if (width <= 2048 && height <= 2048) return@withContext null

            val scale = if (width > height) 2048f / width else 2048f / height
            val targetWidth = (width * scale).toInt()
            val targetHeight = (height * scale).toInt()

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(width, height, 2048, 2048)
            }

            val bitmap = contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, decodeOptions) 
            } ?: return@withContext null

            val resizedBitmap = bitmap.scale(targetWidth, targetHeight)
            
            // Save to temp file
            val tempFile = File.createTempFile("resized_image_", ".jpg")
            FileOutputStream(tempFile).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            Log.e("ChatModel", "Error processing image", e)
            null
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun sendFileMessage(uri: Uri){
        screenModelScope.launch {
            val (name, size) = contentResolver.getFileInfo(uri)
            if (size == null) return@launch
            if (size > 10 * 1024 * 1024) return@launch //10mb

            val input = MessageInput.File(
                fileUri = uri,
                fileName = name,
                fileSize = size
            )
            val tmpMsgId = UUID.randomUUID().toString()
            val tmpMsg = input.toMessage(
                messageId = tmpMsgId,
                senderId = chat?.participants?.first { it != chat?.interlocutorId }.orEmpty()
            )
            _sendingMessageIds.add(tmpMsgId)
            _messages.add(tmpMsg)
            _scrollTopEvent.emit(Unit)

            messagesRepository.sendMessage(
                chatID = chatID,
                message = input
            ).execute()
                .onSuccess {
                    _sendingMessageIds.remove(tmpMsgId)
                    _messages.remove(tmpMsg)
                    _messages.add(it)
                }
                .onError {
                    _sendingMessageIds.remove(tmpMsgId)
                    _messages.remove(tmpMsg)
                    Log.e("ChatModel", "Error sending message", it)
                }
        }
    }

    private fun ContentResolver.getFileInfo(uri: Uri): Pair<String?, Long?> {
        val cursor = query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
            ),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)

                val name = if (nameIndex != -1) it.getString(nameIndex) else null
                val size = if (sizeIndex != -1) it.getLong(sizeIndex) else null

                return name to size
            }
        }

        return null to null
    }

    private fun loadNextMessages(){
        if (isLoadingMessages) return
        screenModelScope.launch {
            isLoadingMessages = true
            val paging = PagingInput(
                pageLimit = 15,
                cursor = pagingMeta?.cursor
            )
            var cachePagingMeta: PagingMeta? = null
            var cacheAdded: List<Message> = emptyList()
            messagesRepository
                .getChatMessages(paging, chatID)
                .get(FetchMode.CacheOnly)
                .onSuccess {
                    _messages.addAll(it.items)
                    cachePagingMeta = it.meta
                    cacheAdded = it.items
                }
            messagesRepository
                .getChatMessages(paging, chatID)
                .get(FetchMode.NetworkOnly)
                .onSuccess {
                    if (pagingMeta == null){
                        markChatAsRead()
                    }
                    pagingMeta = it.meta
                    if (it.items != cacheAdded) {
                        _messages.removeAll(cacheAdded)
                        _messages.addAll(it.items)
                    }
                }
                .onError {
                    Log.e("ChatModel", "Error loading messages", it)
                    pagingMeta = cachePagingMeta?: pagingMeta?.copy(
                        hasNext = false
                    )
                }
        }.invokeOnCompletion {
            isLoadingMessages = false
        }
    }

    private fun waitNewMessages(){
        screenModelScope.launch {
            messagesRepository.observeLastMessages(chatID)
                .observe(FetchMode.NetworkFirst)
                .data()
                .collect {
                    _messages.addAll(it)
                    markChatAsRead()
                    _scrollTopEvent.emit(Unit)
                }
        }
    }

    private fun markChatAsRead(){
        screenModelScope.launch {
            messagesRepository.markChatAsRead(chatID)
                .execute()
                .onError {
                    Log.e("ChatModel", "Error marking chat as read", it)
                }
        }
    }

    private fun loadChatData(){
        screenModelScope.launch {
            messagesRepository
                .getChat(chatID)
                .get(FetchMode.CacheFirst)
                .onSuccess {
                    chat = it
                }
                .onError {
                    Log.e("ChatModel", "Error loading chat data", it)
                    _goBackEvent.emit(it.message?: "Chat not found")
                }
        }
    }


    fun isFileCached(messageId: ID, fileName: String?): Boolean {
        if (_cachedFiles.containsKey(messageId)) return true
        val file = getLocalFile(messageId, fileName)
        if (file.exists()) {
            _cachedFiles[messageId] = Uri.fromFile(file)
            return true
        }
        return false
    }

    private fun getLocalFile(messageId: ID, fileName: String?): File {
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "chat_files")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val extension = fileName?.substringAfterLast('.', "")?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
        return File(cacheDir, "${messageId}$extension")
    }

    fun downloadFile(message: Message.FileMessage) {
        if (_downloadingFileIds.contains(message.id)) return

        screenModelScope.launch {
            _downloadingFileIds.add(message.id)
            try {
                val file = withContext(Dispatchers.IO) {
                    val request = Request.Builder().url(message.fileUrl).build()
                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@withContext null
                        val targetFile = getLocalFile(message.id, message.fileName)
                        response.body?.byteStream()?.use { input ->
                            targetFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        targetFile
                    }
                }
                if (file != null) {
                    _cachedFiles[message.id] = Uri.fromFile(file)
                }
            } catch (e: Exception) {
                Log.e("ChatModel", "Error downloading file", e)
            } finally {
                _downloadingFileIds.remove(message.id)
            }
        }
    }

    fun downloadFileByUri(context: Context, uri: Uri, fileName: String? = null){
        screenModelScope.launch {
            runCatching {
                println("Download Uri: $uri")
                val meta = when(fileName == null){
                    true -> messagesRepository.getFileMeta(uri.toString()).get().getOrNull()
                    false -> {
                        val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                        FileMeta(
                            contentType = type.orEmpty(),
                            extension = ext,
                            name = fileName
                        )
                    }
                }
                val rawFileName = meta?.name?: LocalDateTime.toString()
                val finalFileName = when(rawFileName.endsWith(meta?.extension.orEmpty(), true)){
                    true -> rawFileName
                    false -> "${rawFileName}.${meta?.extension}"
                }
                println("Download File: $finalFileName, meta: $meta")
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val request = DownloadManager.Request(uri)
                    .setTitle(finalFileName)
                    .setDescription("Скачивание...")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, finalFileName)
                val downloadId = downloadManager.enqueue(request)
            }.onFailure {
                Log.e("ChatModel", "Error downloading file", it)
            }
        }
    }

    fun deleteFromCache(messageId: ID, fileName: String?) {
        val file = getLocalFile(messageId, fileName)
        if (file.exists()) file.delete()
        _cachedFiles.remove(messageId)
    }

    fun openFile(context: Context, messageId: ID, fileName: String?) {
        val file = getLocalFile(messageId, fileName)
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Открыть файл"))
    }

    fun shareFile(context: Context, messageId: ID, fileName: String?) {
        val file = getLocalFile(messageId, fileName)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = context.contentResolver.getType(uri) ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Сохранить как"))
    }

    fun saveFile(context: Context, messageId: ID, fileName: String?, saveUri: Uri){
        screenModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val sourceFile = getLocalFile(messageId, fileName)
                    if (!sourceFile.exists()) return@withContext

                    context.contentResolver.openOutputStream(saveUri)?.use { output ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }

                } catch (e: Exception) {
                    Log.e("ChatModel", "Error saving file", e)
                }
            }
        }
    }


}