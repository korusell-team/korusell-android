package net.alienminds.ethnogram.ui.screens.session.contacts.list.entities

import android.content.Context
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.data.entities.City
import net.alienminds.ethnogram.service.user.entities.User
import java.time.Instant


internal data class UserGroup(
    val title: String,
    val users: List<User>
)

internal class UserGrouper(
    private val ctx: Context
) {

    fun filteredCategories(
        allCategories: List<Category>,
        searchMode: Boolean,
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
                unblockedUsers.topUsersGroup()
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

    private fun List<User>.newUsersGroup(): UserGroup {
        val newUserMinLimit = Instant.now().minus(15, java.time.temporal.ChronoUnit.DAYS)
        return UserGroup(
            title = ctx.getString(R.string.new_users),
            users = filter {
                it.created?.isAfter(newUserMinLimit) == true
            }.take(3)
        )
    }
    
    private fun List<User>.topUsersGroup(): UserGroup {
        return UserGroup(
            title = ctx.getString(R.string.top_users),
            users = sortedByDescending { it.likes.size }.take(20)
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
        }


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

    private fun List<User>.filterUsersByBlocking(myId: String) = filterNot{ user ->
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