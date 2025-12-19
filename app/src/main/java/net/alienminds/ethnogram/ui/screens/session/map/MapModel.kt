package net.alienminds.ethnogram.ui.screens.session.map

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import com.google.android.gms.maps.Projection
import com.google.maps.android.clustering.Cluster
import kotlinx.coroutines.flow.onEach
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.data.DataRepository
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.ui.screens.session.contacts.list.SelectCategoryScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.list.entities.UserGrouper
import net.alienminds.ethnogram.ui.screens.session.contacts.profile.ProfileScreen
import net.alienminds.ethnogram.ui.screens.session.map.entities.UserClusterItem
import net.alienminds.ethnogram.ui.screens.session.map.entities.id
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject
import kotlin.getValue

internal class MapModel(
    private val navigatorRequester: () -> Navigator
): AppScreenModel() {

    private val authRepo by inject<AuthRepository>()
    private val userRepo by inject<UserRepository>()
    private val dataRepo by inject<DataRepository>()
    private val appContext by inject<Context>()

    private val navigator get() = navigatorRequester()

    private val grouper = UserGrouper(appContext)

    private val allUsers by userRepo.publicUsersFlow.asStateWithLoading(emptyList())
    private var currentCluster: Cluster<UserClusterItem>? by mutableStateOf(null)

    val allCategories by dataRepo.getCategoriesFlow()
        .onEach(SelectCategoryScreen::setupAllCategories)
        .asStateWithLoading(emptyList())

    val allCities by dataRepo.getCitiesFlow()
        .asStateWithLoading(emptyList())

    val me by userRepo.meFlow.asState(null)
    val isAnonymous get() = authRepo.isAnonymous

    var currentCategory
        get() = SelectCategoryScreen.currentCategory
        set(value){ SelectCategoryScreen.currentCategory = value }

    var currentSubCategory
        get() = SelectCategoryScreen.currentSubCategory
        set(value){ SelectCategoryScreen.currentSubCategory = value }

    val categories by derivedStateOf { grouper.filteredCategories(allCategories) }
    val subCategories by derivedStateOf { grouper.filteredSubCategories(allCategories, currentCategory?.id) }
    var currentProjection by mutableStateOf<Projection?>(null)

    val mapItems by derivedStateOf { grouper.filterMapUser(
        myId = me?.uid,
        category = currentCategory,
        subCategory = currentSubCategory,
        allCategories = allCategories,
        allUsers = allUsers,
    ) }

    val currentClusterId
        get() = currentCluster?.takeIf { it.items.isNotEmpty() }?.id

    val sheetItems by derivedStateOf {
        when(currentCluster?.items.isNullOrEmpty()){
            true -> allUsers.filterSheetUsers(currentCategory, currentSubCategory, currentProjection)
            false -> currentCluster?.items?.map { it.user }.orEmpty()
        }
    }

    private fun List<User>.filterSheetUsers(category: Category?, subCategory: Category?, projection: Projection?): List<User> = with(grouper){
        val unblocked = filterUsersByBlocking(me?.uid.orEmpty())
        val isDefault = category == null && subCategory == null
        return@with when(isDefault){
            true -> unblocked
            false -> unblocked.filterByCategories(
                currentCategory = category,
                currentSubCategory = subCategory,
                subCategories = filteredSubCategories(allCategories, category?.id)
            ).filterInBounds(projection)
        }.filter { it.isSponsored && it.isLocationAvailable }
            .sortedByDescending { it.likes.size }
            .let {
                when(isDefault){
                    true -> it.take(10)
                    false -> it
                }
            }
    }

    fun selectCluster(cluster: Cluster<UserClusterItem>?): Boolean{
        if (cluster == currentCluster){
            return false
        }
        currentCluster = cluster
        return true
    }

    fun selectClusterItem(item: UserClusterItem): Boolean =
        selectUser(item.user)

    fun selectUser(user: User): Boolean{
        val userId = user.uid?: return false
        navigator.push(ProfileScreen(userId))
        return true
    }

    fun selectCategory(category: Category){
        SelectCategoryScreen.selectCategory(category)
        currentCluster = null
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

    private fun List<User>.filterInBounds(projection: Projection?): List<User> {
        if(projection == null) return emptyList()
        return mapNotNull(UserClusterItem::fromUserOrNull)
            .filter { projection.visibleRegion.latLngBounds.contains(it.position) }
            .map { it.user }
    }

}