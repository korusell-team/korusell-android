package net.alienminds.ethnogram.ui.screens.session.feed.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.mappers.displayName
import net.alienminds.ethnogram.mappers.emoji
import net.alienminds.ethnogram.service.feed.entities.FeedType
import net.alienminds.ethnogram.ui.extentions.shimmerBrush
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


    @Composable
    override fun Content() = Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.brown50)
    ){
        val navigator = LocalNavigator.current
        val vm = rememberScreenModel { FeedListModel() }
        Toolbar(
            modifier = Modifier.statusBarsPadding(),
            onClickMyPost = {  }
        )

        TypeFilter(
            modifier = Modifier,
            current = vm.type,
            onChange = { vm.type = it }
        )

        LazyColumn(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(
                        topStart = 100f,
                        topEnd = 100f
                    )
                )
                .background(
                    color = AppColor.gray100,
                    shape = RoundedCornerShape(
                        topStart = 100f,
                        topEnd = 100f
                    )
                )
                .fillMaxSize()
        ){
            items(
                items = vm.feeds,
                key = { it.id?: it.hashCode() }
            ){ feed ->
                FeedCard(
                    modifier = Modifier
                        .animateItem()
                        .padding(16.dp),
                    feed = feed,
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
        }

    }


    @Composable
    private fun TypeFilter(
        modifier: Modifier = Modifier,
        current: FeedType?,
        onChange: (FeedType?) -> Unit
    ) = Row(
        modifier = modifier
            .padding(vertical = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Spacer(Modifier.width(8.dp))
        FeedType.entries.forEach {
            FilterChip(
                emoji = it.emoji,
                title = it.displayName.lowercase().replaceFirstChar { it.uppercase() },
                isSelected = current == it,
                onSelect = { onChange(it.takeIf { it != current }) }
            )
        }
        Spacer(Modifier.width(8.dp))
    }
    
    @Composable
    private fun Toolbar(
        modifier: Modifier = Modifier,
        onClickMyPost: () -> Unit
    ) = Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        Spacer(Modifier)
        Text(
            text = title(),
            style = MaterialTheme.typography.titleMedium,
            color = AppColor.gray900,
            fontWeight = FontWeight.SemiBold
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .padding(vertical = 4.dp)
                .clip(CircleShape)
                .background(shimmerBrush())
                .clickable(
                    enabled = false
                ) { onClickMyPost() }
                .padding(
                    vertical = 4.dp,
                    horizontal = 12.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "BETA",//stringResource(R.string.my_posts),
                style = MaterialTheme.typography.bodySmall,
                color = AppColor.gray100
            )
        }
    }

    @Composable
    private fun FilterChip(
        modifier: Modifier = Modifier,
        emoji: String,
        title: String,
        isSelected: Boolean,
        onSelect: () -> Unit
    ) = TextButton(
        modifier = modifier.height(34.dp),
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp
        ),
        colors = ButtonDefaults.textButtonColors(
            containerColor = when (isSelected) {
                true -> AppColor.blueGray700
                false -> AppColor.gray100
            }
        ),
        onClick = { onSelect() }
    ) {
        Text(
            text = "$emoji $title",
            style = MaterialTheme.typography.labelLarge,
            color = when (isSelected) {
                true -> AppColor.blueGray100
                false -> AppColor.gray700
            }
        )
    }


}