package net.alienminds.ethnogram.service.feedback

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.SnapshotListenOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.base.BaseRepository
import net.alienminds.ethnogram.service.base.entities.InputField
import net.alienminds.ethnogram.service.feedback.entities.UserFeedback
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.service.utils.FirestoreProvider

class FeedbackRepository internal constructor(
    private val firestoreProvider: FirestoreProvider,
    private val userRepository: UserRepository
): BaseRepository(){

    private val userCollection
        get() = firestoreProvider.get().collection("users")

    private var syncFeedbacks = mutableListOf<String>()//list user uid


    /** Получить отзывы пользователя */
    private suspend fun getUserFeedbacks(userId: String) = apiQuery {
        val isCached = syncFeedbacks.contains(userId)
        return@apiQuery userCollection
            .document(userId)
            .collection("feedback")
            .get(when(isCached) {
                true -> Source.CACHE
                false -> Source.DEFAULT
            })
            .await()
            .documents
            .mapNotNull { UserFeedback(it, userId) }
            .also {
                if (isCached.not()) {
                    syncFeedbacks.add(userId)
                }
            }
    }

    /** Получить отзывы пользователя в реальном времени */
    fun getUserFeedbacksFlow(userId: String): Flow<List<UserFeedback>> = callbackFlow {
        val isCached = syncFeedbacks.contains(userId)
        val listenerRegistration = runCatching {
            userCollection
                .document(userId)
                .collection("feedback")
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

                    val feedbacks =
                        snapshot?.documents?.mapNotNull { UserFeedback(it) } ?: emptyList()
                    trySend(feedbacks).isSuccess
                }
        }.getOrNull()

        runCatching {
            userCollection
                .document(userId)
                .collection("feedback")
                .get(Source.CACHE)
                .await()
                .documents
                .mapNotNull { UserFeedback(it, userId) }
        }.onFailure { trySend(emptyList()) }

        if (isCached.not()) {
            runCatching {
                userCollection
                    .document(userId)
                    .collection("feedback")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { UserFeedback(it, userId) }
                    .also {
                        if (isCached.not()) {
                            syncFeedbacks.add(userId)
                        }
                    }
            }.onFailure { trySend(emptyList()) }
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    suspend fun addUserFeedback(
        userId: String,
        comment: String,
        rating: Double
    ) = apiQuery {
        val me  = userRepository.getMe().getOrNull()
        val myId = me?.uid?: error("User id is null")
        if (rating !in 1.0..5.0) error("Rating must be between 1.0 and 5.0")
        if (comment.isEmpty()) error("Comment must not be empty")

        val feedbacks = getUserFeedbacks(userId).getOrNull()?: emptyList()
        val myFeedback = feedbacks.find { it.fromUserId == myId }
        val avgRating = feedbacks.filterNot{
            it.fromUserId == myId
        }.map { it.rating }.plus(rating).average()


        val collection = userCollection.document(userId).collection("feedback")

        val feedbackData = mutableMapOf<String, Any>().apply {
            put(UserFeedback.Field.COMMENT.key, comment)
            put(UserFeedback.Field.RATING.key, rating)
            put(UserFeedback.Field.FROM_USER_ID.key, myId)
            me.phone?.let {
                put(UserFeedback.Field.FROM_USER_PHONE.key, it)
            }
            put(UserFeedback.Field.UPDATED_AT.key, FieldValue.serverTimestamp())
            if (myFeedback == null){
                put(UserFeedback.Field.CREATED_AT.key, FieldValue.serverTimestamp())
            }
        }

        collection.document(myFeedback?.id?: myId).set(feedbackData, SetOptions.merge()).await()

        userRepository.updateUser(userId, InputField(User.Field.AVG_RATING, avgRating))
    }

    suspend fun removeUserFeedback(
        userId: String,
    ) = apiQuery {
        val me  = userRepository.getMe().getOrNull()
        val myId = me?.uid?: error("User id is null")

        val feedbacks = getUserFeedbacks(userId).getOrNull()?: emptyList()
        val myFeedback = feedbacks.find { it.fromUserId == myId }
        val avgRating = feedbacks.filterNot{
            it.fromUserId == myId
        }.map { it.rating }.average()


        val collection = userCollection.document(userId).collection("feedback")

        collection.document(myFeedback?.id?: myId).delete().await()

        userRepository.updateUser(userId, InputField(User.Field.AVG_RATING, avgRating))
    }

}