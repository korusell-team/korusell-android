package net.alienminds.ethnogram.ui.screens.auth.phone

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.auth.entities.SignInByPhoneResult
import net.alienminds.ethnogram.ui.extentions.navigateByUserState
import net.alienminds.ethnogram.ui.screens.auth.otp.AuthOTPScreen
import net.alienminds.ethnogram.utils.AppScreenModel
import net.alienminds.ethnogram.utils.UserStateProvider
import net.alienminds.ethnogram.utils.findActivity
import net.alienminds.ethnogram.utils.phoneToFbPhone
import org.koin.core.component.inject

class AuthPhoneViewModel: AppScreenModel() {

    private val authRepo by inject<AuthRepository>()
    private val userStateProvider by inject<UserStateProvider>()

    var phone by mutableStateOf("")

    fun signIn(
        context: Context,
        navigator: Navigator
    ) = launchWithLoading {
        context.findActivity()?.let { activity ->
            authRepo.signInByPhone(
                phoneNumber = phoneToFbPhone(phone),
                activity = activity
            ).onFailure {
                error = it
            }.onSuccess{ when(it){
                is SignInByPhoneResult.Completed -> {
                    val userState = userStateProvider.getUserState()
                    navigator.navigateByUserState(userState)
                }
                is SignInByPhoneResult.NeedOTP -> navigator.push(AuthOTPScreen(it.verificationId))
            } }

        }
    }
}