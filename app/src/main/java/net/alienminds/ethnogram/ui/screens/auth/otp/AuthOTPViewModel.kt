package net.alienminds.ethnogram.ui.screens.auth.otp

import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import net.alienminds.ethnogram.service.API
import net.alienminds.ethnogram.ui.screens.auth.profile_setup.ProfileSetupScreen
import net.alienminds.ethnogram.ui.screens.session.SessionScreen
import net.alienminds.ethnogram.utils.AppScreenModel

class AuthOTPViewModel: AppScreenModel(){


    fun signIn(
        rootNavigator: Navigator,
        verificationId: String,
        otpCode: String
    ) = withLoadingScope{
        val result = API.auth.confirmPhone(verificationId, otpCode)
        error = result.error

        if (result.isSuccess) {
            val firebaseUser = API.auth.currentUser ?: return@withLoadingScope
            API.users.reloadFromServer()
            val existingUser = withContext(Dispatchers.IO) {
              API.users.getUser(firebaseUser.uid).firstOrNull()
            }
            if (existingUser == null) {
                rootNavigator.replaceAll(ProfileSetupScreen())
            } else {
                rootNavigator.replaceAll(SessionScreen())
            }
        }
    }

    fun resendCode(){

    }
}