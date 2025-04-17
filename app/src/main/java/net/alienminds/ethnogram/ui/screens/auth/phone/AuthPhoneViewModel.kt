package net.alienminds.ethnogram.ui.screens.auth.phone

import android.content.Context
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.service.API
import net.alienminds.ethnogram.service.auth.entities.SignInByPhoneResult
import net.alienminds.ethnogram.ui.extentions.getRoot
import net.alienminds.ethnogram.ui.screens.auth.otp.AuthOTPScreen
import net.alienminds.ethnogram.ui.screens.session.SessionScreen
import net.alienminds.ethnogram.utils.findActivity
import net.alienminds.ethnogram.utils.getSuitableScreen

class AuthPhoneViewModel: StateScreenModel<AuthPhoneViewModel.Companion.State>(State()) {

    fun signIn(
        context: Context,
        navigator: Navigator,
        phoneNumber: String,
        callback: ()->Unit
    ) = screenModelScope.launch{
        context.findActivity()?.let { activity ->
            mutableState.value = State(loading = true)
            API.auth.signInByPhone(
                phoneNumber = phoneNumber,
                activity = activity
            ).apply {
                mutableState.value = State(
                    errorMessage = error?.localizedMessage?: error?.message
                )
                if (isSuccess) {
                    (data as? SignInByPhoneResult.Completed)?.run {
                        navigator.getRoot().replaceAll(getSuitableScreen(context))
                    }
                    (data as? SignInByPhoneResult.NeedOTP)?.run {
                        navigator.push(AuthOTPScreen(verificationId))
                    }
                }
                callback()
            }
        }
    }

    companion object {
        data class State(
            val loading: Boolean = false,
            val errorMessage: String? = null
        )
    }

}