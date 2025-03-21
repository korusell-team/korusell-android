package net.alienminds.ethnogram.ui.screens.session.constacts

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.mappers.localName
import net.alienminds.ethnogram.service.categories.entities.Category
import net.alienminds.ethnogram.service.cities.entities.City
import net.alienminds.ethnogram.service.users.entities.User
import net.alienminds.ethnogram.ui.extentions.custom.Avatar
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.ChipPickerDialog
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.rememberAppDialogState
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.screens.session.constacts.components.ContactsScreenHeader
import net.alienminds.ethnogram.ui.screens.session.constacts.components.ContactsToolbar
import net.alienminds.ethnogram.ui.screens.session.profile.ProfileScreen
import net.alienminds.ethnogram.ui.theme.AppColor

class ContactsScreen: PageTransitionScreen {

    override val position: Int = 0


    @Composable
    override fun Content(){

        val navigator = LocalNavigator.current
        val vm = rememberScreenModel { ContactsViewModel() }

        val dialogCities = rememberAppDialogState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColor.brown50)
        ){
            ContactsToolbar(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp),
                avatarUrl = vm.me?.smallImage?: vm.me?.image?.firstOrNull(),
                initials = vm.me?.initials.orEmpty(),
                searchMode = vm.searchMode,
                onChangeSearchMode = vm::switchSearchMode,
                onOpenCities = { dialogCities.show() },
                onShowProfile = { navigator?.push(ProfileScreen()) }
            )

            ContactsScreenHeader(
                modifier = Modifier.padding(vertical = 16.dp),
                searchMode = vm.searchMode,
                searchText = vm.searchText,
                currentCategory = vm.currentCategory,
                currentSubCategory = vm.currentSubCategory,
                categories = vm.categories,
                subCategories = vm.subCategories,
                onSelectCategory = vm::selectCategory,
                onSwitchSearchMode = vm::switchSearchMode,
                onChangeSearch = { vm.searchText = it },
            )

            PrimaryContent(
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(
                            topStart = 100f,
                            topEnd = 100f
                        )
                    )
                    .fillMaxSize(),
                users = vm.users,
                me = vm.me,
                categories = vm.allCategories,
                cities = vm.allCities,
                isLoading = vm.loading,
                onChangeFavorite = vm::changeFavorite,
                onLoadMore = vm::loadMore
            )
        }

        ChipPickerDialog(
            state = dialogCities,
            title = stringResource(R.string.filter_by_cities),
            items = vm.allCities,
            itemTitle = { it.localName },
            itemSelected = { vm.currentCity == it || vm.currentCity?.id == 0 },
            onSelect = vm::selectCity
        )
    }

    @Composable
    private fun PrimaryContent(
        modifier: Modifier = Modifier,
        users: List<User>,
        me: User?,
        categories: List<Category>,
        cities: List<City>,
        isLoading: Boolean,
        onChangeFavorite: (String, Boolean) -> Unit,
        onLoadMore: () -> Unit
    ){

        val navigator = LocalNavigator.current
        val lazyState = rememberLazyListState()

        val shouldLoadMore by remember {
            derivedStateOf {
                val totalItemsCount = lazyState.layoutInfo.totalItemsCount
                val lastVisibleItemIndex = lazyState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisibleItemIndex >= (totalItemsCount) && !isLoading
            }
        }

        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore){
                onLoadMore()
            }
        }

        LazyColumn(
            modifier = modifier
                .background(
                    color = AppColor.gray100,
                    shape = RoundedCornerShape(
                        topStart = 100f,
                        topEnd = 100f
                    )
                ),
            state = lazyState,
            contentPadding = PaddingValues(top = 16.dp)
        ){
            items(
                items = users,
                key = { it.uid }
            ){ user ->
                UserItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .animateItem(),
                    user = user,
                    isFavorite = user.likes.any { me?.uid?.equals(it) == true },
                    categories = categories,
                    cities = cities,
                    onChangeFavorite = { onChangeFavorite(user.uid, it) },
                    onClick = { navigator?.push(ProfileScreen(user.uid)) }
                )
            }
            if (users.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .height(64.dp)
                            .fillMaxWidth()
                            .animateItem()
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(R.string.empty_list),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColor.gray400
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun UserItem(
        modifier: Modifier = Modifier,
        user: User,
        isFavorite: Boolean,
        categories: List<Category>,
        cities: List<City>,
        onChangeFavorite: (Boolean) -> Unit,
        onClick: () -> Unit
    ){

        Column(
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onClick
                )
        ){
            val userCategories = remember(user.categories, categories){
                user.categories.mapNotNull{ catId ->
                    categories.find { it.id == catId }
                }
            }

            val userCities = remember(user.cities, cities){
                user.cities.mapNotNull { cityId ->
                    cities.find { it.id == cityId }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth()
            ){
                Row {
                    Avatar(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(52.dp),
                        model = user.image.firstOrNull(),
                        initials = user.initials,
                        contentScale = ContentScale.Crop,
                        border = BorderStroke(1.dp, AppColor.blueGray900)
                    )
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    ){
                        Text(
                            text = remember(user.name, user.surname){ buildString {
                                user.name?.let { append("$it ") }
                                user.surname?.let { append(it) }
                            } },
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColor.blueGray900
                        )
                        Text(
                            text = userCities.joinToString { it.localName },
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColor.gray500
                        )
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = user.bio.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColor.gray600,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )
                    }
                }
                AnimatedContent(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = { onChangeFavorite(isFavorite.not()) }
                        ),
                    targetState = user.likes
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = when (isFavorite) {
                                true -> Icons.Filled.Favorite
                                false -> Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = null,
                            tint = when(isFavorite) {
                                true -> AppColor.red500
                                false -> AppColor.gray600
                            }
                        )
                        if (it.isNotEmpty()) {
                            Text(
                                text = "${user.likes.size}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp, top = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){

                userCategories.forEach { cat ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = AppColor.gray200,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                    ) {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = cat.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColor.gray700,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            HorizontalDivider(Modifier.padding(start = 80.dp, bottom = 8.dp))
        }
    }

}