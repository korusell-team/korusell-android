package net.alienminds.ethnogram.service.feed

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.SnapshotListenOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.base.BaseRepository
import net.alienminds.ethnogram.service.feed.entities.Feed
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.service.utils.FirestoreProvider
import net.alienminds.ethnogram.service.utils.addCacheListener
import java.time.Instant

class FeedRepository internal constructor(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository,
    private val ioScope: CoroutineScope,
    private val firestoreProvider: FirestoreProvider
): BaseRepository(){

    private val collection
        get() = firestoreProvider.get().collection("posts")

    private val listenerMutex = Mutex()
    private var listenerRegistration: ListenerRegistration? = null

    @Volatile
    private var isSyncFeeds = false

    private val _feedsFlow = MutableStateFlow<List<Feed>?>(null)
    val feedsFlow: StateFlow<List<Feed>?> get() = _feedsFlow

    init {
        waitLogout()
    }

    suspend fun getFeeds() = apiQuery{
        ensureListening()
        if (isSyncFeeds) {
            feedsFlow.value?.let { return@apiQuery it }
        }
        val result = collection
            .get()
            .await()
            .documents
            .mapNotNull { Feed(it) }
            .relevantSort()
        isSyncFeeds = true
        return@apiQuery result
    }

    suspend fun getFeed(
        feedId: String
    ) = apiQuery{
        if (isSyncFeeds) {
            feedsFlow.value?.find { it.id == feedId }?.let { return@apiQuery it }
        }

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
                launch {
                    val feed = snapshot?.let(::Feed)
                    trySend(feed).isSuccess
                }
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

    private fun waitLogout(){
        authRepo.logoutFlow.onEach {
            ioScope.launch {
                stopListening()
                clearCache()
            }
        }.launchIn(ioScope)
    }

    private fun clearCache(){
        _feedsFlow.value = null
        isSyncFeeds = false
    }

    private suspend fun stopListening() {
        listenerMutex.withLock {
            listenerRegistration?.remove()
            listenerRegistration = null
        }
    }

    private suspend fun ensureListening() {
        listenerMutex.withLock {
            if (listenerRegistration == null) {
                listenCache()
            }
        }
    }

    private fun listenCache(){
        val registration = collection.addCacheListener { snapshot, error ->
            if (snapshot != null && snapshot.isEmpty.not()) {
                val feeds = snapshot.documents.mapNotNull(::Feed)
                _feedsFlow.value = feeds.relevantSort()
            }
        }
        ioScope.launch {
            listenerMutex.withLock {
                listenerRegistration = registration
            }
        }
    }

    private fun List<Feed>.relevantSort() = filter {
        it.postValidUntil?.isAfter(Instant.now()) != false
    }.sortedWith(
        compareByDescending<Feed>{
            it.isPromotedNow
        }.thenByDescending {
            it.createdAt
        }
    )

}