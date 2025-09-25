package net.alienminds.ethnogram.ui.screens.session.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.alienminds.ethnogram.service.feed.entities.Feed
import net.alienminds.ethnogram.service.feed.entities.FeedAuthor
import net.alienminds.ethnogram.service.feed.entities.FeedType
import net.alienminds.ethnogram.ui.extentions.custom.LikeButton
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.utils.rememberRelativeTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun FeedCard(
    modifier: Modifier = Modifier,
    feed: Feed,
    author: FeedAuthor?,
    isFavorite: Boolean? = null,
    onClick: () -> Unit,
    onChangeFavorite: ((Boolean) -> Unit)? = null
){
    if (feed.isPromotedNow) {
        BigFeedCard(
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(AppColor.white)
                .clickable { onClick() }
                .padding(16.dp),
            feed = feed,
            author = author,
            isFavorite = isFavorite,
            onChangeFavorite = onChangeFavorite
        )
    } else {
        DefaultFeedCard(
            modifier = modifier
                .clickable{ onClick() },
            feed = feed,
            author = author
        )
    }
}

@Composable
private fun DefaultFeedCard(
    modifier: Modifier = Modifier,
    feed: Feed,
    author: FeedAuthor?
) = Row(
    modifier = modifier
){
    CoverImage(
        modifier = Modifier
            .padding(end = 16.dp)
            .height(96.dp)
            .aspectRatio(1f),
        imageUrl = feed.imageUrl,
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

        FeedAuthor(
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
    author: FeedAuthor?,
    isFavorite: Boolean?,
    onChangeFavorite: ((Boolean) -> Unit)?
) = Column(
    modifier = modifier.fillMaxWidth()
){
    Box{
        CoverImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            imageUrl = feed.imageUrl
        )
        feed.type?.let { type ->
            FeedTypeMark(
                modifier = Modifier.padding(8.dp),
                type = type
            )
        }
    }
    if (feed.type == FeedType.EVENT) {
        val date = feed.eventDetails?.startTime?.let {
            LocalDateTime.ofInstant(it, ZoneId.systemDefault())
        }
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = date?.let {
                DateTimeFormatter.ofPattern("dd MMMM в hh:mm").format(it)
            }.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = AppColor.gray700
        )
    }
    PrimaryContent(
        modifier = Modifier.padding(top = 8.dp),
        title = feed.title,
        description = feed.description,
        multiline = true
    )
    FeedAuthor(
        modifier = Modifier.padding(top = 8.dp),
        author = author
    )
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
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

