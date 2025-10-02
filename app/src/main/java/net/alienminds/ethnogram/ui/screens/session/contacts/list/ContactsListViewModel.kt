package net.alienminds.ethnogram.ui.screens.session.contacts.list

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.alienminds.ethnogram.service.data.DataRepository
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.data.entities.City
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

class ContactsListViewModel: AppScreenModel() {

    private val userRepo by inject<UserRepository>()
    private val dataRepo by inject<DataRepository>()

    private val allUsers by userRepo.publicUsersFlow.asStateWithLoading(emptyList())

    val allCities by dataRepo.getCitiesFlow().asStateWithLoading(emptyList())
    val allCategories by dataRepo.getCategoriesFlow().asStateWithLoading(emptyList())

    val me by userRepo.meFlow.asState(null)

    var currentCategory by mutableStateOf<Category?>(null)
        private set

    var currentSubCategory by mutableStateOf<Category?>(null)
        private set

    var currentCity by mutableStateOf<City?>(null)
        private set

    var searchMode by mutableStateOf(false)
        private set

    var searchText by mutableStateOf("")


    val categories by filteredCategories()
    val subCategories by filteredSubCategories()
    val users by filteredUsers()


    fun selectCategory(category: Category){
        searchMode = false
        when(category.isCategory){
            true -> currentCategory = category.takeUnless { it == currentCategory }
            false -> currentSubCategory = category.takeUnless { it == currentSubCategory }?.apply {
                currentCategory = allCategories.find { it.id == parentId }
            }
        }
        if (category.isCategory){
            currentSubCategory = null
        }
    }

    fun selectCity(city: City){
        currentCity = city.takeUnless { it == currentCity }
    }

    fun switchSearchMode(
        enable: Boolean = searchMode.not()
    ){
        searchMode = enable
        currentCategory = null
        currentSubCategory = null
        searchText = ""
    }

    fun changeFavorite(
        userId: String,
        isFavorite: Boolean
    ) = launchWithLoading {
        userRepo.favoriteUser(userId, isFavorite)
    }

    private fun filteredCategories() = derivedStateOf {
        when(searchMode){
            true -> allCategories.filter { it.contains(searchText) }
            false -> allCategories.filter { it.isCategory }
        }
    }

    private fun filteredSubCategories() = derivedStateOf {
        allCategories.filter {
            it.isSubCategory && it.parentId == currentCategory?.id
        }
    }

    private fun filteredUsers() = derivedStateOf {
        allUsers.filterByBlocking(me?.uid.orEmpty())
            .filterByCities(currentCity)
            .let { filteredUsers ->
                when(searchMode){
                    true -> filteredUsers.filterBySearch(
                        searchText = searchText,
                        categories = categories
                    )
                    false -> filteredUsers.filterByCategories(
                        currentCategory = currentCategory,
                        currentSubCategory = currentSubCategory,
                        subCategories = subCategories
                    )
                }
            }
    }

    private fun List<User>.filterByBlocking(myId: String) = filterNot{ user ->
        user.blockedBy.any { it == myId } ||
        user.reports.any { it == myId }
    }

    private fun List<User>.filterBySearch(
        searchText: String,
        categories: List<Category>
    ) = filter { it.contains(searchText, categories) }


    private fun List<User>.filterByCities(
        currentCity: City?
    ) = filter { user ->
        currentCity == null ||
        currentCity.id == 0L ||
        user.cities.any { it == 0L } ||
        user.cities.any { it == currentCity.id }
    }


    private fun List<User>.filterByCategories(
        currentCategory: Category?,
        currentSubCategory: Category?,
        subCategories: List<Category>
    ): List<User> {
        fun bySubCategory(
            currentSubCategory: Category?
        ) = currentSubCategory?.let { sub ->
            filter { user ->
                user.categories.any { it == sub.id }
            }
        }

        fun byCategory(
            currentCategory: Category?,
            subCategories: List<Category>
        ) = currentCategory?.let { root ->
            filter { user ->
                subCategories.any { subs ->
                    user.categories.any { it == subs.id || it == root.id }
                }
            }
        }

        return bySubCategory(
            currentSubCategory = currentSubCategory
        )?: byCategory(
            currentCategory = currentCategory,
            subCategories = subCategories
        )?: this
    }



    private fun Category.contains(
        text: String
    ): Boolean = text.lowercase().let{ lText ->
        title.lowercase().contains(lText) ||
        emoji.lowercase().contains(lText) ||
        tags.any { it.lowercase().contains(lText) }
    }

    private fun User.contains(
        text: String,
        searchCategories: List<Category>
    ): Boolean = text.lowercase().let{ lText ->

        fun User.containsCategories(
            searchCategories: List<Category>
        ) = categories.mapNotNull{ catId ->
            allCategories.find { it.id == catId }
        }.any{ uCat ->
            searchCategories.any {
                it.id == uCat.id || (uCat.isSubCategory && it.parentId == uCat.parentId)
            }
        }

        fun String?.containsOrFalse(text: String) = this?.contains(text)?: false

        return containsCategories(searchCategories) ||
                (name?.lowercase().containsOrFalse(lText)) ||
                (surname?.lowercase().containsOrFalse(lText)) ||
                (phone?.lowercase().containsOrFalse(lText).takeIf { phoneIsAvailable == true } == true) ||
                (social.instagram?.lowercase().containsOrFalse(lText)) ||
                (social.telegram?.lowercase().containsOrFalse(lText)) ||
                (social.whatsApp?.lowercase().containsOrFalse(lText))
    }


}