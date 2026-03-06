package net.alienminds.ethnogram.ui.screens.session.messages.chat.components

import android.net.Uri
import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.valentinilk.shimmer.shimmer
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.data.model.chat.Message
import net.alienminds.ethnogram.ui.screens.session.messages.chat.ChatScreen
import kotlin.time.Instant

@Composable
fun ChatScreen.MessageLine(
    modifier: Modifier = Modifier,
    message: Message,
    isMe: Boolean,
    isSending: Boolean,
    isDownloadingFile: Boolean,
    isCachedFile: Boolean,
    onDownloadFile: () -> Unit,
    onDownloadImage: (Uri) -> Unit,
    onOpenFile: () -> Unit,
    onShareFile: () -> Unit,
    onSaveFile: () -> Unit,
    onDeleteFileCache: () -> Unit
){
    val hAlign = if (isMe) Alignment.End else Alignment.Start
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterVertically.plus(hAlign)
    ){
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.aligned(hAlign)
        ) {
            if (isMe && message is Message.TextMessage) {
                TimeOrStatus(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    sentAt = message.sentAt,
                    isMe = true,
                    isText = true,
                    isSending = isSending,
                )
            }

            val bubbleShape = RoundedCornerShape(16.dp).copy(
                bottomEnd = if (isMe) CornerSize(2.dp) else CornerSize(16.dp),
                bottomStart = if (isMe) CornerSize(16.dp) else CornerSize(2.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f, false)
                    .clip(bubbleShape)
                    .background(
                        if (isMe) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerLow
                    )
            ) {
                when (message) {
                    is Message.TextMessage -> TextMessageContent(
                        text = message.text,
                        isMe = isMe
                    )
                    is Message.ImageMessage -> ImageMessageContent(
                        message = message,
                        isMe = isMe,
                        isSending = isSending,
                        onDownload = onDownloadImage
                    )
                    is Message.FileMessage -> FileMessageContent(
                        message = message,
                        isMe = isMe,
                        isSending = isSending,
                        isCached = isCachedFile,
                        isDownloading = isDownloadingFile,
                        onDownloadFile = onDownloadFile,
                        onOpenFile = onOpenFile,
                        onShareFile = onShareFile,
                        onSaveFile = onSaveFile,
                        onDeleteFileCache = onDeleteFileCache
                    )
                }
            }

            if (!isMe && message is Message.TextMessage) {
                TimeOrStatus(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    sentAt = message.sentAt,
                    isMe = false,
                    isText = true,
                    isSending = isSending,
                )
            }
        }
    }
}

@Composable
private fun TextMessageContent(
    text: String,
    isMe: Boolean
) = SelectionContainer(
    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
){
    Text(
        text = buildTextMessageAnnotated(
            text = text,
            linkColor = when(isMe) {
                true -> MaterialTheme.colorScheme.onPrimary
                false -> MaterialTheme.colorScheme.primary
            }
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
    )
}

private val urlRegex = "(https?://[\\w\\-._~:/?#\\\\@!$&'()*+,;=%]+)".toRegex()


private fun buildTextMessageAnnotated(
    text: String,
    linkColor: Color
): AnnotatedString =
    buildAnnotatedString {
        var lastIndex = 0
        urlRegex.findAll(text).forEach { match ->
            val start =  match.range.first
            val end = match.range.last + 1

            append(text.substring(lastIndex, start))

            val url = match.value
            withLink(
                LinkAnnotation.Url(
                    url = url,
                    styles = TextLinkStyles(SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )),
                ),
                block = { append(url) }
            )
            lastIndex = end
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }


//fun buildAnnotatedStringWithLinks(text: String): AnnotatedString {
//    val builder = AnnotatedString.Builder()
//
//    var lastIndex = 0
//
//    urlRegex.findAll(text).forEach { match ->
//        val start = match.range.first
//        val end = match.range.last + 1
//
//        builder.append(text.substring(lastIndex, start))
//
//        val url = match.value
//
//        builder.pushStringAnnotation(
//            tag = "URL",
//            annotation = url
//        )
//
//
//        builder.withStyle(
//            SpanStyle(
//                color = Color(0xFF64B5F6),
//                textDecoration = TextDecoration.Underline
//            )
//        ) {
//            append(url)
//        }
//
//        builder.pop()
//
//        lastIndex = end
//    }
//
//    if (lastIndex < text.length) {
//        builder.append(text.substring(lastIndex))
//    }
//
//    return builder.toAnnotatedString()
//}

@Composable
private fun ImageMessageContent(
    message: Message.ImageMessage,
    isMe: Boolean,
    isSending: Boolean,
    onDownload: (Uri) -> Unit
) = Box(contentAlignment = Alignment.BottomEnd) {
    val painter = rememberAsyncImagePainter(message.imageUrl)
    val painterState = painter.state.collectAsState()
    val isLoadingImage = painterState.value is AsyncImagePainter.State.Loading
    if (isLoadingImage){
        Spacer(Modifier
            .heightIn(max = 200.dp)
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.outline)
            .shimmer())
    } else{
        var showViewDialog by remember { mutableStateOf(false) }
        Image(
            modifier = Modifier
                .heightIn(max = 400.dp)
                .clickable { showViewDialog = showViewDialog.not() },
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
        if(showViewDialog){
            ViewImageDialog(
                image = painter,
                onDownload = { message.imageUrl.toUri().let(onDownload) },
                onDismiss = { showViewDialog = false }
            )
        }
    }
    if (isSending || isLoadingImage){
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
    Box(
        modifier = Modifier
            .padding(8.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        TimeOrStatus(
            sentAt = message.sentAt,
            isMe = isMe,
            isText = false,
            isSending = isSending
        )
    }
}

@Composable
private fun ViewImageDialog(
    image: Painter,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) = Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false
    )
){
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, false),
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        IconButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .navigationBarsPadding()
                .padding(vertical = 16.dp),
            onClick = onDownload,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun FileMessageContent(
    message: Message.FileMessage,
    isMe: Boolean,
    isSending: Boolean,
    isCached: Boolean,
    isDownloading: Boolean,
    onDownloadFile: () -> Unit,
    onOpenFile: () -> Unit,
    onShareFile: () -> Unit,
    onSaveFile: () -> Unit,
    onDeleteFileCache: () -> Unit,
){
    val context = LocalContext.current
//    val isCached = vm.isFileCached(message.id, message.fileName)
//    val isDownloading = vm.downloadingFileIds.contains(message.id)

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    when (isMe) {
                        true -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        false -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    }
                )
                .clickable {
                    when {
                        isCached.not() && isDownloading.not() -> onDownloadFile()
                        isCached -> onOpenFile()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val tint = when(isMe) {
                true -> MaterialTheme.colorScheme.onPrimary
                false -> MaterialTheme.colorScheme.primary
            }
            if (isSending || isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = tint,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(when(isCached) {
                        true -> R.drawable.ic_attach_file
                        false -> R.drawable.ic_download
                    }),
                    contentDescription = null,
                    tint = tint
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f, fill = false)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ){
                val contentColor = when(isMe){
                    true -> MaterialTheme.colorScheme.onPrimary
                    false -> MaterialTheme.colorScheme.onSurface
                }
                Text(
                    modifier = Modifier.weight(1f, false),
                    text = message.fileName ?: "Файл",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis
                )
                if (isCached) {
                    FileOptionMenu(
                        tint = contentColor,
                        onOpenFile = onOpenFile,
                        onShareFile = onShareFile,
                        onSaveFile = onSaveFile,
                        onDeleteFromCache = onDeleteFileCache
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f, false),
                    text = message.fileSize?.let { Formatter.formatShortFileSize(context, it) } ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = when(isMe) {
                        true -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        false -> MaterialTheme.colorScheme.outline
                    }
                )
                TimeOrStatus(
                    sentAt = message.sentAt,
                    isMe = isMe,
                    isText = false,
                    isSending = isSending
                )
            }
        }


    }
}

@Composable
private fun FileOptionMenu(
    tint: Color,
    onOpenFile: () -> Unit,
    onShareFile: () -> Unit,
    onSaveFile: () -> Unit,
    onDeleteFromCache: () -> Unit
){
    var showMenu by remember { mutableStateOf(false) }
    Box {
        IconButton(
            modifier = Modifier.size(16.dp),
            onClick = { showMenu = true }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = null,
                tint = tint
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Открыть") },
                onClick = {
                    onOpenFile()
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Поделиться") },
                onClick = {
                    onShareFile()
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Сохранить как") },
                onClick = {
                    onSaveFile()
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Удалить из кэша") },
                onClick = {
                    onDeleteFromCache()
                    showMenu = false
                }
            )
        }
    }
}

@Composable
private fun TimeOrStatus(
    modifier: Modifier = Modifier,
    sentAt: Instant,
    isMe: Boolean,
    isText: Boolean,
    isSending: Boolean,
) {
    val color: Color = when {
        isText -> MaterialTheme.colorScheme.outline
        isMe -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.outline
    }
    if (isSending){
        Icon(
            modifier = modifier.size(12.dp),
            painter = painterResource(R.drawable.ic_clock_outline),
            contentDescription = null,
            tint = color
        )
    } else {
        val formatedTime = sentAt.toLocalDateTime(TimeZone.currentSystemDefault())
            .format(LocalDateTime.Format {
                hour()
                char(':')
                minute()
            })
        Text(
            modifier = modifier,
            text = formatedTime,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = color
        )
    }
}