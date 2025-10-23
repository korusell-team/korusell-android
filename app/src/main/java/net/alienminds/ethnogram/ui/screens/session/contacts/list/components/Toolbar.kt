package net.alienminds.ethnogram.ui.screens.session.contacts.list.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import net.alienminds.ethnogram.ui.theme.AppColor


@Composable
fun ContactsListScreen.ContactsToolbar(
    modifier: Modifier = Modifier,
    avatarUrl: String?,
    initials: String,
    searchMode: Boolean,
    isAnonymous: Boolean,
    onChangeSearchMode: (Boolean) -> Unit,
    onOpenCities: () -> Unit,
    onShowProfile: () -> Unit,
    onSignIn: () -> Unit
) = Row(
    modifier = modifier
        .fillMaxWidth()
        .heightIn(44.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
){
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
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
    }

    Text(
        text = stringResource(R.string.contacts),
        style = MaterialTheme.typography.titleMedium,
        color = AppColor.gray900,
        fontWeight = FontWeight.SemiBold
    )

    if (isAnonymous){
        TextButton(
            onClick = onSignIn
        ) {
            Text(
                text = "Войти",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.blue400,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        Avatar(
            modifier = Modifier.size(34.dp),
            model = avatarUrl,
            initials = initials,
            onClick = onShowProfile,
            border = BorderStroke(1.dp, AppColor.gray900),
            textStyle = MaterialTheme.typography.bodyMedium,
            contentScale = ContentScale.Crop
        )
    }
}