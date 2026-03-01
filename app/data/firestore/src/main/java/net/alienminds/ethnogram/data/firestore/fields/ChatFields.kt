package net.alienminds.ethnogram.data.firestore.fields

import net.alienminds.ethnogram.data.model.common.ID

internal object ChatFields {

    const val PARTICIPANTS = "participants"
    const val CREATED_AT = "createdAt"
    const val LAST_MESSAGE = "lastMessage"
    const val LAST_MESSAGE_DATE = "lastMessageTimestamp"
    const val VISIBLE_FOR = "visibleFor"
    fun interlocutorAvatar(userId: ID) = "otherUserAvatar_$userId"
    fun interlocutorName(userId: ID) = "otherUserName_$userId"
    fun unreadMessagesCount(userId: ID) = "unreadCount_$userId"

}