package net.alienminds.ethnogram.ui.screens.session.feed.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.ui.extentions.custom.YoutubePreview
import net.alienminds.ethnogram.ui.extentions.custom.YoutubePreviewControls
import net.alienminds.ethnogram.ui.extentions.custom.YoutubePreviewIvLoadPolicy
import net.alienminds.ethnogram.ui.extentions.custom.YoutubePreviewParams
import net.alienminds.ethnogram.ui.theme.AppColor
import ru.iquack.linkpreview.compose.LinkPreviewState
import ru.iquack.linkpreview.compose.LinkPreviewStateHolder
import ru.iquack.linkpreview.core.LinkPreview
import ru.iquack.linkpreview.core.LinkPreviewLoader
import ru.iquack.linkpreview.core.OpenGraphTag


val LocalLinkPreviewStateHolder = staticCompositionLocalOf { LinkPreviewStateHolder(
    loader = LinkPreviewLoader.Builder().build(),
    scope = CoroutineScope(Dispatchers.IO)
) }


/**
 * Компонент для отображения превью ссылки с возможностью воспроизведения YouTube видео
 *
 * @param modifier Модификатор для настройки компонента
 * @param linkPreviewUrl URL ссылки для которой нужно отобразить превью
 * @param fallbackImageUrl URL изображения, используемого в случае отсутствия изображения в превью ссылки
 */
@Composable
fun LinkPreviewCover(
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
                        .weight(1f),
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
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current.takeIf { isYoutubeVideo.not() },
                            onClick = { when (isYoutubeVideo) {
                                true -> playVideo = true
                                false -> onShowInfo()
                            } }
                        )
                ) {
                    CoverImage(
                        modifier = Modifier.matchParentSize(),
                        imageUrl = imageUrl,
                        shape = RectangleShape
                    )
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
