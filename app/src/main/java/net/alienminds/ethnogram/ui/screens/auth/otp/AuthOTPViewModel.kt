package net.alienminds.ethnogram.ui.screens.auth.otp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.ui.extentions.navigateByUserState
import net.alienminds.ethnogram.utils.AppScreenModel
import net.alienminds.ethnogram.utils.UserStateProvider
import org.koin.core.component.inject

internal class AuthOTPViewModel(
    private val verificationId: String
): AppScreenModel(){

    private val authRepo by inject<AuthRepository>()
    private val userStateProvider by inject<UserStateProvider>()

    var otpCode by mutableStateOf("")


    fun signIn(
        navigator: Navigator
    ) = launchWithLoading{
        authRepo.confirmPhone(verificationId, otpCode)
            .onFailure {
                error = it
            }.onSuccess {
                val userState = userStateProvider.getUserState()
                navigator.navigateByUserState(userState)
            }
    }

    fun resendCode(){

    }
}