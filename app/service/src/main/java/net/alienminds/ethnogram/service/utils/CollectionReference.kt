package net.alienminds.ethnogram.service.utils

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SnapshotListenOptions

internal fun CollectionReference.addCacheListener(
    onEvent: (snapshot: QuerySnapshot?, error: Exception?) -> Unit
): ListenerRegistration = this.addSnapshotListener(
    SnapshotListenOptions.Builder()
        .setMetadataChanges(MetadataChanges.INCLUDE)
        .setSource(ListenSource.CACHE)
        .build()
){ snapshot, error ->
    if (error != null) {
        Log.w("CacheListener", "Listen failed.", error)
        return@addSnapshotListener
    }
    onEvent(snapshot, error)
}