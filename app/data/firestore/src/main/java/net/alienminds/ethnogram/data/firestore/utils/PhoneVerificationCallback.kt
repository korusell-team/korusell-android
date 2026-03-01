package net.alienminds.ethnogram.data.firestore.utils

import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class PhoneVerificationCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

    private val _state = MutableStateFlow<PhoneVerificationState>(PhoneVerificationState.Init)
    val state: StateFlow<PhoneVerificationState> = _state.asStateFlow()

    private fun emitValue(newState: PhoneVerificationState) {
        if (!_state.tryEmit(newState)) {
            _state.value = newState
        }
    }

    override fun onVerificationCompleted(credential: PhoneAuthCredential) =
        emitValue(PhoneVerificationState.Completed(credential))

    override fun onVerificationFailed(error: FirebaseException) =
        emitValue(PhoneVerificationState.Failed(error))

    override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) = emitValue(PhoneVerificationState.CodeSent(verificationId))

    override fun onCodeAutoRetrievalTimeOut(verificationId: String) =
        emitValue(PhoneVerificationState.Timeout(verificationId))

    sealed class PhoneVerificationState {
        object Init : PhoneVerificationState()
        data class Completed(val credential: PhoneAuthCredential) : PhoneVerificationState()
        data class Failed(val error: FirebaseException) : PhoneVerificationState()
        data class CodeSent(val verificationId: String) : PhoneVerificationState()
        data class Timeout(val verificationId: String) : PhoneVerificationState()
    }
}