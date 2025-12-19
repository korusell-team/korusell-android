package net.alienminds.ethnogram.service.utils

import com.google.firebase.firestore.DocumentSnapshot
import net.alienminds.ethnogram.service.base.entities.Field
import net.alienminds.ethnogram.service.user.entities.UserType
import java.time.Instant


internal inline fun <reified T>DocumentSnapshot.getValue(field: Field<T>): T{
    return data?.withDefault { field.defaultValue() }
        ?.let { it[field.key] as? T? }
        ?: field.defaultValue()
}

internal fun DocumentSnapshot.getInstant(field: Field<Instant?>): Instant? =
    getTimestamp(field.key)?.toInstant() ?: field.defaultValue()

internal fun DocumentSnapshot.getUserType(field: Field<UserType>): UserType =
    when(getBoolean(field.key)){
        true -> UserType.BUSINESS
        false -> UserType.PERSONAL
        else -> field.defaultValue()
    }