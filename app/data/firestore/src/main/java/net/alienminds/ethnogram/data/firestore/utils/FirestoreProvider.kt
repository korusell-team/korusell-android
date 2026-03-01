package net.alienminds.ethnogram.data.firestore.utils

import android.util.Log
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class FirestoreProvider internal constructor(){

    private val mutex = Mutex()
    private var firestore = Firebase.firestore

    fun getFirestore() = firestore

    suspend fun clearFirestoreCache() {
        mutex.withLock {
            try {
                Firebase.firestore.terminate().await()
                Firebase.firestore.clearPersistence().await()
            } catch (e: Exception){
                Log.e("FirestoreProvider", "clearFirestoreCache: ${e.message}")
            } finally {
                firestore = FirebaseFirestore.getInstance()
            }
        }
    }

}