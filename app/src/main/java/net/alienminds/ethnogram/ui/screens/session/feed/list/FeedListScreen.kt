package net.alienminds.ethnogram.ui.screens.session.feed.list

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.feed.entities.Feed
import net.alienminds.ethnogram.service.feed.entities.FeedType
import net.alienminds.ethnogram.ui.screens.session.NavBarScreen
import net.alienminds.ethnogram.ui.screens.session.feed.components.FeedCard
import net.alienminds.ethnogram.ui.screens.session.feed.details.FeedDetailsScreen
import net.alienminds.ethnogram.ui.theme.AppColor

internal object FeedListScreen: NavBarScreen {

    private fun readResolve(): Any = FeedListScreen

    override val position: Int
        get() = 0

    override val title: @Composable (() -> String)
        get() = { stringResource(R.string.tape) }

    override val icon: @Composable (() -> Painter)
        get() = { painterResource(R.drawable.ic_feed) }

    override val activeIcon: @Composable (() -> Painter)
        get() = { painterResource(R.drawable.ic_feed_fill) }

    private var lazyListState by mutableStateOf<LazyListState?>(null)


    override suspend fun onClickAgain() {
        lazyListState?.animateScrollToItem(0)
    }

    enum class Tabs(
        @param:StringRes val titleId: Int
    ){
        MAIN(R.string.main),
        EVENTS(R.string.events)
    }

    data class EventGroup(
        val title: String,
        val feeds: List<Feed>
    )


    @Composable
    override fun Content() = Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        val navigator = LocalNavigator.current
        val vm = rememberScreenModel { FeedListModel() }
        val lazyState = rememberLazyListState()

        val scope = rememberCoroutineScope()
        val density = LocalDensity.current
        val statusBarSize = with(density) {
            WindowInsets.statusBars.getTop(this).toDp()
        }

        LaunchedEffect(lazyListState) {
            lazyListState = lazyState
        }

        fun changeTab(tab: Tabs){
            vm.currentTab = tab
            scope.launch {
                lazyState.animateScrollToItem(0)
            }
        }

        Spacer(Modifier
            .fillMaxWidth()
            .height(statusBarSize)
            .background(AppColor.brown50))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyState
        ){
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColor.brown50)
                ){
                    Text(
                        modifier = Modifier
                            .padding(
                                vertical = 8.dp,
                                horizontal = 16.dp
                            ),
                        text = title(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            stickyHeader {
                SecondaryTabRow(
                    selectedTabIndex = vm.currentTab.ordinal,
                    containerColor = AppColor.brown50,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(
                                selectedTabIndex = vm.currentTab.ordinal,
                                matchContentSize = false
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                ) {
                    Tabs.entries.forEach { tab ->
                        Tab(
                            modifier = Modifier.heightIn(48.dp),
                            selected = vm.currentTab == tab,
                            onClick = { changeTab(tab) }
                        ) {
                            Text(
                                text = stringResource(tab.titleId),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            item{
                Spacer(Modifier
                    .fillMaxWidth()
                    .height(32.dp))
            }

            if(vm.currentTab == Tabs.MAIN){
                items(
                    items = vm.mainFeeds,
                    key = { it.id?: it.hashCode() }
                ){ feed ->
                    FeedCard(
                        modifier = Modifier.animateItem(),
                        feed = feed,
                        author = feed.authorId?.let { vm.authors[it] },
                        isFavorite = feed.likeList?.any { it == vm.myId } == true,
                        onClick = { feed.id?.let {
                            navigator?.push(FeedDetailsScreen(it))
                        } },
                        onChangeFavorite = { feed.id?.let { fid ->
                            vm.changeFavorite(fid, it)
                        } }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = AppColor.gray400
                    )
                }
            } else{
                vm.eventFeeds.forEach { group ->
                    item{
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = group.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 12.sp,
                                maxFontSize = 16.sp
                            ),
                            color = AppColor.gray500
                        )
                    }
                    items(
                        items = group.feeds,
                        key = { it.id?: it.hashCode() }
                    ){ feed ->
                        FeedCard(
                            modifier = Modifier.animateItem(),
                            feed = feed,
                            author = feed.authorId?.let { vm.authors[it] },
                            isFavorite = feed.likeList?.any { it == vm.myId } == true,
                            onClick = { feed.id?.let {
                                navigator?.push(FeedDetailsScreen(it))
                            } },
                            onChangeFavorite = { feed.id?.let { fid ->
                                vm.changeFavorite(fid, it)
                            } }
                        )
                    }
                }
            }
        }

    }

}