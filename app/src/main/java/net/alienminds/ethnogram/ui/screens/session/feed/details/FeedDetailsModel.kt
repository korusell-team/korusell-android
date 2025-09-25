package net.alienminds.ethnogram.ui.screens.session.feed.details

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.service.feed.FeedRepository
import net.alienminds.ethnogram.service.feed.entities.Feed
import net.alienminds.ethnogram.service.feed.entities.FeedAuthor
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

internal class FeedDetailsModel(
    private val feedId: String
): AppScreenModel() {

    private val feedRepo by inject<FeedRepository>()
    private val userRepo by inject<UserRepository>()

    private var myId by mutableStateOf<String?>(null)

    var feed by mutableStateOf<Feed?>(null)
        private set

    var author by mutableStateOf<FeedAuthor?>(null)
        private set

    val isFavorite by derivedStateOf { feed?.likeList?.any { it == myId } == true }

    init {
        screenModelScope.launch {
            launchWithLoading {
                loadFeed()
            }.join()
            observeFeed()
        }
    }

    fun changeFavoriteFeed(
        isFavorite: Boolean
    ) = launchWithLoading{
        feed = feed?.copy(
            likeList = feed?.likeList.orEmpty().let {
                when(isFavorite){
                    true -> myId?.let { uid -> it.plus(uid) }
                    false -> it.filter { it != myId }
                }
            }
        )
        feedRepo.favoriteFeed(
            feedId, isFavorite
        )
    }

    private suspend fun observeFeed(){
        feedRepo.getFeedFlow(feedId).collect {
            feed = it
        }
    }

    private suspend fun loadFeed(){
        feed = feedRepo.getFeed(feedId).getOrNull()
        author = feed?.authorId?.let { userRepo.getAuthor(it).getOrNull() }
        myId = userRepo.getMyId()
    }
}