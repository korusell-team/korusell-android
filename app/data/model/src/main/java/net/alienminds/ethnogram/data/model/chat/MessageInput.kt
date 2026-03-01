package net.alienminds.ethnogram.data.model.chat

import android.net.Uri
import net.alienminds.ethnogram.data.model.common.ID
import kotlin.time.Clock
import kotlin.time.Instant

sealed interface MessageInput{

    data class Text(
        val text: String
    ): MessageInput

    data class Image(
        val imageUri: Uri,
        val thumbnailUri: Uri? = null
    ): MessageInput

    data class File(
        val fileUri: Uri,
        val fileName: String? = null,
        val fileSize: Long? = null
    ): MessageInput
}

fun MessageInput.preview() = when(this){
    is MessageInput.Text -> text
    is MessageInput.Image -> "📷 Фото"
    is MessageInput.File -> "📎 ${fileName?: "Файл"}"
}
fun MessageInput.toMessage(
    messageId: ID,
    senderId: ID,
    sentAt: Instant = Clock.System.now(),
    mediaURL: String? = null,
    thumbnailUrl: String? = null,
) = when(this){
    is MessageInput.Text -> Message.TextMessage(
        id = messageId,
        senderId = senderId,
        sentAt = sentAt,
        text = text
    )
    is MessageInput.Image -> Message.ImageMessage(
        id = messageId,
        senderId = senderId,
        sentAt = sentAt,
        imageUrl = mediaURL?: imageUri.toString(),
        thumbnailUrl = thumbnailUrl?: thumbnailUri?.toString()
    )
    is MessageInput.File -> Message.FileMessage(
        id = messageId,
        senderId = senderId,
        sentAt = sentAt,
        fileUrl = mediaURL?: fileUri.toString(),
        fileName = fileName,
        fileSize = fileSize
    )
}