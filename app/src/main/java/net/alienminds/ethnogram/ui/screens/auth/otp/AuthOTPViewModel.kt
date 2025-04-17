package net.alienminds.ethnogram.ui.screens.auth.otp

import android.content.Context
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import net.alienminds.ethnogram.service.API
import net.alienminds.ethnogram.ui.screens.auth.profile_setup.ProfileSetupScreen
import net.alienminds.ethnogram.ui.screens.session.SessionScreen
import net.alienminds.ethnogram.utils.AppScreenModel
import net.alienminds.ethnogram.utils.getSuitableScreen

internal class AuthOTPViewModel(
    private val contextRequester: () -> Context
): AppScreenModel(){

    private val context
        get() = contextRequester()

    fun signIn(
        rootNavigator: Navigator,
        verificationId: String,
        otpCode: String,
        callback: () -> Unit = {}
    ) = withLoadingScope{
        val result = API.auth.confirmPhone(verificationId, otpCode)
        error = result.error

        if (result.isSuccess) {
//            val firebaseUser = API.auth.currentUser ?: return@withLoadingScope
            rootNavigator.replaceAll(getSuitableScreen(context))

//            API.users.reloadFromServer()
//            val existingUser = withContext(Dispatchers.IO) {
//              API.users.getUser(firebaseUser.uid).firstOrNull()
//            }
//            if (existingUser == null) {
//                rootNavigator.replaceAll(ProfileSetupScreen())
//            } else {
//                rootNavigator.replaceAll(SessionScreen())
//            }
        }
        callback()
    }

    fun resendCode(){

    }
}