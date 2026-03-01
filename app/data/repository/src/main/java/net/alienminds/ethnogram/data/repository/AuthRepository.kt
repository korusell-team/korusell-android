package net.alienminds.ethnogram.data.repository

import net.alienminds.ethnogram.data.model.auth.SignInByPhoneResult
import net.alienminds.ethnogram.data.model.auth.UserIdentity
import net.alienminds.ethnogram.data.model.core.GetRequestExecutor
import net.alienminds.ethnogram.data.model.core.MutationRequestExecutor
import net.alienminds.ethnogram.data.model.core.QueryRequestExecutor

interface AuthRepository {

    fun signInWithPhone(
        phone: String
    ): MutationRequestExecutor<SignInByPhoneResult>

    fun confirmPhoneCode(
        verificationId: String,
        code: String
    ): MutationRequestExecutor<UserIdentity>

    fun signInAnonymously(): MutationRequestExecutor<UserIdentity>

    fun logout(): MutationRequestExecutor<Unit>

    fun getIdentity(): GetRequestExecutor<UserIdentity>

}