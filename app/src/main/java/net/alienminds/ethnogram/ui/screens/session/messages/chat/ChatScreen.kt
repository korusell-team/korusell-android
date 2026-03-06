package net.alienminds.ethnogram.ui.screens.session.messages.chat

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.data.model.chat.Chat
import net.alienminds.ethnogram.data.model.chat.Message
import net.alienminds.ethnogram.data.model.common.ID
import net.alienminds.ethnogram.service.utils.ActiveChatTracker
import net.alienminds.ethnogram.ui.extentions.buttons.BackButton
import net.alienminds.ethnogram.ui.extentions.custom.Avatar
import net.alienminds.ethnogram.ui.extentions.fields.CompactTextField
import net.alienminds.ethnogram.ui.screens.session.contacts.profile.ProfileScreen
import net.alienminds.ethnogram.ui.screens.session.messages.chat.components.MessageLine

class ChatScreen(
    private val chatID: ID
): Screen {


    @Composable
    override fun Content() = Column(
        modifier = Modifier.fillMaxSize()
    ){
        val context = LocalContext.current
        val navigator = LocalNavigator.current
        val vm = rememberScreenModel { ChatModel(chatID, context.contentResolver) }
        val lazyState = rememberLazyListState()
        LaunchedEffect(Unit) {
            launch {
                vm.goBackEvent.collect {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    navigator?.pop()
                }
            }
            launch {
                vm.scrollTopEvent.collect {
                    lazyState.animateScrollToItem(0)
                }
            }
        }
        DisposableEffect(Unit) {
            ActiveChatTracker.currentChatId = chatID
            onDispose {
                if(ActiveChatTracker.currentChatId == chatID) {
                    ActiveChatTracker.currentChatId = null
                }
            }
        }
        ChatToolbar(
            modifier = Modifier.statusBarsPadding(),
            chat = vm.chat
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = 12.dp,
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true,
            state = lazyState
        ) {
            vm.chat?.let { chat ->
                messages(vm, chat)
            }
            if (vm.pagingMeta?.hasNext == true) {
                item {
                    LaunchedEffect(Unit) {
                        vm.requireNext()
                    }
                    Box(
                        modifier = Modifier.fillParentMaxWidth(),
                        contentAlignment = Alignment.Center,
                        content = { CircularProgressIndicator() }
                    )
                }
            }
        }
        MessageField(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 16.dp)
                .padding(horizontal = 20.dp),
            state = vm.messageField,
            onSendText = vm::sendTextMessage,
            onSendPhoto = vm::sendPhotoMessage,
            onSendFile = vm::sendFileMessage
        )

    }

    private fun LazyListScope.messages(
        vm: ChatModel,
        chat: Chat
    ) = items(
        items = vm.messages,
        key = { it.id }
    ){ message ->
        val context = LocalContext.current
        val isMe = message.senderId != chat.interlocutorId
        val isSending = vm.sendingMessageIds.contains(message.id)
        val isCachedFile = message is Message.FileMessage && vm.isFileCached(message.id, message.fileName)
        val isDownloadingFile = message is Message.FileMessage && vm.downloadingFileIds.contains(message.id)
        val fileName = (message as? Message.FileMessage)?.fileName
        val fileSavePicker = rememberLauncherForActivityResult(
            contract = CreateDocument("*/*")
        ) { uri ->
            uri?.let {
                vm.saveFile(
                    context = context,
                    messageId = message.id,
                    fileName = fileName,
                    saveUri = it
                )
            }
        }
        MessageLine(
            modifier = Modifier.fillParentMaxWidth(),
            message = message,
            isMe = isMe,
            isSending = isSending,
            isDownloadingFile = isDownloadingFile,
            isCachedFile = isCachedFile,
            onDownloadFile = { (message as? Message.FileMessage)?.let(vm::downloadFile) },
            onDownloadImage = { vm.downloadFileByUri(context, it) },
            onOpenFile = { vm.openFile(
                context = context,
                messageId = message.id,
                fileName = fileName
            ) },
            onShareFile = { vm.shareFile(
                context = context,
                messageId = message.id,
                fileName = fileName
            ) },
            onSaveFile = { fileSavePicker.launch(fileName?: "file") },
            onDeleteFileCache = { vm.deleteFromCache(
                messageId = message.id,
                fileName = fileName
            ) }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MessageField(
        modifier: Modifier,
        state: TextFieldState,
        onSendText: () -> Unit,
        onSendPhoto: (uri: Uri) -> Unit,
        onSendFile: (uri: Uri) -> Unit
    ) = Column(
        modifier = modifier,
    ){
        var showPickFile by remember { mutableStateOf(false) }
        val imagePicker =  rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { it?.let { onSendPhoto(it) } }
        )
        val filePicker =  rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { it?.let { onSendFile(it) } }
        )
        if (showPickFile) {
            ModalBottomSheet(
                onDismissRequest = { showPickFile = false },
            ){
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        showPickFile = false
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text("Изображение")
                }
                HorizontalDivider()
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        filePicker.launch(arrayOf("*/*"))
                        showPickFile = false
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text("Файл")
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ){

            IconButton(
                modifier = Modifier.size(32.dp),
                onClick = { showPickFile = showPickFile.not() },
                shape = CircleShape,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    disabledContentColor = MaterialTheme.colorScheme.outline
                )
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_attach_file),
                    contentDescription = null
                )
            }

            CompactTextField(
                modifier = Modifier
                    .heightIn(32.dp)
                    .weight(1f),
                state = state,
                textStyle = MaterialTheme.typography.labelLarge,
                shape = MaterialTheme.shapes.small,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                suffix = {
                    Spacer(Modifier.weight(1f))
                    AnimatedVisibility(state.text.toString().isNotEmpty()) {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable{ onSendText() },
                            painter = painterResource(R.drawable.ic_send),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null
                        )
                    }
                    Spacer(Modifier.weight(1f))
                },
                lineLimits = TextFieldLineLimits.MultiLine(
                    minHeightInLines = 1,
                    maxHeightInLines = 6
                ),
                placeholder = {
                    Text(
                        text = "Сообщение",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            )

        }
    }

    @Composable
    private fun ChatToolbar(
        modifier: Modifier = Modifier,
        chat: Chat?
    ) = Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(48.dp)
            .padding(end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navigator = LocalNavigator.current
        BackButton()
        Text(
            modifier = Modifier.weight(1f),
            text = chat?.interlocutorName.orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Avatar(
            modifier = Modifier.size(44.dp),
            model = chat?.interlocutorAvatar,
            initials = chat?.interlocutorName.orEmpty().split(' ')
                .joinToString(""){ it.firstOrNull()?.toString().orEmpty() },
            contentScale = ContentScale.Crop,
            textStyle = MaterialTheme.typography.titleLarge,
            border = BorderStroke(
                width = 4.dp,
                color = MaterialTheme.colorScheme.background
            ),
            onClick = {
                chat?.interlocutorId?.let { userId ->
                    val dest = ProfileScreen(userId)
                    navigator?.push(dest)
                }
            }
        )
    }

}