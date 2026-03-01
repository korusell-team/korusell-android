package net.alienminds.ethnogram.data.model.auth

import net.alienminds.ethnogram.data.model.common.ID

data class UserIdentity(
    val id: ID,
    val phoneNumber: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean
)