package net.alienminds.ethnogram.data.firestore.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import net.alienminds.ethnogram.data.firestore.executors.base.BaseGetRequestExecutor
import net.alienminds.ethnogram.data.firestore.executors.base.BaseMutationRequestExecutor
import net.alienminds.ethnogram.data.firestore.utils.PhoneVerificationCallback
import net.alienminds.ethnogram.data.firestore.utils.PhoneVerificationCallback.PhoneVerificationState
import net.alienminds.ethnogram.data.model.auth.SignInByPhoneResult
import net.alienminds.ethnogram.data.model.auth.UserIdentity
import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.model.core.FetchState
import net.alienminds.ethnogram.data.model.core.GetRequestExecutor
import net.alienminds.ethnogram.data.model.core.MutationRequestExecutor
import net.alienminds.ethnogram.data.repository.AuthRepository
import net.alienminds.ethnogram.data.repository.utils.ActivityProvider
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

class FirestoreAuthRepository(
    private val activityProvider: ActivityProvider
) : AuthRepository {

    override fun signInWithPhone(
        phone: String
    ): MutationRequestExecutor<SignInByPhoneResult> =
        BaseMutationRequestExecutor {
            runCatching {
                getIdentity().get(FetchMode.NetworkOnly)
                val fbAuth = Firebase.auth
                val activity = activityProvider.getActivity()
                val callbackState = PhoneVerificationCallback()
                val options = PhoneAuthOptions.newBuilder(fbAuth)
                    .setPhoneNumber(phone)
                    .setTimeout(60, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbackState)
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)

                val result = withTimeout(2.minutes) {
                    callbackState.state.first {
                        it !is PhoneVerificationState.Init
                    }
                }

                when (result) {
                    PhoneVerificationState.Init -> throw IllegalStateException(
                        "Phone verification state is init"
                    )

                    is PhoneVerificationState.CodeSent -> SignInByPhoneResult.CodeSent(
                        result.verificationId
                    )

                    is PhoneVerificationState.Completed -> {
                        val result = fbAuth.signInWithCredential(result.credential).await()
                        val user = result.user
                            ?: throw IllegalStateException("Authorization is success, but User is null")
                        SignInByPhoneResult.Completed(user.toUserIdentity())
                    }

                    is PhoneVerificationState.Failed -> throw result.error
                    is PhoneVerificationState.Timeout -> throw IllegalStateException(
                        "Phone verification timeout"
                    )
                }
            }.toFetchState()
        }

    override fun confirmPhoneCode(
        verificationId: String,
        code: String
    ): MutationRequestExecutor<UserIdentity> = BaseMutationRequestExecutor{
        runCatching {
            val credentials = PhoneAuthProvider.getCredential(verificationId, code)
            val result = Firebase.auth.signInWithCredential(credentials).await()
            val user = result.user
                ?: throw IllegalStateException("Authorization is success, but User is null")
            user.toUserIdentity()
        }.toFetchState()
    }

    override fun signInAnonymously(): MutationRequestExecutor<UserIdentity> = BaseMutationRequestExecutor{
        runCatching {
            val result = Firebase.auth.signInAnonymously().await()
            val user = result.user
                ?: throw IllegalStateException("Authorization is success, but User is null")
            user.toUserIdentity()
        }.toFetchState()
    }

    override fun logout(): MutationRequestExecutor<Unit> = BaseMutationRequestExecutor{
        runCatching {
            Firebase.auth.signOut()
        }.toFetchState()
    }


    override fun getIdentity(): GetRequestExecutor<UserIdentity> = BaseGetRequestExecutor{
        //IMPORTANT: this method ignore fetchMode value
        runCatching {
            val user = Firebase.auth.currentUser
                ?: throw IllegalStateException("User is null")
            user.toUserIdentity()
        }.toFetchState()
    }


    private fun FirebaseUser.toUserIdentity() = UserIdentity(
        id = uid,
        phoneNumber = phoneNumber,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
        isAnonymous = isAnonymous
    )

    private fun <T>Result<T>.toFetchState(): FetchState<T>{
        val data = getOrNull()
        return if (isSuccess && data != null) {
            FetchState.Success(data)
        } else {
            FetchState.Error(exceptionOrNull()?: IllegalStateException("Unknown error"))
        }
    }

}