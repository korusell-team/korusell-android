package net.alienminds.ethnogram.ui.screens.session.feed.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.feed.entities.EventDetails
import net.alienminds.ethnogram.service.feed.entities.FeedType
import net.alienminds.ethnogram.service.feed.entities.PromoDetail
import net.alienminds.ethnogram.ui.extentions.buttons.BackButton
import net.alienminds.ethnogram.ui.extentions.custom.LikeButton
import net.alienminds.ethnogram.ui.screens.session.contacts.profile.ProfileScreen
import net.alienminds.ethnogram.ui.screens.session.feed.components.AuthorContent
import net.alienminds.ethnogram.ui.screens.session.feed.components.FeedTypeMark
import net.alienminds.ethnogram.ui.screens.session.feed.components.LinkPreviewCover
import net.alienminds.ethnogram.ui.screens.session.feed.components.LocalLinkPreviewStateHolder
import net.alienminds.ethnogram.ui.theme.AppColor
import ru.iquack.linkpreview.compose.LinkPreviewState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val urlRegex = Regex("""(https?://[^\s]+)""")

internal class FeedDetailsScreen(
    private val feedId: String
): Screen {

    @Composable
    override fun Content() = Column(
        modifier = Modifier.fillMaxSize()
    ){
        val navigator = LocalNavigator.current
        val vm = rememberScreenModel { FeedDetailsModel(feedId) }
        val uriHandler = LocalUriHandler.current

        val previewState = LocalLinkPreviewStateHolder.current.getState(vm.feed?.webLink.orEmpty())
        val coverFail = previewState is LinkPreviewState.Idle && vm.feed?.imageUrl == null
        Toolbar(
            modifier = Modifier.statusBarsPadding(),
            title = vm.feed?.title.orEmpty(),
            type = vm.feed?.type?.takeIf { vm.feed?.imageUrl == null }
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ){
            if (coverFail.not()) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    LinkPreviewCover(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.large),
                        linkPreviewUrl = vm.feed?.webLink,
                        fallbackImageUrl = vm.feed?.imageUrl,
                        onShowInfo = { vm.feed?.webLink?.let(uriHandler::openUri) }
                    )
//                    CoverImage(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .aspectRatio(1f),
//                        imageUrl = vm.feed?.imageUrl,
//                    )
                    vm.feed?.type?.let { type ->
                        FeedTypeMark(
                            modifier = Modifier.padding(8.dp),
                            type = type
                        )
                    }
                }
            }
            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                text = vm.feed?.title.orEmpty(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            val descr = vm.feed?.description.orEmpty().parseUrl()
            ClickableText(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp),
                text = descr,
                onClick = { offset ->
                    descr.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let {
                            uriHandler.openUri(it.item)
                        }
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AppColor.gray700
                ),
            )

            AnimatedVisibility(
                visible = vm.feed?.eventDetails != null
            ) {
                EventCard(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp),
                    event = vm.feed?.eventDetails
                )
            }

            AnimatedVisibility(
                visible = (vm.feed?.promoDetails?.discount?: 0) > 0
            ) {
                PromoCard(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp),
                    promo = vm.feed?.promoDetails
                )
            }

            AuthorContent(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp)
                    .clickable{
                        vm.feed?.authorId?.let {
                            navigator?.push(ProfileScreen(it))
                        }
                    },
                author = vm.author
            )
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp)
            ){
                LikeButton(
                    count = vm.feed?.likeList?.size?: 0,
                    isFavorite = vm.isFavorite,
                    onChange = { vm.changeFavoriteFeed(it) }
                )
            }
            Spacer(Modifier
                .navigationBarsPadding()
                .height(48.dp))
        }
    }


    @Composable
    private fun EventCard(
        modifier: Modifier,
        event: EventDetails?
    ) = Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFFE5EEFF))
            .padding(
                vertical = 8.dp,
                horizontal = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ){
        @Composable
        fun Item(
            icon: Painter,
            text: String
        ) = Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ){
            Icon(
                modifier = Modifier.size(20.dp),
                painter = icon,
                tint = Color(0xFF0076FF),
                contentDescription = null
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF0076FF),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        Item(
            icon = painterResource(R.drawable.ic_pin_drop),
            text = event?.location.orEmpty(),
        )
        Item(
            icon = painterResource(R.drawable.ic_clock_outline),
            text = stringResource(R.string.start, event?.startTime?.displayValue.orEmpty()),
        )
        Item(
            icon = painterResource(R.drawable.ic_clock_fill),
            text = stringResource(R.string.end, event?.endTime?.displayValue.orEmpty()),
        )
    }

    @Composable
    private fun PromoCard(
        modifier: Modifier,
        promo: PromoDetail?
    ) = Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFFE9F9EE))
            .padding(
                vertical = 8.dp,
                horizontal = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ){
        @Composable
        fun Item(
            icon: Painter,
            text: String
        ) = Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ){
            Icon(
                modifier = Modifier.size(20.dp),
                painter = icon,
                tint = Color(0xFF36C65E),
                contentDescription = null
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF36C65E),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        Item(
            icon = painterResource(R.drawable.ic_calendar),
            text = stringResource(R.string.discount, promo?.discount?.toString().orEmpty()),
        )
        Item(
            icon = painterResource(R.drawable.ic_discount),
            text = stringResource(R.string.valid_before, promo?.validUntil?.displayValue.orEmpty()),
        )

    }


    @Composable
    private fun Toolbar(
        modifier: Modifier = Modifier,
        title: String,
        type: FeedType?
    ) = Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 8.dp),
    ) {
        BackButton(
            modifier = Modifier.align(Alignment.CenterStart),
            text = stringResource(R.string.tape),
            tint = AppColor.blue600
        )
//        Text(
//            modifier = Modifier.align(Alignment.Center),
//            text = title,
//            style = MaterialTheme.typography.titleMedium,
//            color = AppColor.gray900,
//            fontWeight = FontWeight.SemiBold,
//            overflow = TextOverflow.Ellipsis,
//            maxLines = 1
//        )

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterEnd),
            visible = type != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FeedTypeMark(
                modifier = Modifier.padding(end = 8.dp),
                type = type?: FeedType.NEWS
            )
        }
    }

    private fun String.parseUrl() = buildAnnotatedString {
        val text = this@parseUrl
        var lastIndex = 0

        for (match in urlRegex.findAll(text)) {
            val start = match.range.first
            val end = match.range.last + 1

            // Добавляем обычный текст перед ссылкой
            append(text.substring(lastIndex, start))

            // Добавляем ссылку с аннотацией
            val url = match.value
            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(
                SpanStyle(
                    color = Color(0xFF1E88E5),
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(url)
            }
            pop()

            lastIndex = end
        }

        // Добавляем остаток текста
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }


    private val Instant.displayValue
        get() = DateTimeFormatter
            .ofPattern("dd LLLL yyyy, HH:mm")
            .withLocale(Locale("ru"))
            .format(LocalDateTime.ofInstant(this, ZoneId.systemDefault()))
}