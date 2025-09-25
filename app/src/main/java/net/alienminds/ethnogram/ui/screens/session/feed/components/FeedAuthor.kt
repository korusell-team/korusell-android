package net.alienminds.ethnogram.ui.screens.session.feed.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.feed.entities.FeedAuthor
import net.alienminds.ethnogram.ui.extentions.shimmerState
import net.alienminds.ethnogram.ui.theme.AppColor

@Composable
internal fun FeedAuthor(
    modifier: Modifier = Modifier,
    author: FeedAuthor?
) = Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
){
    val avatar = rememberAsyncImagePainter(author?.avatarUrl)
    val avatarState by avatar.state.collectAsState()
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(AppColor.gray100)
            .shimmerState(avatarState is AsyncImagePainter.State.Loading),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = avatarState is AsyncImagePainter.State.Success
        ) { isSuccess ->
            if (isSuccess) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    painter = avatar,
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            } else{
                Text(
                    text = author?.initials.orEmpty(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
    Text(
        text = author?.fullName?: stringResource(R.string.no_author),
        style = MaterialTheme.typography.titleSmall,
        color = AppColor.gray500,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}