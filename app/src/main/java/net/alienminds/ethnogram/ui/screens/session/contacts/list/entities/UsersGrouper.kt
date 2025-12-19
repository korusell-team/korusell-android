package net.alienminds.ethnogram.ui.screens.session.contacts.list.entities

import android.content.Context
import com.google.android.gms.maps.Projection
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.BuildConfig
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.data.entities.City
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.ui.screens.session.map.entities.UserClusterItem
import java.time.Instant
import kotlin.collections.sortedWith
import kotlin.text.compareTo


internal data class UserGroup(
    val title: String?,
    val users: List<User>,
    val groupType: Type = Type.DEFAULT
){
    enum class Type{
        DEFAULT, SPONSORED
    }
}

internal class UserGrouper(
    private val ctx: Context
) {

    fun filteredCategories(
        allCategories: List<Category>,
        searchMode: Boolean = false,
        searchText: String = "",
    ) = when (searchMode) {
        true -> allCategories.filter { it.contains(searchText) }
        false -> allCategories.categories
    }

    fun filteredSubCategories(
        allCategories: List<Category>,
        categoryId: Long?,
    ) = allCategories.filter { it.isSubCategory && it.parentId == categoryId }


    fun groupedAndFilteredUsers(
        myId: String?,
        city: City?,
        category: Category?,
        subCategory: Category?,
        searchMode: Boolean,
        searchText: String,
        allCategories: List<Category>,
        allUsers: List<User>
    ): List<UserGroup> {
        val unblockedUsers = allUsers.filterUsersByBlocking(myId.orEmpty())
        val isDefault = category == null &&
                subCategory == null &&
                city == null &&
                (searchMode && searchText.isNotEmpty()).not()

        return when(isDefault){
            true -> listOf(
                unblockedUsers.newUsersGroup(),
                unblockedUsers.topUsersGroup(20),
                unblockedUsers.activeUsersGroup(),
            )
            false -> listOf(
                unblockedUsers.filterUsersGroup(
                    city = city,
                    category = category,
                    subCategory = subCategory,
                    searchMode = searchMode,
                    searchText = searchText,
                    allCategories = allCategories
                )
            )
        }.filter { it.users.isNotEmpty() }
    }

    fun filterMapUser(
        myId: String?,
        category: Category?,
        subCategory: Category?,
        allCategories: List<Category>,
        allUsers: List<User>,
    ): List<UserClusterItem> {
        val unblockedUsers = allUsers.filterUsersByBlocking(myId.orEmpty())
        val isDefault = category == null && subCategory == null

        return when(isDefault){
            true -> unblockedUsers.filter { it.isSponsored }
            false -> {
                unblockedUsers.filterByCategories(
                    currentCategory = category,
                    currentSubCategory = subCategory,
                    subCategories = filteredSubCategories(allCategories, category?.id)
                ).filter { it.isSponsored }.sortedWith(
                    compareByDescending<User> { it.sponsoredExpDate?.let { it > Instant.now() } == true }
                        .thenByDescending { it.priority ?: 0L }
                        .thenByDescending { it.likes.size }
                )
            }
        }.mapNotNull(UserClusterItem::fromUserOrNull)
    }

    private fun List<UserClusterItem>.filterInBounds(projection: Projection?): List<UserClusterItem> {
        if(projection == null) return emptyList()
        return this.filter { projection.visibleRegion.latLngBounds.contains(it.position) }
    }

    fun List<User>.sponsoredUsersGroup(): UserGroup {
        return UserGroup(
            title = null,
            users = filter {
                it.isSponsored && (it.priority?: 0) >= 1
            }.sortedWith(
                compareByDescending<User> { it.priority ?: 0L }
                    .thenByDescending { it.likes.size }
            ),
            groupType = UserGroup.Type.SPONSORED
        )
    }

    private fun List<User>.newUsersGroup(): UserGroup {
        val newUserMinLimit = Instant.now().minus(15, java.time.temporal.ChronoUnit.DAYS)
        return UserGroup(
            title = ctx.getString(R.string.new_users),
            users = filter {
                it.created?.isAfter(newUserMinLimit) == true
            }.sortedByDescending { it.created }.take(3)
        )
    }
    
    fun List<User>.topUsersGroup(count: Int): UserGroup {
        return UserGroup(
            title = ctx.getString(R.string.top_users, count),
            users = sortedWith(
                compareByDescending<User>{ it.isSponsored }
                    .thenByDescending { it.likes.size }
            ).take(count)
        )
    }


    private fun List<User>.activeUsersGroup(): UserGroup {
        return UserGroup(
            title = ctx.getString(R.string.active_users),
            users = sortedByDescending { it.updated }
                .take(15)
                .sortedByDescending { it.isSponsored }
        )
    }

    private fun List<User>.filterUsersGroup(
        city: City?,
        category: Category?,
        subCategory: Category?,
        searchMode: Boolean,
        searchText: String,
        allCategories: List<Category>,
    ): UserGroup {
        val filteredUsers = filterUsersByCities(city?.id).let { users ->
            when(searchMode){
                true -> users.filterBySearch(
                    searchText = searchText,
                    allCategories = allCategories
                )
                false -> users.filterByCategories(
                    currentCategory = category,
                    currentSubCategory = subCategory,
                    subCategories = filteredSubCategories(allCategories, category?.id)
                )
            }
        }.sortedWith(
            compareByDescending<User> { it.sponsoredExpDate?.let { it > Instant.now() } == true }
                .thenByDescending { it.priority ?: 0L }
                .thenByDescending { it.likes.size }
            )


        val title = listOfNotNull(
            subCategory?.run { "$emoji $title" }?: category?.run { "$emoji $title" },
            city?.ru,
            when(searchMode && searchText.isNotEmpty()) {
                true -> ctx.resources.getQuantityString(
                    R.plurals.found_users,
                    filteredUsers.size,
                    filteredUsers.size
                )
                false -> null
            }
        ).joinToString(" | ")

        return UserGroup(
            title = title,
            users = filteredUsers
        )
    }

    fun List<User>.filterUsersByBlocking(myId: String) = filterNot{ user ->
        user.blockedBy.any { it == myId } ||
                user.reports.any { it == myId }
    }

    private fun List<User>.filterUsersByCities(cityId: Long?) = filter { user ->
        cityId == null ||
        cityId == 0L || // 0L means "all cities"
        user.cities.any { it == 0L } || // 0L means "all cities"
        user.cities.any { it == cityId }
    }

    private fun List<User>.filterBySearch(
        searchText: String,
        allCategories: List<Category>
    ): List<User> {
        val categories = filteredCategories(allCategories, true, searchText)
        return filter { it.contains(allCategories, searchText, categories) }
    }

    fun List<User>.filterByCategories(
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


    private val List<Category>.subCategories
        get() = filter{ it.isSubCategory }

    private val List<Category>.categories
        get() = filter{ it.isCategory }


    private fun Category.contains(
        text: String
    ): Boolean = text.lowercase().let{ lText ->
        title.lowercase().contains(lText) ||
                emoji.lowercase().contains(lText) ||
                tags.any { it.lowercase().contains(lText) }
    }

    private fun User.contains(
        allCategories: List<Category>,
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