package net.alienminds.ethnogram.service.auth

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.BuildConfig
import net.alienminds.ethnogram.service.auth.PhoneVerificationCallback.PhoneVerificationState
import net.alienminds.ethnogram.service.auth.entities.CurrentUser
import net.alienminds.ethnogram.service.auth.entities.SignInByPhoneResult
import net.alienminds.ethnogram.service.base.BaseService
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class AuthFBService internal constructor(): BaseService(), AuthService {

    private val auth = Firebase.auth

    override val isSignIn
        get() = auth.currentUser != null

    override val currentUser
        get() = auth.currentUser?.let { CurrentUser(it) }



    /**
     * @param phoneNumber - Phone number for sending SMS
     * @param activity - Required to perform a fallback reCAPTCHA for client verification
     * @return [SignInByPhoneResult]
     */
    @OptIn(FlowPreview::class)
    override suspend fun signInByPhone(
        phoneNumber: String,
        activity: Activity
    ) = withSave {
        val state = PhoneVerificationCallback()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(state)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        val result = state.value.takeIf {
            it !is PhoneVerificationState.Init
        }?: state.timeout(60.seconds).first {
            it !is PhoneVerificationState.Init
        }

        when(result){
            is PhoneVerificationState.Failed -> throw result.error
            is PhoneVerificationState.Completed -> SignInByPhoneResult.Completed(signInByCredential(result.credential))
            is PhoneVerificationState.CodeSent -> SignInByPhoneResult.NeedOTP(result.verificationId)
            else -> throw IllegalArgumentException("Unknown Exception")
        }
    }


    /**
     * @param verificationId - id obtained using [signInByPhone]
     * @param code - OTP code received via SMS
     * @return UserId
     */
    override suspend fun confirmPhone(
        verificationId: String,
        code: String
    ) = withSave{
        signInByCredential(
            credential = PhoneAuthProvider.getCredential(verificationId, code)
        )
    }


    override suspend fun logout() = withSave{
        Firebase.auth.signOut()
    }


    private suspend fun signInByCredential(
        credential: PhoneAuthCredential
    ): String {
        val result = auth.signInWithCredential(credential).await()
        return result.user?.apply {
            Log.d(logTag, "SignIn $displayName Success by phone: $phoneNumber, \nUserID: $uid")
        }?.uid?: throw IllegalStateException("User id is null")
    }



}



