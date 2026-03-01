package net.alienminds.ethnogram.data.model.chat

import net.alienminds.ethnogram.data.model.common.ID
import kotlin.time.Instant


sealed interface Message{

    val id: ID
    val senderId: ID
    val sentAt: Instant

    data class TextMessage(
        override val id: ID,
        override val senderId: ID,
        override val sentAt: Instant,
        val text: String,
    ): Message

    data class ImageMessage(
        override val id: ID,
        override val senderId: ID,
        override val sentAt: Instant,
        val imageUrl: String,
        val thumbnailUrl: String? = null
    ): Message

    data class FileMessage(
        override val id: ID,
        override val senderId: ID,
        override val sentAt: Instant,
        val fileUrl: String,
        val fileName: String?,
        val fileSize: Long?
    ): Message

}
