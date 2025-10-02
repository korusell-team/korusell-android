//package net.alienminds.ethnogram.service.user
//
//import android.net.Uri
//import com.google.firebase.Firebase
//import com.google.firebase.firestore.FieldValue
//import com.google.firebase.firestore.ListenSource
//import com.google.firebase.firestore.ListenerRegistration
//import com.google.firebase.firestore.MetadataChanges
//import com.google.firebase.firestore.Query
//import com.google.firebase.firestore.SetOptions
//import com.google.firebase.firestore.SnapshotListenOptions
//import com.google.firebase.storage.FirebaseStorage
//import com.google.firebase.storage.storage
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.callbackFlow
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.onEach
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.sync.Mutex
//import kotlinx.coroutines.sync.withLock
//import kotlinx.coroutines.tasks.await
//import net.alienminds.ethnogram.service.auth.AuthRepository
//import net.alienminds.ethnogram.service.base.BaseRepository
//import net.alienminds.ethnogram.service.base.entities.Field
//import net.alienminds.ethnogram.service.base.entities.InputField
//import net.alienminds.ethnogram.service.feed.entities.Author
//import net.alienminds.ethnogram.service.user.entities.User
//import net.alienminds.ethnogram.service.utils.FirestoreProvider
//import net.alienminds.ethnogram.service.utils.addCacheListener
//import java.util.UUID
//import kotlin.Any
//
//class UserRepositoryOld internal constructor(
//    private val authRepository: AuthRepository,
//    private val ioScope: CoroutineScope,
//    private val storage: FirebaseStorage = Firebase.storage,
//    private val firestoreProvider: FirestoreProvider,
//): BaseRepository() {
//
//    private val collection
//        get() = firestoreProvider.get().collection("users")
//
//    private val listenerMutex = Mutex()
//    private var listenerRegistration: ListenerRegistration? = null
//
//    @Volatile
//    private var isSyncMe = false
//
//    @Volatile
//    private var isSyncPublic = false
//
//    @Volatile
//    private var syncUsers = mutableListOf<String>()//list uid
//
//    private val _meFlow = MutableStateFlow<User?>(null)
//    val meFlow: StateFlow<User?> get() = _meFlow
//
//    private val _publicUsersFlow = MutableStateFlow<List<User>?>(null)
//    val publicUsersFlow: StateFlow<List<User>?> get() = _publicUsersFlow
//
//    private val _usersFlow = MutableStateFlow<List<User>?>(null)
//    val usersFlow: StateFlow<List<User>?> get() = _usersFlow
//
//
//    init { waitLogout() }
//
//    suspend fun getMe() = apiQuery{
//        ensureListening()
//        val currentUser = authRepository.currentUser
//            ?: throw IllegalStateException("User is not signed in")
//
//        val phone = currentUser.phoneNumber
//            ?: throw IllegalStateException("User phone is null")
//
//        if (isSyncMe) {
//            meFlow.value?.takeIf { it.phone == phone }?.let { return@apiQuery it }
//            publicUsersFlow.value?.find { it.phone == phone }?.let { return@apiQuery it }
//        }
//
//        val result = collection
//            .whereEqualTo(User.Field.PHONE.key, phone)
//            .limit(1)
//            .get()
//            .await()
//            .documents
//            .firstOrNull()
//            ?.let(::User)
//            ?: throw NoSuchElementException("User not found in Firestore")
//        isSyncMe = true
//        return@apiQuery result
//    }
//
//
//    fun getUserFlow(userId: String): Flow<User?> = callbackFlow {
//        launch {
//            val initial = getUser(userId).getOrNull()
//            trySend(initial).isSuccess
//        }
//
//        val listenerRegistration = collection
//            .whereEqualTo(User.Field.UID.key, userId)
//            .limit(1)
//            .addSnapshotListener(
//                SnapshotListenOptions.Builder()
//                    .setMetadataChanges(MetadataChanges.INCLUDE)
//                    .setSource(ListenSource.CACHE)
//                    .build()
//            ) { snapshot, error ->
//                if (error != null) {
//                    close(error)
//                    return@addSnapshotListener
//                }
//
//                val user = snapshot?.documents?.firstOrNull()?.let(::User)
//                trySend(user).isSuccess
//            }
//
//        awaitClose {
//            listenerRegistration.remove()
//        }
//    }
//
//    suspend fun getUser(
//        uid: String
//    ) = apiQuery{
//        ensureListening()
//        if (isSyncPublic) {
//            publicUsersFlow.value?.find { it.uid == uid }?.let { return@apiQuery it }
//        }
//        if (isSyncMe) {
//            meFlow.value?.takeIf { it.uid == uid }?.let { return@apiQuery it }
//        }
//        if (syncUsers.contains(uid)){
//            usersFlow.value?.find { it.uid == uid }?.let { return@apiQuery it }
//        }
//        collection
//            .whereEqualTo(User.Field.UID.key, uid)
//            .limit(1)
//            .get()
//            .await()
//            .documents
//            .firstOrNull()
//            ?.let(::User)
//            ?.also{ it.uid?.let(syncUsers::add) }
//            ?: throw NoSuchElementException("User not found in Firestore")
//    }
//
//    suspend fun getAuthor(
//        authorId: String
//    ) = apiQuery{
//        getUser(authorId).getOrNull()?.let {
//            Author(it)
//        }?: throw NoSuchElementException("Author not found in Firestore")
//    }
//
//    suspend fun getAuthors(
//        vararg authorIds: String
//    ) = apiQuery{
//        val found = mutableMapOf<String, Author>()
//
//        for (aid in authorIds) {
//            if (isSyncMe) {
//                meFlow.value?.takeIf { it.uid == aid }?.let {
//                    found[aid] = Author(it)
//                }
//            }
//            if (isSyncPublic) {
//                publicUsersFlow.value?.find { it.uid == aid }?.let {
//                    found[aid] = Author(it)
//                }
//            }
//            if (syncUsers.contains(aid)) {
//                usersFlow.value?.find { it.uid == aid }?.let {
//                    found[aid] = Author(it)
//                }
//            }
//        }
//        val toFetch = authorIds.filterNot { found.containsKey(it) }
//
//        if (toFetch.isNotEmpty()) {
//            val chunks = toFetch.chunked(10) // Firestore limit for `whereIn`
//            for (chunk in chunks) {
//                val snapshot = collection
//                    .whereIn(User.Field.UID.key, chunk)
//                    .get()
//                    .await()
//
//                snapshot.documents
//                    .mapNotNull(::User)
//                    .forEach { user ->
//                        user.uid?.let {
//                            syncUsers.add(it)
//                            found[it] = Author(user)
//                        }
//                    }
//            }
//        }
//        return@apiQuery found.toMap()
//    }
//
//    suspend fun getPublicUsers() = apiQuery {
//        ensureListening()
//        if (isSyncPublic) {
//            publicUsersFlow.value?.let {
//                return@apiQuery it.sortedPriorityLikes()
//            }
//        }
//        val result = collection
//            .onlyPublic()
//            .get()
//            .await()
//            .documents
//            .mapNotNull(::User)
//            .sortedPriorityLikes()
//        isSyncPublic = true
//        return@apiQuery result
//    }
//
//    internal suspend fun updateUser(
//        userId: String,
//        values: List<InputField<Any>>
//    ) = updateUser(
//        userId = userId,
//        values = values.toTypedArray()
//    )
//
//    internal suspend fun updateUser(
//        userId: String,
//        vararg values: InputField<Any>
//    ){
//        val map = values.associate { Pair(it.field.key, it.value) }.toMutableMap()
//        map[User.Field.UID.key] = userId
//        map[User.Field.UPDATED.key] = FieldValue.serverTimestamp()
//
//        val task = collection
//            .document(userId)
//            .set(map, SetOptions.merge())
//        task.await()
//        task.isSuccessful
//    }
//
//    suspend fun updateMe(
//        values: List<InputField<Any>>
//    ) = updateMe(
//        values = values.toTypedArray()
//    )
//
//    suspend fun updateMe(
//        vararg values: InputField<Any>
//    ) = apiQuery {
//        val me = getMe().getOrNull()
//        val meId = getMyId(me)
//        val isNotCreated = me?.phone.isNullOrEmpty() || me.created == null
//
//        val map = values.associate { Pair(it.field.key, it.value) }.toMutableMap()
//        map[User.Field.UID.key] = meId
//        map[User.Field.UPDATED.key] = FieldValue.serverTimestamp()
//        if (isNotCreated){
//            map[User.Field.PHONE.key] = authRepository.currentUser?.phoneNumber?: throw IllegalStateException("User is not signed in")
//            map[User.Field.CREATED.key] = FieldValue.serverTimestamp()
//        }
//
//        val task = collection
//            .document(meId)
//            .set(map, SetOptions.merge())
//        task.await()
//        task.isSuccessful
//    }
//
//    suspend fun uploadPhoto(uri: Uri) = apiQuery{
//        val meId = getMyId()
//        val extension = uri.lastPathSegment?.substringAfterLast('.') ?: "jpg"
//        val reference = storage.reference.child("avatars/$meId/${UUID.randomUUID()}.$extension")
//        val uploadTask = reference.putFile(uri)
//        uploadTask.continueWithTask { task ->
//            if (task.isSuccessful.not()) {
//                task.exception?.let { throw it }
//            }
//            reference.downloadUrl
//        }.await()
//    }
//
//    suspend fun removeImage(
//        url: String
//    ) = apiQuery{
//        val reference = storage.getReferenceFromUrl(url)
//        val task = reference.delete()
//        task.await()
//        task.isSuccessful
//    }
//
//    suspend fun favoriteUser(
//        userId: String,
//        isFavorite: Boolean
//    ) = apiQuery{
//        val meId = getMyId()
//        when(isFavorite){
//            true -> addToArray(userId, User.Field.LIKES, meId)
//            false -> removeFromArray(userId, User.Field.LIKES, meId)
//        }
//    }
//
//    suspend fun blockUser(
//        userId: String
//    ) = apiQuery{
//        addToArray(userId, User.Field.BLOCKED, getMyId())
//    }
//
//    suspend fun reportUser(
//        userId: String
//    ) = apiQuery{
//        addToArray(userId, User.Field.REPORTS, getMyId())
//    }
//
//    suspend fun getMyId(
//        me: User? = null
//    ) = (me?: getMe().getOrNull())?.uid?: authRepository.currentUser?.uid?: throw IllegalStateException("User is not signed in")
//
//    private suspend fun <T>addToArray(
//        userId: String,
//        field: Field<List<T>>,
//        value: T
//    ): Boolean {
//        val updateMap = mapOf(
//            field.key to FieldValue.arrayUnion(value)
//        )
//        val task = collection
//            .document(userId)
//            .set(updateMap, SetOptions.merge())
//
//        task.await()
//        return task.isSuccessful
//    }
//
//    private suspend fun <T>removeFromArray(
//        userId: String,
//        field: Field<List<T>>,
//        value: T
//    ): Boolean {
//        val updateMap = mapOf(
//            field.key to FieldValue.arrayRemove(value)
//        )
//        val task = collection
//            .document(userId)
//            .set(updateMap, SetOptions.merge())
//
//        task.await()
//        return task.isSuccessful
//    }
//
//
//    private fun waitLogout(){
//        authRepository.logoutFlow.onEach {
//            stopListening()
//            clearCache()
//        }.launchIn(ioScope)
//    }
//
//    private fun clearCache(){
//        _meFlow.value = null
//        _publicUsersFlow.value = null
//        isSyncMe = false
//        isSyncPublic = false
//    }
//
//    private suspend fun stopListening() {
//        listenerMutex.withLock {
//            listenerRegistration?.remove()
//            listenerRegistration = null
//        }
//    }
//
//    private suspend fun ensureListening() {
//        listenerMutex.withLock {
//            if (listenerRegistration == null) {
//                listenCache()
//            }
//        }
//    }
//
//    private fun listenCache(){
//        val registration = collection.addCacheListener { snapshot, error ->
//            if (snapshot != null && snapshot.isEmpty.not()) {
//                val users = snapshot.documents.mapNotNull(::User).sortedPriorityLikes()
//                val public = users.filter { it.isPublic == true }
//                val nonPublic = users.filterNot { it.isPublic == true }
//
//                _publicUsersFlow.value = public
//                _usersFlow.value = nonPublic
//
//                authRepository.currentUser?.phoneNumber
//                    ?.takeIf { it.isNotEmpty() }
//                    ?.let { phone ->
//                        _meFlow.value = users.find { it.phone == phone }
//                    }
//            }
//        }
//        ioScope.launch {
//            listenerMutex.withLock {
//                listenerRegistration = registration
//            }
//        }
//    }
//
//    private fun Query.onlyPublic() =
//        whereEqualTo(User.Field.IS_PUBLIC.key, true)
//
//    private fun List<User>.sortedPriorityLikes() = sortedWith(
//        compareByDescending<User> { it.priority ?: 0L }
//            .thenByDescending { it.likes.size }
//    )
//
//
//}