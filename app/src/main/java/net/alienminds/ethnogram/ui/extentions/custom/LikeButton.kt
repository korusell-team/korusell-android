package net.alienminds.ethnogram.ui.extentions.custom

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.alienminds.ethnogram.ui.theme.AppColor

@Composable
internal fun LikeButton(
    modifier: Modifier = Modifier,
    count: Int?,
    isFavorite: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit
) = AnimatedContent(
    modifier = modifier
        .clickable(
            interactionSource = null,
            indication = null,
            enabled = enabled,
            onClick = { onChange(isFavorite.not()) }
        ),
    targetState = count to isFavorite
) { (count, isFavorite) ->
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = when (isFavorite) {
                true -> Icons.Filled.Favorite
                false -> Icons.Outlined.FavoriteBorder
            },
            contentDescription = null,
            tint = when(isFavorite) {
                true -> AppColor.red500
                false -> AppColor.gray600
            }
        )
        if (count != null && count > 0) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}