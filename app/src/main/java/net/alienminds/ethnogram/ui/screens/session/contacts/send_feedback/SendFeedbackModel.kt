package net.alienminds.ethnogram.ui.screens.session.contacts.send_feedback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.alienminds.ethnogram.service.feedback.FeedbackRepository
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

internal class SendFeedbackModel(
    private val userId: String,
    currentComment: String?,
    currentRating: Double?,
): AppScreenModel() {

    private val feedbackRepository by inject<FeedbackRepository>()

    var rating: Double? by mutableStateOf(currentRating)
    var comment: String by mutableStateOf(currentComment.orEmpty())

    var errorRating by mutableStateOf(false)
    var errorComment by mutableStateOf(false)

    fun sendFeedback(onSuccess: () -> Unit){
        errorRating = rating == null
        errorComment = comment.isEmpty()
        if (errorRating || errorComment) return
        rating?.let { rating ->
            launchWithLoading {
                feedbackRepository.addUserFeedback(userId, comment, rating)
                    .onSuccess {
                        onSuccess()
                    }
            }
        }
    }

    fun removeFeedback(onSuccess: () -> Unit) {
        launchWithLoading {
            feedbackRepository.removeUserFeedback(userId).onSuccess { onSuccess() }
        }
    }

}