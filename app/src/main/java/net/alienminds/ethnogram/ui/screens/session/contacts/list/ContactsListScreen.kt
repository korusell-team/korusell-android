package net.alienminds.ethnogram.ui.screens.session.contacts.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.mappers.localName
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.data.entities.City
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.ui.extentions.custom.Avatar
import net.alienminds.ethnogram.ui.extentions.custom.LikeButton
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.ChipPickerDialog
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.rememberAppDialogState
import net.alienminds.ethnogram.ui.extentions.navigateByUserState
import net.alienminds.ethnogram.ui.screens.session.NavBarScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.list.components.ContactsScreenHeader
import net.alienminds.ethnogram.ui.screens.session.contacts.list.components.ContactsToolbar
import net.alienminds.ethnogram.ui.screens.session.contacts.list.entities.UserGroup
import net.alienminds.ethnogram.ui.screens.session.contacts.profile.ProfileScreen
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.utils.UserState
import java.time.Instant

object ContactsListScreen: NavBarScreen {

    private fun readResolve(): Any = ContactsListScreen

    override val position: Int
        get() = 1

    override val title: @Composable (() -> String)
        get() = { stringResource(R.string.contacts) }

    override val icon: @Composable (() -> Painter)
        get() = { painterResource(R.drawable.ic_contacts) }

    override val activeIcon: @Composable (() -> Painter)
        get() = { painterResource(R.drawable.ic_contacts_fill) }

    private var lazyListState by mutableStateOf<LazyListState?>(null)


    override suspend fun onClickAgain() {
        lazyListState?.animateScrollToItem(0)
    }

    @Composable
    override fun Content(){

        val navigator = LocalNavigator.currentOrThrow
        val vm = navigator.rememberNavigatorScreenModel { ContactsListViewModel() }
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
                isAnonymous = vm.isAnonymous,
                onChangeSearchMode = vm::switchSearchMode,
                onOpenCities = { dialogCities.show() },
                onShowProfile = { navigator.push(ProfileScreen()) },
                onSignIn = { navigator.navigateByUserState(UserState.Unauthorized) }
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
                onShowAllCategories = { navigator.push(SelectCategoryScreen) }
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
                userGroups = vm.userGroups,
                me = vm.me,
                categories = vm.allCategories,
                cities = vm.allCities,
                isAnonymous = vm.isAnonymous,
                onChangeFavorite = vm::changeFavorite,
            )
        }

        ChipPickerDialog(
            state = dialogCities,
            title = stringResource(R.string.filter_by_cities),
            items = vm.allCities,
            itemTitle = { it.localName },
            itemSelected = { vm.currentCity == it || vm.currentCity?.id == 0L },
            onSelect = vm::selectCity
        )
    }

    @Composable
    private fun PrimaryContent(
        modifier: Modifier = Modifier,
        userGroups: List<UserGroup>,
        me: User?,
        categories: List<Category>,
        cities: List<City>,
        isAnonymous: Boolean,
        onChangeFavorite: (String, Boolean) -> Unit,
    ){

        val navigator = LocalNavigator.current
        val lazyState = rememberLazyListState()

        LaunchedEffect(lazyState) {
            lazyListState = lazyState
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
            state = lazyState
        ){
            userGroups.forEach { group ->

                stickyHeader {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColor.gray100)
                            .padding(bottom = 8.dp, top = 16.dp)
                            .padding(horizontal = 16.dp)
                            .animateItem(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ){
                        Text(
                            text = group.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColor.blueGray600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                items(
                    items = group.users
                ){ user ->
                    UserItem(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .animateItem(),
                        user = user,
                        isFavorite = user.likes.any { me?.uid == it },
                        categories = categories,
                        cities = cities,
                        onChangeFavorite = { isFavorite ->
                            user.uid?.let { userId ->
                                onChangeFavorite(userId, isFavorite)
                            }
                        },
                        clickable = isAnonymous.not(),
                        onClick = { if (isAnonymous.not()) navigator?.push(ProfileScreen(user.uid)) }
                    )
                }
            }


            if (userGroups.isEmpty()) {
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
        clickable: Boolean,
        onChangeFavorite: (Boolean) -> Unit,
        onClick: () -> Unit
    ){

        Column(
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = null,
                    indication = null,
                    enabled = clickable,
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
                                user.surname?.let { append("$it ") }
                                user.name?.let { append(it) }
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
                LikeButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp),
                    count = user.likes.size,
                    isFavorite = isFavorite,
                    enabled = clickable,
                    onChange = onChangeFavorite
                )

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