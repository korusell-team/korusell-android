package net.alienminds.ethnogram.ui.screens.session.contacts.list.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.ui.extentions.custom.Avatar
import net.alienminds.ethnogram.ui.screens.session.contacts.list.ContactsListScreen
import net.alienminds.ethnogram.ui.screens.session.feed.list.FeedListScreen
import net.alienminds.ethnogram.ui.theme.AppColor


@Composable
fun ContactsListScreen.ContactsToolbar(
    modifier: Modifier = Modifier,
    searchMode: Boolean,
    onChangeSearchMode: (Boolean) -> Unit,
    onOpenCities: () -> Unit,
) = Box(
    modifier = modifier
        .fillMaxWidth()
        .heightIn(44.dp),
    contentAlignment = Alignment.Center
){
    Text(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(vertical = 8.dp),
        text = stringResource(R.string.contacts),
        style = MaterialTheme.typography.titleMedium,
        color = AppColor.gray900,
        fontWeight = FontWeight.SemiBold
    )

    Row(
        modifier = Modifier.align(Alignment.CenterEnd),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        IconButton(
            modifier = Modifier.size(34.dp),
            onClick = onOpenCities
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_mappin_circle),
                tint = AppColor.gray900,
                contentDescription = null
            )
        }
        IconButton(
            modifier = Modifier.size(34.dp),
            onClick = { onChangeSearchMode(searchMode.not()) }
        ) {
            AnimatedContent(searchMode) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = when (it) {
                        true -> painterResource(R.drawable.ic_undo)
                        false -> rememberVectorPainter(Icons.Default.Search)
                    },
                    tint = AppColor.gray900,
                    contentDescription = null
                )
            }
        }
    }

}