package net.alienminds.ethnogram.service.user

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.SnapshotListenOptions
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.base.BaseRepository
import net.alienminds.ethnogram.service.base.entities.Field
import net.alienminds.ethnogram.service.base.entities.InputField
import net.alienminds.ethnogram.service.feed.entities.Author
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.service.utils.FirestoreProvider
import java.util.UUID
import kotlin.collections.associate

class UserRepository internal constructor(
    private val authRepository: AuthRepository,
    private val ioScope: CoroutineScope,
    private val storage: FirebaseStorage = Firebase.storage,
    private val firestoreProvider: FirestoreProvider,
): BaseRepository() {

    private val collection
        get() = firestoreProvider.get().collection("users")
    
    private var isSyncMe = false
    private var isSyncPublic = false
    
    private val syncedUsers = mutableSetOf<String>() // list of user uids
    
    init {
        activateUser()
        authRepository.logoutFlow.onEach { 
            isSyncMe = false
            isSyncPublic = false
            syncedUsers.clear()
        }.launchIn(ioScope)
    }

    private fun activateUser() = ioScope.launch{
        apiQuery {
            val me = getMe().getOrNull()
            val meId = getMyId(me)
            val isNotCreated = me?.phone.isNullOrEmpty() || me.created == null

            val map = mutableMapOf<String, Any>()
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
        }.onFailure {
            it.printStackTrace()
        }
    }


    suspend fun getMe() = apiQuery{
        val currentUser = authRepository.currentUser 
            ?: throw IllegalStateException("User is not signed in")

        val phone = currentUser.phoneNumber
            ?: throw IllegalStateException("User phone is null")

        collection
            .whereEqualTo(User.Field.PHONE.key, phone)
            .limit(1)
            .get(when(isSyncMe) {
                true -> Source.CACHE
                false -> Source.DEFAULT
            })
            .await().documents.firstOrNull()
            ?.let(::User)?.also { 
                isSyncMe = true
            }?: throw NoSuchElementException("User not found in Firestore")
    }
    
    val meFlow get() = callbackFlow{
        closeIfLogout()
        val user = getMe().getOrNull()?.also {
            trySend(it)
        }

        val listenerRegistration = user?.uid?.let { userId ->
            runCatching {
                collection
                    .document(userId)
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
                        runCatching {
                            val updatedUser = snapshot?.let(::User)
                            if (updatedUser != null) {
                                trySend(updatedUser).isSuccess
                            }
                        }
                    }
            }.getOrNull()
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    private suspend fun getUser(userId: String) = apiQuery{
        val isCached = syncedUsers.contains(userId)
        if (isCached.not() && isSyncPublic){
            runCatching {
                collection
                    .whereEqualTo(User.Field.UID.key, userId)
                    .limit(1)
                    .get(Source.CACHE).await().documents
                    .firstOrNull()?.let(::User)?.also {
                        if (it.isPublic == true) {
                            it.uid?.let(syncedUsers::add)
                        }
                        return@apiQuery it
                    }
            }
        }
        collection
            .whereEqualTo(User.Field.UID.key, userId)
            .limit(1)
            .get(
                when (isCached) {
                    true -> Source.CACHE
                    false -> Source.DEFAULT
                }
            )
            .await().documents.firstOrNull()
            ?.let(::User)?.also {
                it.uid?.let(syncedUsers::add)
            } ?: throw NoSuchElementException("User not found in Firestore")
    }

    fun getUserFlow(userId: String) = callbackFlow{
        closeIfLogout()
        val user = getUser(userId).getOrNull()?.also {
            trySend(it)
        }

        val listenerRegistration = user?.uid?.let { userId ->
            runCatching {
                collection
                    .document(userId)
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
                        val updatedUser = snapshot?.let(::User)
                        if (updatedUser != null) {
                            trySend(updatedUser).isSuccess
                        }
                    }
            }.getOrNull()
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }
    
    val publicUsersFlow get() = callbackFlow{
        closeIfLogout()
        val listenerRegistration = runCatching {
            collection
                .whereEqualTo(User.Field.IS_PUBLIC.key, true)
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
                    val users = snapshot?.documents?.mapNotNull(::User)?.sortedPriorityLikes()
                        ?: emptyList()
                    trySend(users).isSuccess
                }
        }.getOrNull()

        runCatching {
            collection
                .whereEqualTo(User.Field.IS_PUBLIC.key, true)
                .get(Source.CACHE).await().documents
                .mapNotNull(::User).sortedPriorityLikes()
                .also { trySend(it).isSuccess }
        }.onFailure { trySend(emptyList()) }

        if (isSyncPublic.not()){
            runCatching {
                collection
                    .whereEqualTo(User.Field.IS_PUBLIC.key, true)
                    .get().await().documents
                    .mapNotNull(::User).sortedPriorityLikes()
                    .also {
                        isSyncPublic = true
                        trySend(it).isSuccess
                    }
            }.onFailure { trySend(emptyList()) }
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    suspend fun getAuthor(
        authorId: String
    ) = apiQuery{
        getUser(authorId).getOrNull()?.let {
            Author(it)
        }?: throw NoSuchElementException("Author not found in Firestore")
    }

    suspend fun getAuthors(
        vararg authorIds: String
    ) = apiQuery{
        val found = mutableMapOf<String, Author>()
        
        val distinctAuthorIds = authorIds.distinct()
        val cachedAuthors = distinctAuthorIds.filter { syncedUsers.contains(it) }
        val notCachedAuthors = distinctAuthorIds.filterNot { syncedUsers.contains(it) }
        
        if (cachedAuthors.isNotEmpty()){
            val chunks = cachedAuthors.chunked(10) // Firestore limit for `whereIn`
            for (chunk in chunks) {
                collection
                    .whereIn(User.Field.UID.key, chunk)
                    .get(Source.CACHE).await().documents
                    .mapNotNull(::User)
                    .forEach { user ->
                        user.uid?.let {
                            found[it] = Author(user)
                        }
                    }
            }
        }

        if (notCachedAuthors.isNotEmpty()){
            val chunks = notCachedAuthors.chunked(10) // Firestore limit for `whereIn`
            for (chunk in chunks) {
                collection
                    .whereIn(User.Field.UID.key, chunk)
                    .get().await().documents
                    .mapNotNull(::User)
                    .forEach { user ->
                        user.uid?.let {
                            found[it] = Author(user)
                        }
                    }
            }
        }
        
        return@apiQuery found.toMap()
    }

    internal suspend fun updateUser(
        userId: String,
        vararg values: InputField<Any>
    ){
        val map = values.associate { Pair(it.field.key, it.value) }.toMutableMap()
        map[User.Field.UID.key] = userId
        map[User.Field.UPDATED.key] = FieldValue.serverTimestamp()

        val task = collection
            .document(userId)
            .set(map, SetOptions.merge())
        task.await()
        task.isSuccessful
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

    suspend fun deleteMyAccount() = apiQuery{
        val meId = getMyId()
        val task = collection
            .document(meId)
            .delete()
        task.await()
        task.isSuccessful
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
    
    val myIdFlow
        get() = channelFlow {
            val id = runCatching { getMyId() }.getOrNull()
            if (id != null) {
                trySend(id).isSuccess
            }
            awaitClose {  }
        }
    
    private fun ProducerScope<*>.closeIfLogout(){
        runCatching {
            authRepository.logoutFlow.onEach {
                this.close()
            }.launchIn(ioScope)
        }
    }

    internal suspend fun getMyId(
        me: User? = null
    ): String {
        me?.uid?.let { 
            return it
        }
        getMe().getOrNull()?.uid?.let {
            return it
        }
        authRepository.currentUser?.uid?.let {
            return it
        }
        throw IllegalStateException("User is not signed in")
    }

    private fun List<User>.sortedPriorityLikes() = sortedWith(
        compareByDescending<User> { it.priority ?: 0L }
            .thenByDescending { it.likes.size }
    )
    
}