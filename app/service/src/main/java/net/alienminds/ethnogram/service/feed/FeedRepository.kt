package net.alienminds.ethnogram.service.feed

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.SnapshotListenOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.base.BaseRepository
import net.alienminds.ethnogram.service.feed.entities.Feed
import net.alienminds.ethnogram.service.user.UserRepository

class FeedRepository internal constructor(
    private val userRepo: UserRepository,
    private val isScope: CoroutineScope,
    firestore: FirebaseFirestore = Firebase.firestore
): BaseRepository(){

    private val collection = firestore.collection("posts")

    private val _feedsFlow = MutableStateFlow<List<Feed>?>(null)
    val feedsFlow: StateFlow<List<Feed>?> get() = _feedsFlow

    init {
        listenCache()
    }

    suspend fun getFeeds() = apiQuery{
        feedsFlow.value?.let {
            return@apiQuery it
        }
        collection
            .get()
            .await()
            .documents
            .mapNotNull { Feed(it) }
            .sortedWith(
                compareByDescending<Feed>{
                    it.isPromotedNow
                }.thenByDescending {
                    it.createdAt
                }
            )
    }

    suspend fun getFeed(
        feedId: String
    ) = apiQuery{
        feedsFlow.value?.find { it.id == feedId }?.let { return@apiQuery it }

        collection
            .document(feedId)
            .get()
            .await()
            .let { Feed(it) }
    }

    fun getFeedFlow(feedId: String): Flow<Feed?> = callbackFlow {
        launch {
            val initial = getFeed(feedId).getOrNull()
            trySend(initial).isSuccess
        }

        val listenerRegistration = collection
            .document(feedId)
            .addSnapshotListener(
                SnapshotListenOptions.Builder()
                    .setMetadataChanges(MetadataChanges.INCLUDE)
                    .setSource(ListenSource.CACHE)
                    .build()
            ) { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val feed = snapshot?.let(::Feed)
                trySend(feed).isSuccess
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    suspend fun favoriteFeed(
        feedId: String,
        isFavorite: Boolean
    ) = apiQuery{
        val myId = userRepo.getMyId()
        when(isFavorite){
            true -> addToArray(feedId, "likelist", myId)
            false -> removeFromArray(feedId, "likelist", myId)
        }
    }

    private suspend fun <T>addToArray(
        feedId: String,
        field: String,//Migrate to Field
        value: T
    ): Boolean {
        val updateMap = mapOf(
            field to FieldValue.arrayUnion(value)
        )
        val task = collection
            .document(feedId)
            .set(updateMap, SetOptions.merge())

        task.await()
        return task.isSuccessful
    }

    private suspend fun <T>removeFromArray(
        feedId: String,
        field: String,//Migrate to Field
        value: T
    ): Boolean {
        val updateMap = mapOf(
            field to FieldValue.arrayRemove(value)
        )
        val task = collection
            .document(feedId)
            .set(updateMap, SetOptions.merge())

        task.await()
        return task.isSuccessful
    }

    private fun listenCache(){
        isScope.launch {
            collection.addSnapshotListener(
                SnapshotListenOptions.Builder()
                    .setMetadataChanges(MetadataChanges.INCLUDE)
                    .setSource(ListenSource.CACHE)
                    .build()
            ) { snapshot, error ->
                if (error != null) {
                    Log.w(logTag, "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.isEmpty.not()) {
                    val feeds = snapshot.documents.mapNotNull(::Feed)
                    _feedsFlow.value = feeds.sortedWith(
                        compareByDescending<Feed>{
                            it.isPromotedNow
                        }.thenByDescending {
                            it.createdAt
                        }
                    )

                }
            }
        }
    }



}