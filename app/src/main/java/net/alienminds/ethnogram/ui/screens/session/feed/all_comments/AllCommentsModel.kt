package net.alienminds.ethnogram.ui.screens.session.feed.all_comments

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.onEach
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.feed.FeedRepository
import net.alienminds.ethnogram.service.feed.entities.Author
import net.alienminds.ethnogram.service.feed.entities.Feed
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

internal class AllCommentsModel(
    private val feedId: String
): AppScreenModel() {

    private val feedRepo by inject<FeedRepository>()
    private val userRepo by inject<UserRepository>()
    private val authRepo by inject<AuthRepository>()

    private val feed by feedRepo.getFeedFlow(feedId)
        .onEach { it?.fetchAuthors() }
        .asStateWithLoading(null)

    val isAnonymous get() = authRepo.isAnonymous

    val me by userRepo.meFlow.asState(null)
    val comments by derivedStateOf { feed?.comments.orEmpty().sortedByDescending { it.createdAt } }
    var authors by mutableStateOf<Map<String, Author>>(emptyMap())
        private set

    var myCommentInput by mutableStateOf("")

    fun sendComment(){
        if (isAnonymous) return
        launchWithLoading {
            feedRepo.addComment(
                feedId = feedId,
                comment = myCommentInput
            ).onSuccess {
                myCommentInput = ""
            }.onFailure {
                error = it
            }
        }
    }

    private suspend fun Feed.fetchAuthors() {
        val authorIds = comments?.mapNotNull { it.userId }?.toTypedArray()
        if (authorIds.isNullOrEmpty()) return
        authors = userRepo.getAuthors(authorIds = authorIds).getOrNull().orEmpty()
    }


}