package net.alienminds.ethnogram.utils

import android.content.Context
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import net.alienminds.ethnogram.service.API
import net.alienminds.ethnogram.ui.screens.auth.AuthScreen
import net.alienminds.ethnogram.ui.screens.auth.onboarding.OnboardingScreen
import net.alienminds.ethnogram.ui.screens.auth.profile_setup.ProfileSetupScreen
import net.alienminds.ethnogram.ui.screens.session.SessionScreen
import kotlin.time.Duration.Companion.seconds

/***
 * @return actual [Screen] for the user taking into account the state of authorization, onboarding and the first launch
 */
internal suspend fun getSuitableScreen(
    ctx: Context,
) = when{
    AppLaunchServiceImpl(ctx).isFirstLaunch -> OnboardingScreen()
    API.auth.isSignIn.not() -> AuthScreen()
    isProfileComplete().not() -> ProfileSetupScreen()
    else -> SessionScreen()
}

/**
 * Call as later as possible. To avoid unnecessary requests for the server
 */
@OptIn(FlowPreview::class)
private suspend fun isProfileComplete() = runCatching {
    API.users.me.timeout(5.seconds)
        .first { it?.phone != null }?.isProfileComplete
}.getOrNull()?: false