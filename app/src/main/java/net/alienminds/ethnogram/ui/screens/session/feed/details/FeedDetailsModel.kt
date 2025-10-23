package net.alienminds.ethnogram.ui.screens.session.feed.details

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.onEach
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.feed.FeedRepository
import net.alienminds.ethnogram.service.feed.entities.Author
import net.alienminds.ethnogram.service.feed.entities.Feed
import net.alienminds.ethnogram.service.feed.entities.FeedComment
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject
import java.time.Instant

internal class FeedDetailsModel(
    private val feedId: String
): AppScreenModel() {

    private val feedRepo by inject<FeedRepository>()
    private val userRepo by inject<UserRepository>()
    private val authRepo by inject<AuthRepository>()

    private val myId by userRepo.myIdFlow.asState(null)

    var author by mutableStateOf<Author?>(null)
        private set

    var feed by feedRepo.getFeedFlow(feedId)
        .onEach { it?.fetchAuthor() }
        .asMutableStateWithLoading(null)
        private set

    val lastComment by derivedStateOf { feed?.comments?.lastComment() }
    var lastCommentAuthor by mutableStateOf<Author?>(null)
        private set

    val isFavorite by derivedStateOf { feed?.likeList?.any { it == myId } == true }

    fun changeFavoriteFeed(
        isFavorite: Boolean
    ){
        if (authRepo.isAnonymous){
            return
        }
        launchWithLoading{
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
    }

    private suspend fun Feed.fetchAuthor() = authorId?.let {
        author = userRepo.getAuthor(it).getOrNull()
        lastCommentAuthor = comments?.lastComment()?.userId?.let { authorId ->
            userRepo.getAuthor(authorId).getOrNull()
        }
    }

    private fun List<FeedComment>.lastComment() = maxByOrNull { it.createdAt ?: Instant.EPOCH }

}