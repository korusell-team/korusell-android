package net.alienminds.ethnogram.ui.screens.auth.profile_setup

import android.net.Uri
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import net.alienminds.ethnogram.service.API
import net.alienminds.ethnogram.service.base.entities.InputField
import net.alienminds.ethnogram.service.users.entities.User
import net.alienminds.ethnogram.utils.AppScreenModel

class ProfileSetupViewModel: AppScreenModel() {

    private val profile by API.users.me.toState(null)


    private val addedImages = mutableStateListOf<String>()
    private val removedImages = mutableStateListOf<String>()

    var name = mutableStateOf(profile?.name)
    var surname = mutableStateOf(profile?.surname)
    var bio = mutableStateOf(profile?.bio)

    val images by derivedStateOf {
        addedImages
            .plus(profile?.image.orEmpty())
            .filterNot { removedImages.contains(it) }
    }


    private val isErrorAvatar by derivedStateOf {images.isEmpty() }
    val isErrorName by derivedStateOf {  name.value.isNullOrEmpty() }
    val isErrorSurname by derivedStateOf {  surname.value.isNullOrEmpty() }

    fun onLoginSuccess() = withLoadingScope {
        val reloadResult = API.users.reloadFromServer()
    }

    val canSave by derivedStateOf {
        isErrorName.not() &&
        isErrorSurname.not() &&
        isErrorAvatar.not()
    }

    fun addImage(
        photos: List<Uri>,
    ) = addedImages.addAll(0, photos.map { it.toString() })


    fun saveUser(onSuccess: () -> Unit) = withLoadingScope {
        val fields = getEditedFields().run {
            if (addedImages.isNotEmpty() || removedImages.isNotEmpty()) {
                plus(InputField(User.Fields.IMAGE, applyImages()))
            } else this
        }

        val result = API.users.updateUser(values = fields)
        if (result.isSuccess) {
            onSuccess()
        }
    }

    private suspend fun applyImages(): List<String> {
        val newImages = profile?.image?.toMutableList()?: mutableListOf()
        removedImages.forEach {
            API.users.removeImage(it)
            newImages.remove(it)
        }
        addedImages.mapNotNull{
            API.users.uploadImage(it.toUri()).data?.toString()
        }.let { newImages.addAll(0, it) }
        removedImages.clear()
        addedImages.clear()
        return newImages
    }

    private fun getEditedFields(): List<InputField<Any>> = listOf(
        Pair(InputField(User.Fields.NAME, name.value.orEmpty()), profile?.name.orEmpty()),
        Pair(InputField(User.Fields.SURNAME, surname.value.orEmpty()), profile?.surname.orEmpty()),
        Pair(InputField(User.Fields.BIO, bio.value.orEmpty()), profile?.bio.orEmpty())).mapNotNull{ pair ->
        pair.first.takeUnless{ it.value == pair.second }
    }

}