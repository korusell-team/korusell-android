package net.alienminds.ethnogram.service.user

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.SnapshotListenOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.base.BaseRepository
import net.alienminds.ethnogram.service.base.entities.Field
import net.alienminds.ethnogram.service.base.entities.InputField
import net.alienminds.ethnogram.service.user.entities.User
import java.util.UUID

class UserRepository internal constructor(
    private val authRepository: AuthRepository,
    private val ioScope: CoroutineScope,
    private val storage: FirebaseStorage = Firebase.storage,
    firestore: FirebaseFirestore = Firebase.firestore,
): BaseRepository() {

    private val collection = firestore.collection("users")

    private val _meFlow = MutableStateFlow<User?>(null)
    val meFlow: StateFlow<User?> get() = _meFlow

    private val _publicUsersFlow = MutableStateFlow<List<User>?>(null)
    val publicUsersFlow: StateFlow<List<User>?> get() = _publicUsersFlow



    init { listenCache() }

    suspend fun getMe() = apiQuery{
        val currentUser = authRepository.currentUser
            ?: throw IllegalStateException("User is not signed in")

        val phone = currentUser.phoneNumber
            ?: throw IllegalStateException("User phone is null")

        meFlow.value?.takeIf { it.phone == phone }?.let { return@apiQuery it }
        publicUsersFlow.value?.find { it.phone == phone }?.let { return@apiQuery it }

        collection
            .whereEqualTo(User.Field.PHONE.key, phone)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.let(::User)
            ?: throw NoSuchElementException("User not found in Firestore")
    }


    fun getUserFlow(userId: String): Flow<User?> = callbackFlow {
        launch {
            val initial = getUser(userId).getOrNull()
            trySend(initial).isSuccess
        }

        val listenerRegistration = collection
            .whereEqualTo(User.Field.UID.key, userId)
            .limit(1)
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

                val user = snapshot?.documents?.firstOrNull()?.let(::User)
                trySend(user).isSuccess
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    suspend fun getUser(
        uid: String
    ) = apiQuery{
        publicUsersFlow.value?.find { it.uid == uid }?.let { return@apiQuery it }
        meFlow.value?.takeIf { it.uid == uid }?.let { return@apiQuery it }

        collection
            .whereEqualTo(User.Field.UID.key, uid)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.let(::User)
            ?: throw NoSuchElementException("User not found in Firestore")
    }



    suspend fun getPublicUsers() = apiQuery {
        publicUsersFlow.value?.let {
            return@apiQuery it.sortedWith(
                compareByDescending<User> { it.priority ?: 0L }
                    .thenByDescending { it.likes.size }
            )
        }
        collection
            .onlyPublic()
            .get()
            .await()
            .documents
            .mapNotNull(::User)
            .sortedWith(
                compareByDescending<User> { it.priority ?: 0L }
                    .thenByDescending { it.likes.size }
            )
    }

    suspend fun updateMe(
        values: List<InputField<Any>>
    ) = updateMe(
        values = values.toTypedArray()
    )

    suspend fun updateMe(
        vararg values: InputField<Any>
    ) = apiQuery {
        val me = getMe().getOrNull()
        val meId = getMyId(me)
        val isNotCreated = me?.phone.isNullOrEmpty() || me.created == null

        val map = values.associate { Pair(it.field.key, it.value) }.toMutableMap()
        map[User.Field.UID.key] = meId
        map[User.Field.UPDATED.key] = FieldValue.serverTimestamp()
        if (isNotCreated){
            map[User.Field.PHONE.key] = authRepository.currentUser?.phoneNumber?: throw IllegalStateException("User is not signed in")
            map[User.Field.CREATED.key] = FieldValue.serverTimestamp()
        }

        val task = collection
            .document(meId)
            .set(map, SetOptions.merge())
        task.await()
        task.isSuccessful
    }

    suspend fun uploadPhoto(uri: Uri) = apiQuery{
        val meId = getMyId()
        val extension = uri.lastPathSegment?.substringAfterLast('.') ?: "jpg"
        val reference = storage.reference.child("avatars/$meId/${UUID.randomUUID()}.$extension")
        val uploadTask = reference.putFile(uri)
        uploadTask.continueWithTask { task ->
            if (task.isSuccessful.not()) {
                task.exception?.let { throw it }
            }
            reference.downloadUrl
        }.await()
    }

    suspend fun removeImage(
        url: String
    ) = apiQuery{
        val reference = storage.getReferenceFromUrl(url)
        val task = reference.delete()
        task.await()
        task.isSuccessful
    }

    suspend fun favoriteUser(
        userId: String,
        isFavorite: Boolean
    ) = apiQuery{
        val meId = getMyId()
        when(isFavorite){
            true -> addToArray(userId, User.Field.LIKES, meId)
            false -> removeFromArray(userId, User.Field.LIKES, meId)
        }
    }

    suspend fun blockUser(
        userId: String
    ) = apiQuery{
        addToArray(userId, User.Field.BLOCKED, getMyId())
    }

    suspend fun reportUser(
        userId: String
    ) = apiQuery{
        addToArray(userId, User.Field.REPORTS, getMyId())
    }

    suspend fun getMyId(
        me: User? = null
    ) = (me?: getMe().getOrNull())?.uid?: authRepository.currentUser?.uid?: throw IllegalStateException("User is not signed in")

    private suspend fun <T>addToArray(
        userId: String,
        field: Field<List<T>>,
        value: T
    ): Boolean {
        val updateMap = mapOf(
            field.key to FieldValue.arrayUnion(value)
        )
        val task = collection
            .document(userId)
            .set(updateMap, SetOptions.merge())

        task.await()
        return task.isSuccessful
    }

    private suspend fun <T>removeFromArray(
        userId: String,
        field: Field<List<T>>,
        value: T
    ): Boolean {
        val updateMap = mapOf(
            field.key to FieldValue.arrayRemove(value)
        )
        val task = collection
            .document(userId)
            .set(updateMap, SetOptions.merge())

        task.await()
        return task.isSuccessful
    }


    private fun listenCache(){
        ioScope.launch {
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
                    val users = snapshot.documents.mapNotNull(::User)
                    _publicUsersFlow.value = users
                        .filter { it.isPublic == true }
                        .sortedWith(
                            compareByDescending<User> { it.priority ?: 0L }
                                .thenByDescending { it.likes.size }
                        )
                    authRepository.currentUser?.phoneNumber?.let { phone ->
                        _meFlow.value = users.find { it.phone == phone }
                    }

                }
            }
        }
    }

    private fun Query.onlyPublic() =
        whereEqualTo(User.Field.IS_PUBLIC.key, true)



}