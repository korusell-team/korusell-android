package net.alienminds.ethnogram.service.users

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import net.alienminds.ethnogram.service.base.entities.InputField
import net.alienminds.ethnogram.service.base.entities.ServiceResult
import net.alienminds.ethnogram.service.users.entities.User

interface UsersService {

    companion object{
        const val DEFAULT_LIMIT = 10L
    }

    val me: Flow<User?>
    val allUsers: Flow<List<User>>
    val publicUsers: Flow<List<User>>

//    suspend fun loadMore(
//        limit: Long = DEFAULT_LIMIT,
//        isPublic: Boolean? = null
//    ): ServiceResult<Boolean>

    fun getUser(userId: String): Flow<User?>
    suspend fun updateUser(values: List<InputField<Any>>): ServiceResult<Boolean>
    suspend fun updateUser(vararg values: InputField<Any>): ServiceResult<Boolean>
    suspend fun uploadImage(uri: Uri): ServiceResult<Uri>
    suspend fun removeImage(url: String): ServiceResult<Void>
    suspend fun favoriteUser(userId: String, isFavorite: Boolean): ServiceResult<Boolean>
    suspend fun blockUser(userId: String): ServiceResult<Boolean>
    suspend fun reportUser(userId: String): ServiceResult<Boolean>
    suspend fun reloadFromServer(): ServiceResult<Boolean>
}