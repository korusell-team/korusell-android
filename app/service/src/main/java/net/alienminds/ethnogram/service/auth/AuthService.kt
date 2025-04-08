package net.alienminds.ethnogram.service.auth

import android.app.Activity
import net.alienminds.ethnogram.service.auth.entities.CurrentUser
import net.alienminds.ethnogram.service.auth.entities.SignInByPhoneResult
import net.alienminds.ethnogram.service.base.entities.ServiceResult

interface AuthService{


    val isSignIn: Boolean
    val isFirstLaunch: Boolean

    suspend fun signInByPhone(phoneNumber: String, activity: Activity): ServiceResult<SignInByPhoneResult>
    suspend fun confirmPhone(verificationId: String, code: String): ServiceResult<String>
    val currentUser: CurrentUser?
    suspend fun logout(): ServiceResult<Unit>
}