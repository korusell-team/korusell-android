package net.alienminds.ethnogram.service.auth.entities

import com.google.firebase.auth.FirebaseUser

class CurrentUser internal constructor(
    internal val fbUser: FirebaseUser
){
    val uid
        get() = fbUser.uid

    val phoneNumber
        get() = fbUser.phoneNumber

    val displayName
        get() = fbUser.displayName

    val photoUrl
        get() = fbUser.photoUrl
}
