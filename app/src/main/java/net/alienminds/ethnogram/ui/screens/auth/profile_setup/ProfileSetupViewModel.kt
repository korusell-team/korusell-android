package net.alienminds.ethnogram.ui.screens.auth.profile_setup

import android.net.Uri
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import kotlinx.coroutines.flow.onEach
import net.alienminds.ethnogram.service.base.entities.InputField
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

class ProfileSetupViewModel: AppScreenModel() {

    private val userRepo by inject<UserRepository>()

    private val profile by userRepo.meFlow.onEach {
        name = it.name
        surname = it.surname
        bio = it.bio
    }.asState(null)

    private val addedImages = mutableStateListOf<String>()
    private val removedImages = mutableStateListOf<String>()

    var name by mutableStateOf<String?>(null)
    var surname by mutableStateOf<String?>(null)
    var bio by mutableStateOf<String?>(null)


    val images by derivedStateOf {
        addedImages
            .plus(profile?.image.orEmpty())
            .filterNot { removedImages.contains(it) }
    }


    private val isErrorAvatar by derivedStateOf {images.isEmpty() }
    private val isErrorName by derivedStateOf { name.isNullOrEmpty() }
    private val isErrorSurname by derivedStateOf { surname.isNullOrEmpty() }


    val canSave by derivedStateOf {
        isErrorName.not() &&
        isErrorSurname.not() &&
        isErrorAvatar.not()
    }

    fun addImage(
        photos: List<Uri>,
    ) = addedImages.addAll(0, photos.map { it.toString() })


    fun saveUser(onSuccess: () -> Unit) = launchWithLoading {
        val fields = getEditedFields().run {
            if (addedImages.isNotEmpty() || removedImages.isNotEmpty()) {
                plus(InputField(User.Field.IMAGE, applyImages()))
            } else this
        }
        val result = userRepo.updateMe(values = fields)
        if (result.isSuccess && result.getOrNull() == true) {
            onSuccess()
        }
    }

    private suspend fun applyImages(): List<String> {
        val newImages = profile?.image?.toMutableList()?: mutableListOf()
        removedImages.forEach {
            userRepo.removeImage(it)
            newImages.remove(it)
        }
        addedImages.mapNotNull{
            userRepo.uploadPhoto(it.toUri()).getOrNull()?.toString()
        }.let { newImages.addAll(0, it) }
        removedImages.clear()
        addedImages.clear()
        return newImages
    }

    private fun getEditedFields(): List<InputField<Any>> = listOf(
        Pair(InputField(User.Field.NAME, name.orEmpty()), profile?.name.orEmpty()),
        Pair(InputField(User.Field.SURNAME, surname.orEmpty()), profile?.surname.orEmpty()),
        Pair(InputField(User.Field.BIO, bio.orEmpty()), profile?.bio.orEmpty())).mapNotNull{ pair ->
        pair.first.takeUnless{ it.value == pair.second }
    }

}