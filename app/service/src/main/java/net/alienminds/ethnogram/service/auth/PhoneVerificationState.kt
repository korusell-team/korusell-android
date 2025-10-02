package net.alienminds.ethnogram.service.auth

import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow



@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
internal class PhoneVerificationCallback:
    PhoneAuthProvider.OnVerificationStateChangedCallbacks(),
    MutableStateFlow<PhoneVerificationCallback.PhoneVerificationState> by MutableStateFlow(
        PhoneVerificationState.Init
    ) {

    private fun emitValue(newState: PhoneVerificationState){
        if (this.tryEmit(newState).not()){
            this.value = newState
        }
    }

    sealed class PhoneVerificationState{

        data object Init: PhoneVerificationState()

        data class Completed(
            val credential: PhoneAuthCredential
        ): PhoneVerificationState()

        data class Failed(
            val error: FirebaseException
        ): PhoneVerificationState()

        data class CodeSent(
            val verificationId: String,
        ): PhoneVerificationState()

    }

    override fun onVerificationCompleted(
        credential: PhoneAuthCredential
    ) = emitValue(PhoneVerificationState.Completed(credential))

    override fun onVerificationFailed(
        error: FirebaseException
    ) = emitValue(PhoneVerificationState.Failed(error))

    override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) = emitValue(PhoneVerificationState.CodeSent(verificationId))

    override fun onCodeAutoRetrievalTimeOut(
        verificationId: String
    ) = emitValue(PhoneVerificationState.CodeSent(verificationId))

}