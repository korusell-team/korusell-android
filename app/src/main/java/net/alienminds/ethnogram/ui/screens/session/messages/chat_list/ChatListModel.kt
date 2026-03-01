package net.alienminds.ethnogram.ui.screens.session.messages.chat_list

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.data.model.chat.Chat
import net.alienminds.ethnogram.data.model.common.PagingInput
import net.alienminds.ethnogram.data.model.common.PagingMeta
import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.repository.MessageRepository
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

class ChatListModel: AppScreenModel() {

    private val messagesRepo by inject<MessageRepository>()

    var isLoadingChats by mutableStateOf(false)
    var chatsMeta by mutableStateOf<PagingMeta?>(null)
    private val _chats = mutableStateListOf<Chat>()
    val chats by derivedStateOf{
        _chats.toList().distinctBy { it.id }
            .sortedByDescending { it.lastMessageDate }
    }

    init {
        subscribeFirstChats()
    }

    fun loadNextChats(){
        if (isLoadingChats) return
        if (chatsMeta?.hasNext != true) return
        screenModelScope.launch {
            isLoadingChats = true
            val request =  messagesRepo.getChats(
                pagingInput = PagingInput(
                    pageLimit = 15,
                    cursor = chatsMeta?.cursor
                )
            )

            var cacheItems: List<Chat> = emptyList()
            var cacheMeta: PagingMeta? = null
            request.get(FetchMode.CacheOnly).onSuccess {
                _chats.addAll(it.items)
                cacheItems = it.items
                cacheMeta = it.meta
            }
            request.get(FetchMode.NetworkOnly).onSuccess {
                _chats.removeAll(cacheItems)
                _chats.addAll(it.items)
                chatsMeta = it.meta
            }.onError {
                chatsMeta = cacheMeta
                Log.e("ChatListModel", "Error: ${it.message}")
            }
        }.invokeOnCompletion {
            isLoadingChats = false
        }
    }

    private fun subscribeFirstChats(){
        if (isLoadingChats) return
        screenModelScope.launch {
            messagesRepo.getChats(
                pagingInput = PagingInput(
                    pageLimit = 15
                )
            ).observe(FetchMode.CacheAndNetwork).collect{ os ->
                isLoadingChats = os.isLoading
                os.dataOrNull()?.fold(
                    onSuccess = { result ->
                        println("Chats: ${result.items}")
                        val newChats = result.items
                        if (_chats.isEmpty()) {
                            _chats.addAll(newChats)
                            chatsMeta = result.meta
                        } else{
                            _chats.removeAll { oldChat ->
                                newChats.any { it.id == oldChat.id }
                            }
                            _chats.addAll(newChats)
                        }
                    },
                    onError = {
                        Log.e("ChatListModel", "Error: ${it.message}")
                    }
                )
            }
        }.invokeOnCompletion {
            Log.w("ChatListModel", "subscribe first chats ended..")
            isLoadingChats = false
        }
    }

}