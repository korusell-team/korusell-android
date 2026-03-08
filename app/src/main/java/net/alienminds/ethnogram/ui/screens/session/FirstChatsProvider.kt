package net.alienminds.ethnogram.ui.screens.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import net.alienminds.ethnogram.data.model.common.PagingInput
import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.model.core.ObserveState
import net.alienminds.ethnogram.data.repository.MessageRepository
import kotlin.time.Duration.Companion.seconds

internal class FirstChatsProvider(
    messageRepository: MessageRepository
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    val firstChatsState = messageRepository
        .getChats(pagingInput = PagingInput(pageLimit = 15))
        .observe(FetchMode.CacheAndNetwork)
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(15.seconds),
            initialValue = ObserveState.loading()
        )

}