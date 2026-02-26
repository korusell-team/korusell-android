package net.alienminds.ethnogram.ui.screens.session.contacts.profile

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.data.DataRepository
import net.alienminds.ethnogram.service.feed.entities.Author
import net.alienminds.ethnogram.service.feedback.FeedbackRepository
import net.alienminds.ethnogram.service.feedback.entities.UserFeedback
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.ui.extentions.root
import net.alienminds.ethnogram.ui.screens.auth.AuthScreen
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

class ProfileViewModel(
    private val userId: String?,
    private val navigator: Navigator?
): AppScreenModel() {

    private val authRepo by inject<AuthRepository>()
    private val userRepo by inject<UserRepository>()
    private val dataRepo by inject<DataRepository>()
    private val feedbackRepository by inject<FeedbackRepository>()

    private val allCities by dataRepo.getCitiesFlow().asState(emptyList())
    private val allCategories by dataRepo.getCategoriesFlow().asState(emptyList())

    private val myId by userRepo.myIdFlow.asState("")

    val user by when(userId == null){
        true -> userRepo.meFlow.asStateWithLoading(null)
        false -> userRepo.getUserFlow(userId).catch {
            navigator?.pop()
        }.asStateWithLoading(null)
    }

    val isMe by derivedStateOf { myId == user?.uid }

    private val feedbacksFlow by derivedStateOf {
        feedbackRepository.getUserFeedbacksFlow(userId?: myId)
            .onEach { it.fetchAuthors() }
            .map { it.sortedWith(
                compareByDescending<UserFeedback> { it.fromUserId == myId }
                    .thenByDescending { it.createdAt }
            ) }
    }
    val feedbacks by feedbacksFlow.asState(emptyList())
    val myFeedback by derivedStateOf { feedbacks.find { it.fromUserId == myId } }

    var authors by mutableStateOf<Map<String, Author>>(emptyMap())
        private set


    val city by derivedStateOf {
        user?.cities?.mapNotNull{ id ->
            allCities.find { it.id == id }
        }?: emptyList()
    }

    val categories by derivedStateOf {
        user?.categories?.mapNotNull { id ->
            allCategories.find { it.id == id }
        }?: emptyList()
    }

    val isFavorite by derivedStateOf { user?.likes?.any { it == myId } == true }



    fun changeFavorite(value: Boolean) = launchWithLoading{
        user?.uid?.let {
            userRepo.favoriteUser(it, value)
        }
    }

    fun blockUser(navigator: Navigator?) = launchWithLoading{
        user?.uid?.let {
            val isSuccess = userRepo.blockUser(it).getOrNull() == true
            if (isSuccess){
                navigator?.pop()
            }
        }

    }

    fun reportUser(navigator: Navigator?) = launchWithLoading{
        user?.uid?.let {
            val isSuccess = userRepo.reportUser(it).getOrNull() == true
            if (isSuccess){
                navigator?.pop()
            }
        }
    }

    fun logout(navigator: Navigator?) = launchWithLoading{
        authRepo.logout()
        navigator?.root?.replaceAll(AuthScreen())
    }

    private suspend fun List<UserFeedback>.fetchAuthors(){
        val authorIds = mapNotNull { it.fromUserId }.toTypedArray()
        authors = userRepo.getAuthors(authorIds = authorIds).getOrNull()?: emptyMap()
    }



}