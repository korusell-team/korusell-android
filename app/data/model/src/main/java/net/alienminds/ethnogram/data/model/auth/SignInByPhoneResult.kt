package net.alienminds.ethnogram.data.model.auth

sealed interface SignInByPhoneResult {
    data class CodeSent(val verificationId: String) : SignInByPhoneResult
    data class Completed(val user: UserIdentity) : SignInByPhoneResult
}