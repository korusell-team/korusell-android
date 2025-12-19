package net.alienminds.ethnogram.ui.extentions.custom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.mappers.localName
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.data.entities.City
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.ui.extentions.CircularTextBadge
import net.alienminds.ethnogram.ui.theme.AppColor

@Composable
fun UserListItem(
    modifier: Modifier = Modifier,
    user: User,
    isFavorite: Boolean,
    allCategories: List<Category>,
    allCities: List<City>,
    clickable: Boolean,
    ignorePlus: Boolean = false,
    onChangeFavorite: (Boolean) -> Unit,
    onClick: () -> Unit
){

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = null,
                enabled = clickable,
                onClick = onClick
            )
    ){
        val userCategories = remember(user.categories, allCategories){
            user.categories.mapNotNull{ catId ->
                allCategories.find { it.id == catId }
            }
        }

        val userCities = remember(user.cities, allCities){
            user.cities.mapNotNull { cityId ->
                allCities.find { it.id == cityId }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth()
        ){
            Row {
                Box(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    Avatar(
                        modifier = Modifier.size(52.dp),
                        model = user.image.firstOrNull(),
                        initials = user.initials,
                        contentScale = ContentScale.Crop,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    )
                    if (user.isSponsored && ignorePlus.not()){
                        CircularTextBadge(
                            modifier = Modifier.padding(vertical = 8.dp),
                            text = stringResource(R.string.badge_plus),
                            textAngleDeg = 135f,
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                ){
                    Text(
                        text = remember(user.name, user.surname){ buildString {
                            user.surname?.let { append("$it ") }
                            user.name?.let { append(it) }
                        } },
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColor.blueGray900
                    )
                    Text(
                        text = userCities.joinToString { it.localName },
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColor.gray500
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = user.bio.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColor.gray600,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                }
            }
            LikeButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp),
                count = user.likes.size,
                isFavorite = isFavorite,
                enabled = clickable,
                onChange = onChangeFavorite
            )

        }
        Row(
            modifier = Modifier
                .padding(bottom = 8.dp, top = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){

            userCategories.forEach { cat ->
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.extraSmall
                        )
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = cat.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        HorizontalDivider(Modifier.padding(bottom = 8.dp))
    }
}