package net.alienminds.ethnogram.service.feed

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.SnapshotListenOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.BuildConfig
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.base.BaseRepository
import net.alienminds.ethnogram.service.feed.entities.Feed
import net.alienminds.ethnogram.service.feed.entities.FeedComment
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.service.utils.FirestoreProvider
import net.alienminds.ethnogram.service.utils.addCacheListener
import java.time.Instant
import java.util.UUID

class FeedRepository internal constructor(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository,
    private val ioScope: CoroutineScope,
    private val firestoreProvider: FirestoreProvider
): BaseRepository(){

    private val collection
        get() = firestoreProvider.get().collection("posts")

    private var isSyncFeeds = false

    init {
        waitLogout()
    }

    fun getFeedsFlow() = callbackFlow{
        val listenerRegistration = runCatching { collection.addCacheListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addCacheListener
            }
            val feeds = snapshot?.documents
                ?.mapNotNull(::Feed)
                ?.relevantSort()
            trySend(feeds).isSuccess
        } }.getOrNull()

        runCatching {
            collection.get(Source.CACHE).await().documents
                .mapNotNull(::Feed)
                .relevantSort()
                .also { trySend(it) }
        }.onFailure { trySend(emptyList()) }

        if (isSyncFeeds.not()){
            runCatching {
                collection.get().await().documents
                    .mapNotNull(::Feed)
                    .relevantSort()
                    .also {
                        isSyncFeeds = true
                        trySend(it)
                    }
            }.onFailure { trySend(emptyList()) }
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    fun getFeedFlow(feedId: String): Flow<Feed?> = callbackFlow {
        val listenerRegistration = runCatching {
            collection
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
        }.getOrNull()

        runCatching {
            collection.document(feedId)
                .get(Source.CACHE).await()
                .let(::Feed)
                .also { trySend(it) }
        }.onFailure { trySend(null) }

        if (isSyncFeeds.not()) {
            runCatching {
                collection.document(feedId)
                    .get().await()
                    .let(::Feed)
                    .also { trySend(it) }
            }.onFailure { trySend(null) }
        }

        awaitClose {
            listenerRegistration?.remove()
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

    suspend fun addComment(
        feedId: String,
        comment: String
    ) = apiQuery {
        val me = userRepo.getMe().getOrNull()
            ?: throw IllegalStateException("Failed to get current user")
        val userId = me.uid?: throw IllegalStateException("Current user has no ID")
        val commentMap = mapOf(
            "userId" to userId,
            "userName" to me.fullName,
            "text" to comment,
            "createdAt" to FieldValue.serverTimestamp(),
            "userAvatarUrl" to me.image.firstOrNull(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        val docRef = collection.document(feedId)
        val snapshot = docRef.get(Source.SERVER).await()
        val rawComments = snapshot.get("comments") as? HashMap<*, *>
        val commentsMap = rawComments?.mapKeys { it.key.toString() }?.toMutableMap() ?: mutableMapOf()
        val key = UUID.randomUUID().toString()
        commentsMap[key] = commentMap
        val task = docRef.update("comments", commentsMap)
        task.await()
        task.isSuccessful
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
            isSyncFeeds = false
        }.launchIn(ioScope)
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