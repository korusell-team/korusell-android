package net.alienminds.ethnogram.ui.screens.session.contacts.profile

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.data.DataRepository
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.data.entities.City
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.ui.extentions.root
import net.alienminds.ethnogram.ui.screens.auth.AuthScreen
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

class ProfileViewModel(
    private val userId: String?
): AppScreenModel() {

    private val authRepo by inject<AuthRepository>()
    private val userRepo by inject<UserRepository>()
    private val dataRepo by inject<DataRepository>()

    private var allCities by mutableStateOf<List<City>>(emptyList())
    private var allCategories by mutableStateOf<List<Category>>(emptyList())

    private val me by userRepo.meFlow.asState(null)

    var user by mutableStateOf<User?>(null)
        private set

    val isMe by derivedStateOf { me?.uid == user?.uid }


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

    val isFavorite by derivedStateOf { user?.likes?.any { it == me?.uid } == true }


    init {
        loadData()
    }


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

    private fun loadData(){
        loading = true
        screenModelScope.launch {
            val myId = userRepo.getMe().getOrNull()?.uid.orEmpty()
            allCities = dataRepo.getCities().getOrNull().orEmpty()
            allCategories = dataRepo.getCategories().getOrNull().orEmpty()

            userRepo.getUserFlow(userId?: myId).collect {
                user = it
                if (it != null) {
                    loading = false
                }
            }
            loading = false
        }
    }

}