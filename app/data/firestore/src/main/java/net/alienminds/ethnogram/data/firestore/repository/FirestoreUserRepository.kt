package net.alienminds.ethnogram.data.firestore.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import net.alienminds.ethnogram.data.firestore.executors.firestore.FirestoreQueryRequestExecutor
import net.alienminds.ethnogram.data.firestore.fields.UserFields
import net.alienminds.ethnogram.data.firestore.utils.FirestoreProvider
import net.alienminds.ethnogram.data.firestore.utils.applyPaging
import net.alienminds.ethnogram.data.firestore.utils.toPagingMeta
import net.alienminds.ethnogram.data.model.common.ID
import net.alienminds.ethnogram.data.model.common.PagingData
import net.alienminds.ethnogram.data.model.common.PagingInput
import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.model.core.QueryRequestExecutor
import net.alienminds.ethnogram.data.model.user.User
import net.alienminds.ethnogram.data.model.user.UserFilter
import net.alienminds.ethnogram.data.repository.AuthRepository
import net.alienminds.ethnogram.data.repository.UserRepository2
import java.util.Date
import kotlin.time.Duration.Companion.days

class FirestoreUserRepository(
    private val firestoreProvider: FirestoreProvider,
    private val authRepository: AuthRepository
): UserRepository2{

    private val usersRef
        get() = firestoreProvider.getFirestore()
            .collection("users")

    override fun getPublicNewUsers(
        paging: PagingInput
    ): QueryRequestExecutor<PagingData<User>> = FirestoreQueryRequestExecutor(
        resolveCall = {
            val fifteenDaysAgo = Date(System.currentTimeMillis() - 15.days.inWholeMilliseconds)
            usersRef
                .onlyPublic()
                .whereLessThan(UserFields.CREATED, fifteenDaysAgo)
                .orderBy(UserFields.CREATED, Query.Direction.DESCENDING)
                .applyPaging(paging)
        },
        mapper = { snapshot ->
            val docs = snapshot.documents
            val users = docs.mapNotNull { doc ->
                doc.toObject(User::class.java)
            }
            PagingData(
                items = users,
                meta = docs.toPagingMeta(paging.pageLimit)
            )
        },
    )

    override fun getPublicTopUsers(paging: PagingInput): QueryRequestExecutor<PagingData<User>> =
        FirestoreQueryRequestExecutor(
            resolveCall = {
                usersRef
                    .onlyPublic()
                    .whereGreaterThan(UserFields.SPONSORED_EXP_DATE, FieldValue.serverTimestamp())
                    .orderBy(UserFields.LIKES_COUNT, Query.Direction.DESCENDING)
                    .applyPaging(paging)
            },
            mapper = { snapshot ->
                val docs = snapshot.documents
                val users = docs.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }.sortedByDescending { it.likes.size }
                PagingData(
                    items = users,
                    meta = docs.toPagingMeta(paging.pageLimit)
                )
            },
        )


    override fun getPublicActiveUsers(paging: PagingInput): QueryRequestExecutor<PagingData<User>> =
        FirestoreQueryRequestExecutor(
            resolveCall = {
                usersRef
                    .onlyPublic()
                    .orderBy(UserFields.UPDATED, Query.Direction.DESCENDING)
                    .applyPaging(paging)
            },
            mapper = { snapshot ->
                val docs = snapshot.documents
                val users = docs.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }
                PagingData(
                    items = users,
                    meta = docs.toPagingMeta(paging.pageLimit)
                )
            },
        )

    override fun filterPublicUsers(
        paging: PagingInput,
        filter: UserFilter
    ): QueryRequestExecutor<PagingData<User>>{
        TODO("Not yet implemented")
    }

    override fun getMe(): QueryRequestExecutor<User> = FirestoreQueryRequestExecutor(
        resolveCall = {
            val identity = authRepository.getIdentity()
                .get(FetchMode.CacheFirst)
                .getOrNull()?: throw IllegalStateException("User not authorized")

            (identity.phoneNumber?.let {
                usersRef.whereEqualTo(UserFields.PHONE, it)
            }?: usersRef.whereEqualTo(UserFields.UID, identity.id))
                .limit(1)

        },
        mapper = {
            it.documents.firstNotNullOfOrNull { doc ->
                doc.toObject(User::class.java)
            }?: throw IllegalStateException("User not found")
        }
    )

    override fun getUser(id: ID): QueryRequestExecutor<User> = FirestoreQueryRequestExecutor(
        resolveCall = {
            usersRef.whereEqualTo(UserFields.UID, id).limit(1)
        },
        mapper = {
            it.documents.firstNotNullOfOrNull { doc ->
                doc.toObject(User::class.java)
            }?: throw IllegalStateException("User not found")
        }
    )


    private fun Query.onlyPublic(): Query =
        whereEqualTo(UserFields.IS_PUBLIC, true)


}