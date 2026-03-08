package net.alienminds.ethnogram.data.firestore.repository

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.firebase.Firebase
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.TransactionOptions
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.data.firestore.executors.base.BaseGetRequestExecutor
import net.alienminds.ethnogram.data.firestore.executors.base.BaseQueryRequestExecutor
import net.alienminds.ethnogram.data.firestore.executors.firestore.FirestoreMutationRequestExecutor
import net.alienminds.ethnogram.data.firestore.executors.firestore.FirestoreObserveRequestExecutor
import net.alienminds.ethnogram.data.firestore.executors.firestore.FirestoreQueryRequestExecutor
import net.alienminds.ethnogram.data.firestore.fields.ChatFields
import net.alienminds.ethnogram.data.firestore.fields.MessageFields
import net.alienminds.ethnogram.data.firestore.utils.FirestoreProvider
import net.alienminds.ethnogram.data.firestore.utils.applyPaging
import net.alienminds.ethnogram.data.firestore.utils.toPagingMeta
import net.alienminds.ethnogram.data.model.chat.Chat
import net.alienminds.ethnogram.data.model.chat.FileMeta
import net.alienminds.ethnogram.data.model.chat.Message
import net.alienminds.ethnogram.data.model.chat.MessageInput
import net.alienminds.ethnogram.data.model.chat.preview
import net.alienminds.ethnogram.data.model.chat.toMessage
import net.alienminds.ethnogram.data.model.common.ID
import net.alienminds.ethnogram.data.model.common.PagingData
import net.alienminds.ethnogram.data.model.common.PagingInput
import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.model.core.FetchState
import net.alienminds.ethnogram.data.model.core.GetRequestExecutor
import net.alienminds.ethnogram.data.model.core.MutationRequestExecutor
import net.alienminds.ethnogram.data.model.core.ObserveRequestExecutor
import net.alienminds.ethnogram.data.model.core.ObserveState
import net.alienminds.ethnogram.data.model.core.QueryRequestExecutor
import net.alienminds.ethnogram.data.model.user.User
import net.alienminds.ethnogram.data.model.user.UserType
import net.alienminds.ethnogram.data.repository.AuthRepository
import net.alienminds.ethnogram.data.repository.MessageRepository
import net.alienminds.ethnogram.data.repository.UserRepository2
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

class FirestoreMessageRepository(
    private val firestoreProvider: FirestoreProvider,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository2
) : MessageRepository {

    private val chatsRef
        get() = firestoreProvider.getFirestore().collection("chats")

    override fun getChatId(userId: ID): GetRequestExecutor<ID> = BaseGetRequestExecutor{ fetchMode ->
        if (fetchMode != FetchMode.CacheFirst){
            Log.w("FirestoreMessageRepository", "getChatId called in non-cache-first mode, this is not supported")
        }
        val meUid = getMyId() ?: return@BaseGetRequestExecutor FetchState.Error(IllegalStateException("User is not authorized"))
        if (userId == meUid){
            return@BaseGetRequestExecutor FetchState.Error(IllegalStateException("You can't chat with yourself"))
        }
        FetchState.Success(buildChatIdFromUsers(meUid, userId))
    }

    private fun buildChatIdFromUsers(
        user1: ID,
        user2: ID
    ) = listOf(user1, user2).sorted().joinToString("_")

    override fun getChats(pagingInput: PagingInput): QueryRequestExecutor<PagingData<Chat>> {
        var meUid = ""
        return FirestoreQueryRequestExecutor(
            resolveCall = {
                meUid = getMyIdOrThrow()
                chatsRef
                    .whereArrayContains(ChatFields.VISIBLE_FOR, meUid)
                    .orderBy(ChatFields.LAST_MESSAGE_DATE, Query.Direction.DESCENDING)
                    .applyPaging(pagingInput)
            },
            mapper = { snapshot ->
                val docs = snapshot.documents
                val chats = docs.mapNotNull {
                    it.toChat(meUid)
                }
                PagingData(
                    items = chats,
                    meta = docs.toPagingMeta(pagingInput.pageLimit)
                )
            },
            observeSource = ListenSource.DEFAULT
        )
    }

    override fun getChat(chatID: ID): GetRequestExecutor<Chat> =
        BaseGetRequestExecutor{ fetchMode ->
            val meUid = getMyId()
                ?: return@BaseGetRequestExecutor FetchState.Error(IllegalStateException("User is not authorized"))

            // Получаем чат из Firestore
            val chat = getChat(chatID, meUid)
                .get(fetchMode)
                .getOrNull()
            if (chat != null){
                return@BaseGetRequestExecutor FetchState.Success(chat)
            }

            // Если чата нет, возвращаем виртуальный
            val otherUserId = chatID.split("_").first { it != meUid }
            val otherUser = userRepository.getUser(otherUserId)
                .get(FetchMode.CacheFirst)
                .getOrNull()
                ?: return@BaseGetRequestExecutor FetchState.Error(IllegalStateException("Chat not found"))

            val chatName = "${otherUser.name} ${otherUser.surname.takeIf { otherUser.type == UserType.PERSONAL }}"

            val virtualChat = Chat(
                id = chatID,
                participants = listOf(meUid, otherUserId),
                lastMessage = "",
                lastMessageDate = Clock.System.now(),
                unreadMessagesCount = 0,
                interlocutorId = otherUserId,
                interlocutorAvatar = otherUser.image.firstOrNull(),
                interlocutorName = chatName,
                createdAt = Clock.System.now(),
                isVirtual = true
            )
            return@BaseGetRequestExecutor FetchState.Success(virtualChat)
        }

    private fun getChat(chatID: ID, meUid: ID) = FirestoreQueryRequestExecutor(
        resolveCall = {
            chatsRef
                .whereEqualTo(FieldPath.documentId(), chatID)
                .limit(1)
        },
        mapper = { snapshot ->
            snapshot.documents.firstOrNull()?.toChat(meUid)
        }
    )

    override fun getChatMessages(paging: PagingInput, chatID: ID): GetRequestExecutor<PagingData<Message>> =
        FirestoreQueryRequestExecutor(
            resolveCall = {
                chatsRef.document(chatID)
                    .collection("messages")
                    .orderBy(MessageFields.SENT_AT, Query.Direction.DESCENDING)
                    .applyPaging(paging)
            },
            mapper = { snapshot ->
                val docs = snapshot.documents

                val messages = docs.mapNotNull { doc ->
                    doc.toMessage()
                }
                PagingData(
                    items = messages,
                    meta = docs.toPagingMeta(paging.pageLimit)
                )
            }
        )

    override fun observeLastMessages(chatID: ID): ObserveRequestExecutor<List<Message>> =
        FirestoreObserveRequestExecutor(
            resolveCall = {
                chatsRef.document(chatID)
                    .collection("messages")
                    .orderBy(MessageFields.SENT_AT, Query.Direction.DESCENDING)
                    .limit(1)
            },
            mapper = { snapshot ->
                snapshot.documents.mapNotNull{
                    it.toMessage()
                }
            },
            observeSource = ListenSource.DEFAULT
        )

    override fun getUnreadChatsCount(): QueryRequestExecutor<Int>{
        var meUid: ID? = null
        return FirestoreQueryRequestExecutor(
            resolveCall = {
                meUid = getMyIdOrThrow()
                chatsRef.whereArrayContains(ChatFields.VISIBLE_FOR, meUid)
            },
            mapper = { snapshot ->
                if (meUid == null) return@FirestoreQueryRequestExecutor 0
                val chats = snapshot.documents.mapNotNull{
                    it.toChat(meUid)
                }
                chats.sumOf { it.unreadMessagesCount }
            },
            observeSource = ListenSource.DEFAULT
        )
    }

    override fun sendMessage(
        chatID: ID,
        message: MessageInput
    ): MutationRequestExecutor<Message> = FirestoreMutationRequestExecutor{
        val me = userRepository.getMe()
            .get(FetchMode.CacheFirst)
            .getOrNull()
            ?: throw IllegalStateException("User is not authorized")

        val meUid = me.uid?: throw IllegalStateException("User is not authorized")

        val otherUserId = chatID.split("_").first { it != meUid }

        var otherUser: User? = null
        val chatIsExist = chatsRef.document(chatID).get().await().exists()
        if (chatIsExist.not()){
            otherUser = userRepository.getUser(otherUserId)
                .get(FetchMode.CacheFirst)
                .getOrNull()
                ?: throw IllegalStateException("User not found")
        }

        val chatRef = chatsRef.document(chatID)
        val messageRef = chatRef.collection("messages").document()
        val mediaUri = when(message){
            is MessageInput.Image -> uploadChatImage(chatID, message.imageUri)
            is MessageInput.File -> uploadChatFile(chatID, message.fileUri)
            else -> null
        }
        firestoreProvider.getFirestore().runTransaction(
            TransactionOptions.Builder()
                .setMaxAttempts(3)
                .build()
        ){ tx ->
            // Обновляем или создаем чат
            val updateMap = mapOf(
                ChatFields.VISIBLE_FOR to FieldValue.arrayUnion(meUid, otherUserId),
                ChatFields.LAST_MESSAGE to message.preview(),
                ChatFields.LAST_MESSAGE_DATE to FieldValue.serverTimestamp(),
                ChatFields.unreadMessagesCount(otherUserId) to FieldValue.increment(1)
            )
            if (chatIsExist){
                Log.d("FirestoreMessageRepository", "Updating chat $chatID")
                tx.update(chatRef, updateMap)
            } else{
                Log.d("FirestoreMessageRepository", "Creating chat $chatID")
                tx.set(chatRef, updateMap + mapOf<String, Any?>(
                    ChatFields.PARTICIPANTS to listOf(meUid, otherUserId),
                    ChatFields.CREATED_AT to FieldValue.serverTimestamp(),
                    ChatFields.interlocutorAvatar(otherUserId) to otherUser?.image?.firstOrNull(),
                    ChatFields.interlocutorAvatar(meUid) to me.image.firstOrNull(),
                    ChatFields.interlocutorName(otherUserId) to "${otherUser?.name.orEmpty()} ${otherUser?.surname.takeIf { otherUser?.type == UserType.PERSONAL }.orEmpty()}".trim(),
                    ChatFields.interlocutorName(meUid) to "${me.name.orEmpty()} ${me.surname.takeIf { me.type == UserType.PERSONAL }.orEmpty()}".trim()
                ))
            }

            // Создаем сообщение
            val type = when(message) {
                is MessageInput.Text -> "text"
                is MessageInput.Image -> "image"
                is MessageInput.File -> "file"
            }
            val payloadMap = when(message) {
                is MessageInput.Text -> mapOf(
                    MessageFields.TEXT to message.text
                )
                is MessageInput.Image -> mapOf(
                    MessageFields.MEDIA_URL to mediaUri,
//                    MessageFields.THUMBNAIL_URL to message.thumbnailUri
                    MessageFields.TEXT to "\uD83D\uDCF7 Фото"
                )
                is MessageInput.File -> mapOf(
                    MessageFields.MEDIA_URL to mediaUri,
                    MessageFields.FILE_NAME to message.fileName,
                    MessageFields.FILE_SIZE to message.fileSize,
                    MessageFields.TEXT to "\uD83D\uDCCE ${message.fileName}"
                )
            }
            tx.set(messageRef, payloadMap + mapOf(
                MessageFields.TYPE to type,
                MessageFields.SENDER_ID to meUid,
                MessageFields.SENT_AT to FieldValue.serverTimestamp(),
            ))
        }.await()
        message.toMessage(
            messageId = messageRef.id,
            senderId = meUid,
            mediaURL = mediaUri.toString()
        )
    }

    private suspend fun uploadChatFile(chatID: ID, uri: Uri): Uri? {
        val storage = Firebase.storage
        val extension = uri.lastPathSegment?.substringAfterLast('.') ?: "jpg"
        val reference = storage.reference.child("chat_files/$chatID/${UUID.randomUUID()}.$extension")
        val uploadTask = reference.putFile(uri)
        return uploadTask.continueWithTask { task ->
            if (task.isSuccessful.not()) {
                task.exception?.let { throw it }
            }
            reference.downloadUrl
        }.await()
    }

    private suspend fun uploadChatImage(chatID: ID, uri: Uri): Uri? {
        val storage = Firebase.storage
        val extension = uri.lastPathSegment?.substringAfterLast('.') ?: "jpg"
        val reference = storage.reference.child("chat_images/$chatID/${UUID.randomUUID()}.$extension")
        val uploadTask = reference.putFile(uri)
        return uploadTask.continueWithTask { task ->
            if (task.isSuccessful.not()) {
                task.exception?.let { throw it }
            }
            reference.downloadUrl
        }.await()
    }

    override fun markChatAsRead(chatID: ID): MutationRequestExecutor<Unit> =
        FirestoreMutationRequestExecutor{
            val meUid = getMyIdOrThrow()
            chatsRef.document(chatID)
                .update(ChatFields.unreadMessagesCount(meUid), 0)
                .await()
        }

    override fun getFileMeta(url: String): GetRequestExecutor<FileMeta> =
        BaseGetRequestExecutor{
            runCatching {
                val storageRef = Firebase.storage.getReferenceFromUrl(url)
                val meta = storageRef.metadata.await()
                val contentType = meta.contentType?: error("File content type is null")
//                val fileSize = meta.sizeBytes
                FileMeta(
                    contentType = contentType,
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType)?: "bin",
                    name = meta.name.orEmpty()
                )
            }.fold(
                onSuccess = { FetchState.Success(it) },
                onFailure = { FetchState.Error(it) }
            )
        }

    private suspend fun getMyId() = authRepository.getIdentity()
        .get().getOrNull()?.id

    private suspend fun getMyIdOrThrow() = getMyId()?: error("User is not authorized")

    private fun DocumentSnapshot.toChat(meID: ID): Chat? {
        val participants = (get(ChatFields.PARTICIPANTS) as? List<*>)
            ?.mapNotNull { it as? ID }
            .orEmpty()

        val interlocutorId = participants.firstOrNull { it != meID }?: run {
            Log.w("FirestoreMessageRepository", "Chat has no other participant")
            return null
        }

        val lastMessageDate = getTimestamp(ChatFields.LAST_MESSAGE_DATE)?.let {
            Instant.fromEpochSeconds(it.seconds, it.nanoseconds)
        }?: run {
            Log.w("FirestoreMessageRepository", "Last message date is null in chat $id")
            return null
        }

        return Chat(
            id = id,
            participants = participants,
            lastMessage = getString(ChatFields.LAST_MESSAGE).orEmpty(),
            lastMessageDate = lastMessageDate,
            unreadMessagesCount = getLong(ChatFields.unreadMessagesCount(meID))?.toInt()?: 0,
            interlocutorId = interlocutorId,
            interlocutorAvatar = getString(ChatFields.interlocutorAvatar(interlocutorId)).orEmpty(),
            interlocutorName = getString(ChatFields.interlocutorName(interlocutorId)).orEmpty(),
            createdAt = getTimestamp(ChatFields.CREATED_AT)?.let {
                Instant.fromEpochSeconds(it.seconds, it.nanoseconds)
            }?: lastMessageDate,
            isVirtual = false
        )
    }

    private fun DocumentSnapshot.toMessage(): Message? {
        val type = getString(MessageFields.TYPE)?: run {
            Log.w("FirestoreMessageRepository", "Message has no type")
            return null
        }

        val senderId = getString(MessageFields.SENDER_ID)?: run {
            Log.w("FirestoreMessageRepository", "Message has no senderId")
            return null
        }

        val sentAt = getTimestamp(MessageFields.SENT_AT)?.let {
            Instant.fromEpochSeconds(it.seconds, it.nanoseconds)
        }?: run {
            Log.w("FirestoreMessageRepository", "Message has no sentAt")
            return null
        }

        return when(type.lowercase()){
            "text" -> Message.TextMessage(
                id = id,
                senderId = senderId,
                sentAt = sentAt,
                text = getString(MessageFields.TEXT).orEmpty()
            )
            "image" -> Message.ImageMessage(
                id = id,
                senderId = senderId,
                sentAt = sentAt,
                imageUrl = getString(MessageFields.MEDIA_URL).orEmpty(),
                thumbnailUrl = getString(MessageFields.THUMBNAIL_URL)
            )
            "file" -> Message.FileMessage(
                id = id,
                senderId = senderId,
                sentAt = sentAt,
                fileUrl = getString(MessageFields.MEDIA_URL).orEmpty(),
                fileName = getString(MessageFields.FILE_NAME),
                fileSize = getLong(MessageFields.FILE_SIZE)
            )
            else -> {
                Log.w("FirestoreMessageRepository", "Unknown message type: $type")
                null
            }
        }
    }


}