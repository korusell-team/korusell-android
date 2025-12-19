package net.alienminds.ethnogram.ui.screens.session.contacts.list

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.onEach
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.data.DataRepository
import net.alienminds.ethnogram.service.data.entities.City
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.ui.screens.session.contacts.list.entities.UserGrouper
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject

internal class ContactsListModel: AppScreenModel() {

    private val authRepo by inject<AuthRepository>()
    private val userRepo by inject<UserRepository>()
    private val dataRepo by inject<DataRepository>()
    private val appContext by inject<Context>()

    private val allUsers by userRepo.publicUsersFlow.asStateWithLoading(emptyList())

    val allCities by dataRepo.getCitiesFlow().asStateWithLoading(emptyList())
    val allCategories by dataRepo.getCategoriesFlow()
        .onEach(SelectCategoryScreen::setupAllCategories)
        .asStateWithLoading(emptyList())

    val me by userRepo.meFlow.asState(null)

    val isAnonymous
        get() = authRepo.isAnonymous

    var currentCategory
        get() = SelectCategoryScreen.currentCategory
        set(value){ SelectCategoryScreen.currentCategory = value }

    var currentSubCategory
        get() = SelectCategoryScreen.currentSubCategory
        set(value){ SelectCategoryScreen.currentSubCategory = value }

    var currentCity by mutableStateOf<City?>(null)
        private set

    var searchMode by mutableStateOf(false)
        private set

    var searchText by mutableStateOf("")


    val grouper = UserGrouper(appContext)

    val categories by derivedStateOf { grouper.filteredCategories(allCategories, searchMode, searchText) }
    val subCategories by derivedStateOf { grouper.filteredSubCategories(allCategories, currentCategory?.id) }
    val userGroups by derivedStateOf { grouper.groupedAndFilteredUsers(
        myId = me?.uid,
        city = currentCity,
        category = currentCategory,
        subCategory = currentSubCategory,
        searchMode = searchMode,
        searchText = searchText,
        allCategories = allCategories,
        allUsers = allUsers
    ) }


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
    ){
        if (isAnonymous){ return }
        launchWithLoading {
            userRepo.favoriteUser(userId, isFavorite)
        }
    }


}