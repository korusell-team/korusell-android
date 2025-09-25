package net.alienminds.ethnogram.service.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.auth.PhoneVerificationCallback.PhoneVerificationState
import net.alienminds.ethnogram.service.auth.entities.CurrentUser
import net.alienminds.ethnogram.service.auth.entities.SignInByPhoneResult
import net.alienminds.ethnogram.service.base.BaseRepository
import net.alienminds.ethnogram.service.utils.FirestoreProvider
import net.alienminds.ethnogram.service.utils.clearAppCache
import java.util.concurrent.TimeUnit

class AuthRepository internal constructor(
    private val firestoreProvider: FirestoreProvider,
    private val context: Context
): BaseRepository() {

    private val auth = Firebase.auth

    val isSignIn
        get() = auth.currentUser != null

    val currentUser
        get() = auth.currentUser?.let { CurrentUser(it) }

    private val _logoutFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    internal val logoutFlow = _logoutFlow.asSharedFlow()



    @OptIn(FlowPreview::class)
    suspend fun signInByPhone(
        phoneNumber: String,
        activity: Activity
    ) = apiQuery {
        val state = PhoneVerificationCallback()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(state)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)


        val result = state.awaitState{
            it !is PhoneVerificationState.Init
        }

        when(result){
            is PhoneVerificationState.Failed -> throw result.error
            is PhoneVerificationState.Completed -> SignInByPhoneResult.Completed(signInByCredential(result.credential))
            is PhoneVerificationState.CodeSent -> SignInByPhoneResult.NeedOTP(result.verificationId)
            else -> throw IllegalArgumentException("Unknown Exception")
        }
    }

    suspend fun confirmPhone(
        verificationId: String,
        code: String
    ) = apiQuery{
        signInByCredential(
            credential = PhoneAuthProvider.getCredential(verificationId, code)
        )
    }


    suspend fun logout(): Result<Unit> = apiQuery{
        Firebase.auth.signOut()
        firestoreProvider.clearFirestoreCache()
        context.clearAppCache()
        _logoutFlow.emit(Unit)
        Log.d(logTag, "Logout Success")
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



