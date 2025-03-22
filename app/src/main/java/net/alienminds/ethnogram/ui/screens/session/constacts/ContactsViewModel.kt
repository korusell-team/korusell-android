package net.alienminds.ethnogram.ui.screens.session.constacts

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.alienminds.ethnogram.service.API
import net.alienminds.ethnogram.service.categories.entities.Category
import net.alienminds.ethnogram.service.cities.entities.City
import net.alienminds.ethnogram.service.users.entities.User
import net.alienminds.ethnogram.utils.AppScreenModel

class ContactsViewModel: AppScreenModel() {

    private val allUsers by API.users.publicUsers.toState(emptyList())
    val allCities by API.cities.all.toState(emptyList())
    val allCategories by API.categories.all.toState(emptyList())

    val me by API.users.me.toState(null)

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
                currentCategory = allCategories.find { it.id == p_id }
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
    ) = withLoadingScope {
        API.users.favoriteUser(userId, isFavorite)
    }

    private fun filteredCategories() = derivedStateOf {
        when(searchMode){
            true -> allCategories.filter { it.contains(searchText) }
            false -> allCategories.filter { it.isCategory }
        }
    }

    private fun filteredSubCategories() = derivedStateOf {
        allCategories.filter {
            it.isSubCategory && it.p_id == currentCategory?.id
        }
    }

    private fun filteredUsers() = derivedStateOf {
        allUsers.filterByBlocking(me?.uid.orEmpty()).filterByCities(currentCity).let { filteredUsers ->
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
                it.id == uCat.id || (uCat.isSubCategory && it.p_id == uCat.p_id)
            }
        }

        fun String?.containsOrFalse(text: String) = this?.contains(text)?: false

        return containsCategories(searchCategories) ||
                (name?.lowercase().containsOrFalse(lText)) ||
                (surname?.lowercase().containsOrFalse(lText)) ||
                (phone?.lowercase().containsOrFalse(lText).takeIf { phoneIsAvailable }?: false) ||
                (instagram?.lowercase().containsOrFalse(lText)) ||
                (telegram?.lowercase().containsOrFalse(lText)) ||
                (whatsApp?.lowercase().containsOrFalse(lText))
    }


    fun loadMore() = withLoadingScope {
//        API.users.loadMore(isPublic = true)
    }


}