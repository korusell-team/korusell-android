package net.alienminds.ethnogram.data.model.chat

import net.alienminds.ethnogram.data.model.common.ID
import kotlin.time.Instant

data class Chat(
    val id: ID,
    val participants: List<ID>,
    val lastMessage: String,
    val lastMessageDate: Instant,
    val unreadMessagesCount: Int,
    val interlocutorId: ID,
    val interlocutorAvatar: String?,
    val interlocutorName: String,
    val createdAt: Instant,
    val isVirtual: Boolean
)