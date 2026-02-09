package net.alienminds.ethnogram.utils

import kotlinx.coroutines.FlowPreview
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.prefs.PrefsRepository
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.ui.screens.auth.AuthScreen
import net.alienminds.ethnogram.ui.screens.auth.onboarding.OnboardingScreen
import net.alienminds.ethnogram.ui.screens.auth.profile_setup.ProfileSetupScreen
import net.alienminds.ethnogram.ui.screens.session.SessionScreen


class UserStateProvider(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val prefsRepository: PrefsRepository
){

    suspend fun getUserState() = when{
        prefsRepository.isFirstLaunch -> UserState.FirstLaunch
        authRepository.isSignIn.not() -> UserState.Unauthorized
        authRepository.isAnonymous -> UserState.Authorized
        isProfileComplete().not() -> UserState.ProfileNotCompleted
        else -> UserState.Authorized
    }

    @OptIn(FlowPreview::class)
    private suspend fun isProfileComplete() = userRepository
        .getMe()
        .getOrNull()
        ?.isProfileComplete == true

}

internal fun UserState.getScreen(checkProfileComplete: Boolean = true) = when(this){
    UserState.FirstLaunch -> OnboardingScreen()
    UserState.Unauthorized -> AuthScreen()
    UserState.ProfileNotCompleted -> when(checkProfileComplete) {
        true -> ProfileSetupScreen()
        false -> SessionScreen()
    }
    UserState.Authorized -> SessionScreen()
}

sealed class UserState{
    object FirstLaunch: UserState()
    object Unauthorized: UserState()
    object ProfileNotCompleted: UserState()
    object Authorized: UserState()
}