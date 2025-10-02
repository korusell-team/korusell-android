package net.alienminds.ethnogram.ui.screens.session.feed.list

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.onEach
import net.alienminds.ethnogram.service.feed.FeedRepository
import net.alienminds.ethnogram.service.feed.entities.Author
import net.alienminds.ethnogram.service.feed.entities.Feed
import net.alienminds.ethnogram.service.feed.entities.FeedType
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

internal class FeedListModel: AppScreenModel() {

    private val userRepo by inject<UserRepository>()
    private val feedRepo by inject<FeedRepository>()

    private val _feeds by feedRepo.getFeedsFlow()
        .onEach { it?.fetchAuthors() }
        .asStateWithLoading(emptyList())

    val myId by userRepo.myIdFlow.asState(null)

    var type by mutableStateOf<FeedType?>(null)

    var authors by mutableStateOf<Map<String, Author>>(emptyMap())
        private set

    val feeds by derivedStateOf { when(type == null) {
        true -> _feeds.orEmpty()
        false -> _feeds?.filter { it.type == type }.orEmpty()
    } }

    fun changeFavorite(
        feedId: String,
        isFavorite: Boolean
    ) = launchWithLoading{
        feedRepo.favoriteFeed(feedId, isFavorite)
    }

    private suspend fun List<Feed>.fetchAuthors() = mapNotNull { it.authorId }
        .toTypedArray()
        .also {
            authors = userRepo.getAuthors(authorIds = it).getOrNull().orEmpty()
        }
}