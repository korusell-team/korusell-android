package net.alienminds.ethnogram.data.firestore.utils

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import net.alienminds.ethnogram.data.model.common.PagingInput
import net.alienminds.ethnogram.data.model.common.PagingMeta

internal fun Query.applyPaging(paging: PagingInput): Query =
    applyCursor(paging.cursor)
        .limit(paging.pageLimit.toLong())

internal fun Query.applyCursor(cursor: Any?): Query = when(cursor){
    null -> this
    is DocumentSnapshot -> startAfter(cursor)
    else -> {
        Log.e("FirestoreUserRepository", "Invalid Paging cursor, expected DocumentSnapshot, got $cursor")
        throw IllegalStateException("Invalid Paging cursor type")
    }
}

internal fun List<DocumentSnapshot>.toPagingMeta(pageLimit: Int) = PagingMeta(
    hasNext = size >= pageLimit,
    cursor = getOrNull(size - 1)
)