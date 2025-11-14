package net.alienminds.ethnogram.ui.screens.session

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.ui.extentions.custom.Avatar
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.extentions.transitions.SlidePageTransition
import net.alienminds.ethnogram.ui.screens.session.account.AccountScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.list.ContactsListScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.profile.ProfileScreen
import net.alienminds.ethnogram.ui.screens.session.feed.list.FeedListScreen
import net.alienminds.ethnogram.ui.theme.AppColor
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SessionScreen: Screen {

    @Composable
    override fun Content() {
        Navigator(FeedListScreen){ navigator ->
            val showNavBar = navigator.lastItemOrNull is NavBarScreen
            Column {
                SlidePageTransition(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    navigator = navigator
                )
                if (showNavBar){
                    Column {
                        val scope = rememberCoroutineScope()
                        HorizontalDivider(
                            modifier = Modifier.alpha(0.5f),
                            color = AppColor.gray400,
                            thickness = 0.5.dp
                        )
                        NavBar(
                            items = navBarScreens,
                            currentScreen = navigator.lastItemOrNull,
                            onItemClick = {
                                if (it.key != navigator.lastItemOrNull?.key) {
                                    navigator.replaceAll(it)
                                } else{
                                    scope.launch {
                                        it.onClickAgain()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun NavBar(
        modifier: Modifier = Modifier,
        items: List<NavBarScreen>,
        currentScreen: Screen?,
        onItemClick: (NavBarScreen) -> Unit
    ) = Row(
        modifier = modifier
            .shadow(4.dp)
            .fillMaxWidth()
            .heightIn(48.dp)
            .background(AppColor.brown50)
            .navigationBarsPadding()
            .padding(
                horizontal = 8.dp,
                vertical = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = currentScreen?.key == item.key
            NavBarItem(
                modifier = Modifier.weight(1f),
                item = item,
                selected = selected,
                onClick = { onItemClick(item) }
            )
        }
    }

    @Composable
    private fun NavBarItem(
        modifier: Modifier = Modifier,
        item: NavBarScreen,
        selected: Boolean,
        onClick: () -> Unit,
        contentColor: Color = Color(0xFF2D264B),
    ) {
        val userRepo = koinInject<UserRepository>()
        val user by userRepo.meFlow.collectAsState(null)
        val avatar = rememberAsyncImagePainter(user?.image?.firstOrNull())
        val avatarState by avatar.state.collectAsState()
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp)
                .padding(top = 4.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = selected,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { selected ->
                if (item == AccountScreen && avatarState is AsyncImagePainter.State.Success){
                    Image(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = when(selected) {
                                    true -> MaterialTheme.colorScheme.primary
                                    false -> MaterialTheme.colorScheme.outline
                                },
                                shape = CircleShape
                            ),
                        painter = avatar,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = when (selected) {
                            true -> item.activeIcon()
                            false -> item.icon()
                        },
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.title(),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )

        }
    }

    companion object{
        private val navBarScreens = listOf<NavBarScreen>(
            FeedListScreen,
            ContactsListScreen,
            AccountScreen
        )
    }

}


internal interface NavBarScreen: PageTransitionScreen{

    val title: @Composable () -> String

    val icon: @Composable () -> Painter

    val activeIcon: @Composable () -> Painter

    suspend fun onClickAgain(){

    }

}