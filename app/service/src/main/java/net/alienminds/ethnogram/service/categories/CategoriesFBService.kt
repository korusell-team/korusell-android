package net.alienminds.ethnogram.service.categories

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SnapshotListenOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.base.BaseService
import net.alienminds.ethnogram.service.categories.entities.Category

class CategoriesFBService internal constructor(): BaseService(), CategoriesService {

    private companion object{
        const val COLLECTION_NAME = "cats"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val collection
        get() = Firebase.firestore.collection(COLLECTION_NAME)

    private val snapshots = MutableStateFlow<QuerySnapshot?>(null)

    override val all
        get() = snapshots.map { snapshot ->
            snapshot?.map { Category(it) }?: emptyList()
        }

    init { listenCache() }

    private fun listenCache(){
        scope.launch{
            collection.get().await()
        }
        scope.launch {
            collection.addSnapshotListener(
                SnapshotListenOptions.Builder()
                    .setMetadataChanges(MetadataChanges.INCLUDE)
                    .setSource(ListenSource.CACHE)
                    .build()
            ) { value, error ->
                if (error != null) {
                    Log.w(logTag, "Listen failed.", error)
                    return@addSnapshotListener
                }
                value?.let { snapshots.tryEmit(it) }
            }
        }
    }

//    private fun getFlowSnapshot(): Flow<QuerySnapshot> {
//        scope.launch {
//            collection.get().await()
//        }
//        return channelFlow {
//            collection.addSnapshotListener(
//                SnapshotListenOptions.Builder()
//                    .setMetadataChanges(MetadataChanges.INCLUDE)
//                    .setSource(ListenSource.CACHE)
//                    .build()
//            ) { value, error ->
//                if (error != null) {
//                    Log.w(logTag, "Listen failed.", error)
//                    return@addSnapshotListener
//                }
//                launch{
//                    value?.let { send(it) }
//                }
//            }
//        }
//    }

}