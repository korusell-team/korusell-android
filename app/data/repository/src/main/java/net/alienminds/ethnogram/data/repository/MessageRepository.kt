package net.alienminds.ethnogram.data.repository

import net.alienminds.ethnogram.data.model.chat.Chat
import net.alienminds.ethnogram.data.model.chat.Message
import net.alienminds.ethnogram.data.model.chat.MessageInput
import net.alienminds.ethnogram.data.model.common.ID
import net.alienminds.ethnogram.data.model.common.PagingData
import net.alienminds.ethnogram.data.model.common.PagingInput
import net.alienminds.ethnogram.data.model.core.GetRequestExecutor
import net.alienminds.ethnogram.data.model.core.MutationRequestExecutor
import net.alienminds.ethnogram.data.model.core.ObserveRequestExecutor
import net.alienminds.ethnogram.data.model.core.QueryRequestExecutor

interface MessageRepository {

    fun getChatId(userId: ID): GetRequestExecutor<ID>

    fun getChats(pagingInput: PagingInput): QueryRequestExecutor<PagingData<Chat>>

    fun getChat(chatID: ID): GetRequestExecutor<Chat>

    fun getChatMessages(paging: PagingInput, chatID: ID): GetRequestExecutor<PagingData<Message>>

    fun observeLastMessages(chatID: ID): ObserveRequestExecutor<List<Message>>

    fun getUnreadChatsCount(): ObserveRequestExecutor<Int>

    fun sendMessage(chatID: ID, message: MessageInput): MutationRequestExecutor<Message>

    fun markChatAsRead(chatID: ID): MutationRequestExecutor<Unit>

}