package net.alienminds.ethnogram.ui.screens.session.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.alienminds.ethnogram.mappers.color
import net.alienminds.ethnogram.mappers.displayName
import net.alienminds.ethnogram.service.feed.entities.FeedType
import net.alienminds.ethnogram.ui.theme.AppColor

@Composable
internal fun FeedTypeMark(
    modifier: Modifier = Modifier,
    type: FeedType
) = Box(
    modifier = modifier
        .clip(MaterialTheme.shapes.extraSmall)
        .background(type.color)
        .padding(
            horizontal = 4.dp,
            vertical = 2.dp
        ),
    contentAlignment = Alignment.Center
){
    Text(
        text = type.displayName,
        color = AppColor.white,
        style = MaterialTheme.typography.labelMedium
    )
}