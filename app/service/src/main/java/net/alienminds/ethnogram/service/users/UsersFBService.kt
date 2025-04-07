package net.alienminds.ethnogram.service.users

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.SnapshotListenOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.service.base.BaseService
import net.alienminds.ethnogram.service.base.entities.DBObject
import net.alienminds.ethnogram.service.base.entities.InputField
import net.alienminds.ethnogram.service.base.entities.UserNotAuthorized
import net.alienminds.ethnogram.service.users.entities.User
import java.util.UUID
import kotlin.time.Duration.Companion.seconds


class UsersFBService internal constructor(): BaseService(), UsersService {

    companion object{
        private const val COLLECTION_NAME = "users"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val currentUser
        get() = Firebase.auth.currentUser

    private val collection
        get() = Firebase.firestore.collection(COLLECTION_NAME)

    private val storage
        get() = Firebase.storage

    private val snapshots = MutableStateFlow<QuerySnapshot?>(null)

    override val allUsers by lazy {
        snapshots.map { snapshot ->
            snapshot
                ?.map { User(it) }
                ?.sortedByDescending { it.likes.size }
                ?.sortedByDescending { it.priority }
                ?: emptyList()
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    }

    override val publicUsers by lazy {
        allUsers.map { users ->
            users.filter { it.isPublic }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    }

    override val me by lazy {
        allUsers.map { users ->
            users.find {
                currentUser?.phoneNumber.isNullOrEmpty().not() &&
                it.phone == currentUser?.phoneNumber
            }
        }.stateIn(scope, SharingStarted.Eagerly, null)
    }

    private val meId: String
        get() = getMyId()




    init { listenCache() }

    override fun getUser(userId: String) = allUsers.map { users ->
        users.find { it.uid == userId }
    }

    override suspend fun updateUser(
        values: List<InputField<Any>>
    ) = updateUser(
        values = values.toTypedArray()
    )

    @OptIn(FlowPreview::class)
    override suspend fun updateUser(
       vararg values: InputField<Any>
    ) = withSave<Boolean>{

        val isCreated = runCatching {
            me.timeout(5.seconds).first {
                it?.uid != null
            }?.run {
                phone.isNullOrEmpty().not() &&
                        created != null
            }
        }.getOrNull() == true

        val map = values.associate { Pair(it.field.key, it.value) }.toMutableMap()
        map[User.Fields.UID.key] = meId
        map[User.Fields.UPDATED.key] = FieldValue.serverTimestamp()
        if (isCreated.not()){
            map[User.Fields.PHONE.key] = currentUser?.phoneNumber?: throw IllegalStateException("Auth Phone is not available")
            map[User.Fields.CREATED.key] = FieldValue.serverTimestamp()
        }

        val task = collection
            .document(meId)
            .set(map, SetOptions.merge())
        task.await()
        task.isSuccessful
    }

    override suspend fun uploadImage(
        uri: Uri
    ) = withSave{

        val reference = storage.reference.child("avatars/$meId/${UUID.randomUUID()}.jpg")
        val uploadTask = reference.putFile(uri)
        uploadTask.continueWithTask { task ->
            if (task.isSuccessful.not()) {
                task.exception?.let { throw it }
            }
            reference.downloadUrl
        }.await()
    }

    override suspend fun removeImage(
        url: String
    ) = withSave{
        val reference = storage.getReferenceFromUrl(url)
        reference.delete().await()
    }

    override suspend fun favoriteUser(
        userId: String,
        isFavorite: Boolean
    ) = withSave{
        when(isFavorite){
            true -> addToArray(userId, User.Fields.LIKES, meId)
            false -> removeFromArray(userId, User.Fields.LIKES, meId)
        }
    }

    override suspend fun blockUser(
        userId: String
    ) = withSave{
        addToArray(userId, User.Fields.BLOCKED, meId)
    }

    override suspend fun reportUser(
        userId: String
    ) = withSave{

        addToArray(userId, User.Fields.REPORTS, meId)
    }

    private fun getMyId() =
        me.value?.uid
            ?: currentUser?.uid
            ?: throw UserNotAuthorized()

    private suspend fun <T>addToArray(
        userId: String,
        field: DBObject.Field<List<T>>,
        value: T
    ): Boolean {
        val task = collection.document(userId).set(mapOf(
            field.key to FieldValue.arrayUnion(value)
        ), SetOptions.merge())
        task.await()
        return task.isSuccessful
    }

    private suspend fun <T>removeFromArray(
        userId: String,
        field: DBObject.Field<List<T>>,
        value: T
    ): Boolean {
        val task = collection.document(userId).set(mapOf(
            field.key to FieldValue.arrayRemove(value)
        ), SetOptions.merge())
        task.await()
        return task.isSuccessful
    }


    private fun listenCache(){
        scope.launch {
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

//    override suspend fun loadMore(
//        limit: Long,
//        isPublic: Boolean?
//    ) = withSave{
//        val lastSnapshot = snapshots.lastOrNull()
//
//        var query = collection
////            .orderBy(User.Fields.UID.key)
////            .orderBy(User.Fields.LIKES.key, Query.Direction.DESCENDING)
//            .startAfter(lastSnapshot)
//            .limit(limit)
//
////        isPublic?.let { public ->
////            query = query.where(Filter.equalTo(User.Fields.IS_PUBLIC.key, public))
////        }
//
////        query.get().await().isEmpty.not()
//        false
//    }


}