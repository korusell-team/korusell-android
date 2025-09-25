package net.alienminds.ethnogram.ui.screens.auth.onboarding

import cafe.adriel.voyager.navigator.Navigator
import net.alienminds.ethnogram.service.prefs.PrefsRepository
import net.alienminds.ethnogram.ui.extentions.navigateByUserState
import net.alienminds.ethnogram.utils.AppScreenModel
import net.alienminds.ethnogram.utils.UserStateProvider
import org.koin.core.component.inject
import kotlin.getValue

internal class OnboardingModel(
    private val navigatorRequester: () -> Navigator
): AppScreenModel() {

    private val prefsRepo by inject<PrefsRepository>()
    private val userStateProvider by inject<UserStateProvider>()

    private val navigator
        get() = navigatorRequester()

    fun goNext(){
        launchWithLoading{
            prefsRepo.isFirstLaunch = false
            val userState = userStateProvider.getUserState()
            navigator.navigateByUserState(userState)
        }
    }


}