package net.alienminds.ethnogram.ui.screens.session.contacts.edit_profile

import android.net.Uri
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.mappers.field
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.base.entities.InputField
import net.alienminds.ethnogram.service.data.DataRepository
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.data.entities.City
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.service.user.entities.UserSocialType
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

class EditProfileViewModel: AppScreenModel() {

    private val authRepo by inject<AuthRepository>()
    private val userRepo by inject<UserRepository>()
    private val dataRepo by inject<DataRepository>()

    private val authProfile
        get() = authRepo.currentUser

    private var profile by mutableStateOf<User?>(null)

    private val cityIds = mutableStateListOf<Long>()
    private val categoryIds = mutableStateListOf<Long>()

    private val addedImages = mutableStateListOf<String>()
    private val removedImages = mutableStateListOf<String>()

    private var allCategories by mutableStateOf<List<Category>>(emptyList())
    var allCities by mutableStateOf<List<City>>(emptyList())

    var isPublic by mutableStateOf(false)
    var isAvailablePhone by mutableStateOf(false)
    val phone by derivedStateOf { profile?.phone?: authProfile?.phoneNumber.orEmpty() }
    var name by mutableStateOf("")
    var surname by mutableStateOf("")
    var bio by mutableStateOf("")
    var info by mutableStateOf("")

    val linksMap = mutableStateMapOf<UserSocialType, String?>()

    val cities by derivedStateOf { allCities.filter { ac ->
        cityIds.any { ac.id == it }
    } }
    val categories by derivedStateOf { allCategories.filter { ac ->
        categoryIds.any { ac.id == it }
    } }

    val images by derivedStateOf {
        addedImages
            .plus(profile?.image.orEmpty())
            .filterNot { removedImages.contains(it) }
    }


    private val isErrorAvatar by derivedStateOf { isPublic && images.isEmpty() }
    val isErrorName by derivedStateOf { isPublic && name.isEmpty() }
    val isErrorSurname by derivedStateOf { isPublic && surname.isEmpty() }
    val isErrorCategory by derivedStateOf { isPublic && categoryIds.isEmpty() }

    val edited by derivedStateOf {
        addedImages.isNotEmpty() ||
        removedImages.isNotEmpty() ||
        isPublic != (profile?.isPublic == true) ||
        isAvailablePhone != (profile?.phoneIsAvailable == true) ||
        name != profile?.name ||
        surname != profile?.surname ||
        bio != profile?.bio ||
        info != profile?.info ||
        linksMap.any { it.value.orEmpty() != profile?.social?.socialMap?.get(it.key).orEmpty() } ||
        cityIds.compareIds(profile?.cities.orEmpty()).not() ||
        categoryIds.compareIds(profile?.categories.orEmpty()).not()
    }

    private fun List<Long>.compareIds(
        list: List<Long>,
    ) = size == list.size && all { a ->
        list.any { it == a }
    }

    val canSave by derivedStateOf {
        edited &&
        isErrorName.not() &&
        isErrorSurname.not() &&
        isErrorCategory.not() &&
        isErrorAvatar.not()
    }

    val allCategoriesGrouped by derivedStateOf {
        allCategories
            .groupBy { it.parentId }
            .filter { it.key != 0L }
            .mapNotNull { item ->
                allCategories.find {
                    it.id == item.key
                }?.let {
                    it to item.value
                }
            }.toMap()
    }

    init {
        screenModelScope.launch {
            launchWithLoading {
                loadData()
                loadProfile()
            }.join()
            observeProfile()
        }
    }

    private suspend fun observeProfile(){
        userRepo.meFlow.collect {
            updateField(it)
        }
    }

    private suspend fun loadProfile(){
        val me = userRepo.getMe().getOrNull()
        updateField(me)
    }

    private fun updateField(user: User?) {
        profile = user
        isPublic = user?.isPublic == true
        isAvailablePhone = user?.phoneIsAvailable == true
        name = user?.name.orEmpty()
        surname = user?.surname.orEmpty()
        bio = user?.bio.orEmpty()
        info = user?.info.orEmpty()

        linksMap.clear()
        linksMap.putAll(profile?.social?.socialMap.orEmpty())

        cityIds.clear()
        cityIds.addAll(user?.cities.orEmpty())

        categoryIds.clear()
        categoryIds.addAll(user?.categories.orEmpty())
    }

    private suspend fun loadData(){
        allCategories = dataRepo.getCategories().getOrNull().orEmpty()
        allCities = dataRepo.getCities().getOrNull().orEmpty()
    }

    fun addImage(
        photos: List<Uri>,
    ) = addedImages.addAll(0, photos.map { it.toString() })

    fun removeImage(
        photo: String? = images.firstOrNull(),
    ) = photo?.let {
        when(addedImages.contains(it)){
            true -> addedImages.remove(it)
            false -> removedImages.add(it)
        }
    }

    fun selectCategory(category: Category){
        val id = categoryIds.find { it == category.id }
        when(categoryIds.contains(id)){
            true -> {
                categoryIds.remove(id)
            }
            false -> categoryIds.add(category.id)
        }
    }

    fun selectCity(city: City){
        when(city.id == 0L){
            true -> when(cityIds.contains(0)){
                true -> cityIds.clear()
                false -> cityIds.run {
                    clear()
                    add(city.id)
                }
            }
            false -> {
                cityIds.remove(0)
                when (cityIds.contains(city.id)) {
                    true -> cityIds.remove(city.id)
                    false -> cityIds.add(city.id)
                }
            }
        }
    }

    fun saveUser() = launchWithLoading{
        val fields = getEditedFields().run {
            when(addedImages.isNotEmpty() || removedImages.isNotEmpty()){
                true -> plus(InputField(User.Field.IMAGE, applyImages()))
                false -> this
            }
        }
        userRepo.updateMe(
            values = fields
        )
    }



    private suspend fun applyImages(): List<String> {
        val newImages = profile?.image?.toMutableList()?: mutableListOf()
        removedImages.forEach {
            userRepo.removeImage(it)
            newImages.remove(it)
        }
        addedImages.mapNotNull{
            userRepo
                .uploadPhoto(it.toUri())
                .getOrNull()?.toString()
        }.let { newImages.addAll(0, it) }
        removedImages.clear()
        addedImages.clear()
        return newImages
    }

    private fun getEditedFields(): List<InputField<Any>> = listOf(
        Pair(InputField(User.Field.IS_PUBLIC, isPublic), profile?.isPublic == true),
        Pair(InputField(User.Field.PHONE_IS_AVAILABLE, isAvailablePhone),
            profile?.phoneIsAvailable == true
        ),
        Pair(InputField(User.Field.NAME, name), profile?.name.orEmpty()),
        Pair(InputField(User.Field.SURNAME, surname), profile?.surname.orEmpty()),
        Pair(InputField(User.Field.BIO, bio), profile?.bio.orEmpty()),
        Pair(InputField(User.Field.INFO, info), profile?.info.orEmpty()),
        Pair(InputField(User.Field.CATEGORIES, categoryIds), profile?.categories.orEmpty()),
        Pair(InputField(User.Field.CITIES, cityIds), profile?.cities.orEmpty())
    ).plus(getLinksFields()).mapNotNull{ pair ->
        pair.first.takeUnless{ it.value == pair.second }
    }

    private fun getLinksFields() = linksMap.map {
        Pair(
            InputField(
                it.key.field,
                it.value?.filter { it != ' ' && it != '@' }.orEmpty()
            ),
            profile?.social?.socialMap?.getOrDefault(it.key, "").orEmpty()
        )
    }


}