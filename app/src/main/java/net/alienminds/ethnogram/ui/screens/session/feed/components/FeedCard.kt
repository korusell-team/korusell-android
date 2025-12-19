package net.alienminds.ethnogram.ui.screens.session.feed.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.feed.entities.Author
import net.alienminds.ethnogram.service.feed.entities.Feed
import net.alienminds.ethnogram.service.feed.entities.FeedType
import net.alienminds.ethnogram.ui.extentions.custom.LikeButton
import net.alienminds.ethnogram.ui.extentions.custom.YoutubePreview
import net.alienminds.ethnogram.ui.extentions.custom.YoutubePreviewControls
import net.alienminds.ethnogram.ui.extentions.custom.YoutubePreviewIvLoadPolicy
import net.alienminds.ethnogram.ui.extentions.custom.YoutubePreviewParams
import net.alienminds.ethnogram.ui.extentions.shimmerState
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.utils.rememberRelativeTime
import ru.iquack.linkpreview.compose.LinkPreviewState
import ru.iquack.linkpreview.core.LinkPreview
import ru.iquack.linkpreview.core.OpenGraphTag
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun FeedCard(
    modifier: Modifier = Modifier,
    feed: Feed,
    author: Author?,
    isFavorite: Boolean? = null,
    onClick: () -> Unit,
    onChangeFavorite: ((Boolean) -> Unit)? = null
){
    if(feed.type == FeedType.EVENT){
        EventCard(
            modifier = modifier
                .padding(16.dp)
                .clickable{ onClick() },
            feed = feed,
            author = author,
        )
    } else if (feed.isPromotedNow) {
        BigFeedCard(
            modifier = modifier
                .padding(vertical = 16.dp)
                .clickable { onClick() },
            feed = feed,
            author = author,
            isFavorite = isFavorite,
            onChangeFavorite = onChangeFavorite,
            onClick = onClick
        )
    } else {
        DefaultFeedCard(
            modifier = modifier
                .padding(16.dp)
                .clickable{ onClick() },
            feed = feed,
            author = author,
        )
    }
}


@Composable
private fun EventCard(
    modifier: Modifier = Modifier,
    feed: Feed,
    author: Author?,
) = Row(
    modifier = modifier
){
    val previewState = LocalLinkPreviewStateHolder.current.getState(feed.webLink.orEmpty())
    val linkPreview = (previewState as? LinkPreviewState.Success)?.preview
    val imageUrl = when(feed.webLink.isNullOrEmpty()) {
        true -> feed.imageUrl
        false -> linkPreview?.openGraph?.image?: feed.imageUrl
    }
    CoverImage(
        modifier = Modifier
            .padding(end = 16.dp)
            .height(96.dp)
            .aspectRatio(1f),
        imageUrl = imageUrl,
    )
    Column{
        Text(
            text = feed.title.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            maxLines = 2
        )
        Text(
            text = feed.description.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColor.gray500,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )

        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            feed.eventDetails?.startTime?.let {
                LocalDateTime.ofInstant(it, ZoneId.systemDefault())
            }?.let {
                DateTimeFormatter.ofPattern("hh:mm").format(it)
            }?.let { timeFormated ->
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.ic_clock_outline),
                    contentDescription = null,
                    tint = AppColor.gray700
                )
                Text(
                    text = timeFormated,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColor.gray700
                )
            }

            Spacer(Modifier.weight(1f))

            AuthorContent(
                modifier = Modifier,
                author = author,
                avatarSize = 16.dp
            )
        }
    }
}

@Composable
private fun DefaultFeedCard(
    modifier: Modifier = Modifier,
    feed: Feed,
    author: Author?,
) = Row(
    modifier = modifier
){
    val previewState = LocalLinkPreviewStateHolder.current.getState(feed.webLink.orEmpty())
    val linkPreview = (previewState as? LinkPreviewState.Success)?.preview
    val imageUrl = when(feed.webLink.isNullOrEmpty()) {
        true -> feed.imageUrl
        false -> linkPreview?.openGraph?.image?: feed.imageUrl
    }
    CoverImage(
        modifier = Modifier
            .padding(end = 16.dp)
            .height(96.dp)
            .aspectRatio(1f),
        imageUrl = imageUrl,
    )
    Column{
        if (feed.type == FeedType.EVENT) {
            val date = feed.eventDetails?.startTime?.let {
                LocalDateTime.ofInstant(it, ZoneId.systemDefault())
            }
            Text(
                text = date?.let {
                    DateTimeFormatter.ofPattern("dd MMMM в hh:mm").format(it)
                }.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = AppColor.gray700
            )
        }
        PrimaryContent(
            title = feed.title,
            description = feed.description,
            multiline = false
        )

        AuthorContent(
            modifier = Modifier.padding(top = 4.dp),
            author = author
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RelativeTime(
                modifier = Modifier.padding(top = 4.dp),
                instant = feed.createdAt
            )

            feed.type?.let {
                FeedTypeMark(
                    type = it
                )
            }
        }
    }
}

@Composable
private fun BigFeedCard(
    modifier: Modifier = Modifier,
    feed: Feed,
    author: Author?,
    isFavorite: Boolean?,
    onChangeFavorite: ((Boolean) -> Unit)?,
    onClick: () -> Unit
) = Column(
    modifier = modifier.fillMaxWidth()
){
    val uriHandler = LocalUriHandler.current
    Box{
        LinkPreviewListCover(
            modifier = Modifier.fillMaxWidth(),
            linkPreviewUrl = feed.webLink,
            fallbackImageUrl = feed.imageUrl,
            onShowInfo = { feed.webLink?.let(uriHandler::openUri)?: run{
                onClick()
            } }
        )
        feed.type?.let { type ->
            FeedTypeMark(
                modifier = Modifier.padding(16.dp),
                type = type
            )
        }
    }
    PrimaryContent(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        title = feed.title,
        description = feed.description,
        multiline = true
    )
    AuthorContent(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        author = author
    )
    Row(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 16.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        RelativeTime(
            instant = feed.createdAt
        )
        isFavorite?.let { isFavorite ->
            LikeButton(
                modifier = Modifier,
                count = feed.likeList?.size,
                isFavorite = isFavorite,
                onChange = { onChangeFavorite?.invoke(it) },
            )
        }
    }
}


@Composable
private fun PrimaryContent(
    modifier: Modifier = Modifier,
    title: String?,
    description: String?,
    multiline: Boolean
) = Column(
    modifier = modifier
){
    Text(
        text = title.orEmpty(),
        style = when(multiline) {
            true -> MaterialTheme.typography.headlineSmall
            false -> MaterialTheme.typography.titleMedium
        },
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Bold,
        maxLines = when(multiline){
            true -> 2
            false -> 1
        }
    )
    Text(
        text = description.orEmpty(),
        style = MaterialTheme.typography.bodyMedium,
        color = AppColor.gray500,
        overflow = TextOverflow.Ellipsis,
        maxLines = when(multiline) {
            true -> 3
            false -> 1
        }
    )
}

@Composable
private fun RelativeTime(
    modifier: Modifier = Modifier,
    instant: Instant?
){
    instant?.let { createdAt ->
        val relativeTime by createdAt.rememberRelativeTime()
        Text(
            modifier = modifier,
            text = relativeTime,
            style = MaterialTheme.typography.labelSmall,
            color = AppColor.gray500,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

/**
 * Компонент для отображения превью ссылки с возможностью воспроизведения YouTube видео
 *
 * @param modifier Модификатор для настройки компонента
 * @param linkPreviewUrl URL ссылки для которой нужно отобразить превью
 * @param fallbackImageUrl URL изображения, используемого в случае отсутствия изображения в превью ссылки
 */
@Composable
fun LinkPreviewListCover(
    modifier: Modifier = Modifier,
    linkPreviewUrl: String?,
    fallbackImageUrl: String?,
    onShowInfo: () -> Unit
) {
    val previewState = LocalLinkPreviewStateHolder.current.getState(linkPreviewUrl.orEmpty())
    val linkPreview = (previewState as? LinkPreviewState.Success)?.preview
    val videoUrl = linkPreview?.openGraph?.tags?.getOther("video:url")
    val imageUrl = linkPreview?.openGraph?.image?: fallbackImageUrl
    val isYoutubeVideo = videoUrl != null && videoUrl.startsWith("https://www.youtube.com/embed")
    var playVideo by remember { mutableStateOf(false) }

    val contentFail = previewState is LinkPreviewState.Idle && fallbackImageUrl == null

    if (contentFail.not()) {
        Column(
            modifier = modifier
        ) {
            if (playVideo && isYoutubeVideo) {
                YoutubePreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    video = videoUrl,
                    params = YoutubePreviewParams(
                        playsInline = true,
                        autoplay = true,
                        controls = YoutubePreviewControls.NONE,
                        disableKb = true,
                        fullscreenButton = false,
                        loop = true,
                        showRelated = false,
                        interfaceLang = "ru",
                        ivLoadPolicy = YoutubePreviewIvLoadPolicy.DISABLED,
                        ccLoadPolicy = false
                    ),
                    onError = { playVideo = false }
                )
            } else {
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current.takeIf { isYoutubeVideo.not() },
                            onClick = { when (isYoutubeVideo) {
                                true -> playVideo = true
                                false -> onShowInfo()
                            } }
                        )
                ) {
                    imageUrl?.let {
                        val image = rememberAsyncImagePainter(it)
                        val imageState by image.state.collectAsState()
                        if (imageState !is AsyncImagePainter.State.Error) {
                            val isLoadingImage = imageState is AsyncImagePainter.State.Loading
                            Image(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(when(isLoadingImage){
                                        true -> Modifier
                                            .aspectRatio(1f)
                                            .background(AppColor.gray200)
                                        false -> Modifier
                                    })
                                    .shimmerState(isLoadingImage),
                                painter = image,
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                    if (isYoutubeVideo) {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AppColor.white)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = LocalIndication.current,
                                    onClick = { playVideo = true }
                                ),
                            painter = painterResource(R.drawable.ic_youtube_round),
                            contentDescription = null,
                            tint = AppColor.red
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = linkPreview != null
            ) {
                LinkPreviewInfo(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColor.brown50),
                    linkPreview = linkPreview,
                    onShowInfo = onShowInfo
                )
            }
        }
    }
}

@Composable
private fun LinkPreviewInfo(
    modifier: Modifier = Modifier,
    linkPreview: LinkPreview?,
    onShowInfo: () -> Unit
) = Row(
    modifier = modifier
        .clickable(linkPreview != null) { onShowInfo() }
        .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
){
    val title = linkPreview?.openGraph?.title?: linkPreview?.pageTitle
    val siteName = linkPreview?.openGraph?.siteName
    Column(
        modifier = Modifier.weight(1f)
    ){

        Text(
            text = title?: siteName.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = AppColor.gray900,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = siteName.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = AppColor.gray700,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
    Icon(
        modifier = Modifier.size(16.dp),
        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
        contentDescription = null,
        tint = AppColor.gray400
    )
}

private fun List<OpenGraphTag>.getOther(tag: String) = find {
    (it as? OpenGraphTag.Other)?.tagName == tag
}?.content
