package net.alienminds.ethnogram.ui.screens.session.contacts.all_feedbacks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.onEach
import net.alienminds.ethnogram.service.feed.entities.Author
import net.alienminds.ethnogram.service.feedback.FeedbackRepository
import net.alienminds.ethnogram.service.feedback.entities.UserFeedback
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

class AllFeedbacksModel(
    userId: String,
): AppScreenModel() {

    private val userRepo by inject<UserRepository>()
    private val feedbackRepo by inject<FeedbackRepository>()

    val user by userRepo.getUserFlow(userId).asStateWithLoading(null)
    val feedbacks by feedbackRepo.getUserFeedbacksFlow(userId).onEach {
        it.fetchAuthors()
    }.asStateWithLoading(emptyList())

    var authors by mutableStateOf<Map<String, Author>>(emptyMap())
        private set

    private suspend fun List<UserFeedback>.fetchAuthors(){
        val authorIds = mapNotNull { it.fromUserId }.toTypedArray()
        authors = userRepo.getAuthors(authorIds = authorIds).getOrNull().orEmpty()
    }

}