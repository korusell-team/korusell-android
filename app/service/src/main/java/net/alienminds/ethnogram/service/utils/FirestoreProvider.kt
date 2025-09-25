package net.alienminds.ethnogram.service.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class FirestoreProvider {

    private val mutex = Mutex()
    private var firestore = Firebase.firestore

    fun get() = firestore

    suspend fun clearFirestoreCache() {
        mutex.withLock {
            Firebase.firestore.terminate().await()
            Firebase.firestore.clearPersistence().await()
            delay(300)
            firestore = FirebaseFirestore.getInstance()
        }
    }

}