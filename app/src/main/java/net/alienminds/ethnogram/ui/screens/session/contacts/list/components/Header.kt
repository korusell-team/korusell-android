package net.alienminds.ethnogram.ui.screens.session.contacts.list.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.ui.theme.AppColor

@Composable
fun ContactsScreenHeader(
    modifier: Modifier = Modifier,
    searchMode: Boolean,
    searchText: String,
    currentCategory: Category?,
    currentSubCategory: Category?,
    categories: List<Category>,
    subCategories: List<Category>,
    onSelectCategory: (Category) -> Unit,
    onSwitchSearchMode: (Boolean) -> Unit,
    onChangeSearch: (String) -> Unit,
    onShowAllCategories: () -> Unit
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp)
){


    val visibleSubCategories by remember(
        searchText, subCategories, currentCategory
    ){ derivedStateOf {
        searchMode.not() &&
        subCategories.isNotEmpty() &&
        currentCategory != null
    } }

    AnimatedVisibility(
        visible = searchMode
    ){
        Box(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
        ){
            BasicTextField(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(end = 64.dp)
                    .fillMaxWidth()
                    .height(34.dp),
                value = searchText,
                onValueChange = onChangeSearch,
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = true,
                cursorBrush = SolidColor(AppColor.indigo200),
                decorationBox = { inner ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = AppColor.gray200,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ){
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Default.Search,
                            tint = AppColor.gray500,
                            contentDescription = null
                        )
                        inner.invoke()
                    }
                }
            )
            TextButton(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterEnd)
                    .height(34.dp),
                onClick = { onSwitchSearchMode(false) }
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = AppColor.lightBlue700
                )
            }

        }
    }

    FiltersCategories(
        showAllIcon = searchMode.not(),
        categories = categories,
        currentCategory = currentCategory,
        onSelect = onSelectCategory,
        onShowAll = onShowAllCategories
    )


    AnimatedVisibility(
        visible = visibleSubCategories
    ) {
        FiltersCategories(
            showAllIcon = false,
            categories = subCategories,
            currentCategory = currentSubCategory,
            onSelect = onSelectCategory,
            onShowAll = onShowAllCategories
        )
    }
}



@Composable
fun FiltersCategories(
    modifier: Modifier = Modifier,
    showAllIcon: Boolean,
    categories: List<Category>,
    currentCategory: Category?,
    onSelect: (Category) -> Unit,
    onShowAll: () -> Unit
){
    val lazyState = rememberLazyListState()
//    val pickerState = rememberAppDialogState()
    LaunchedEffect(currentCategory) {
        runCatching {
            val index = categories.indexOf(currentCategory)
            val min = lazyState.layoutInfo.visibleItemsInfo.minBy { it.index }.index
            val max = lazyState.layoutInfo.visibleItemsInfo.maxBy { it.index }.index
            if (index !in min..max && index >= 0) {
                lazyState.animateScrollToItem(index)
            }
        }
    }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = lazyState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if(showAllIcon)
            openAllItem{ onShowAll() }

        textItems(
            categories = categories,
            current = currentCategory,
            onSelect = onSelect
        )
    }
//    if (showAllIcon) {
//        ChipPickerDialog(
//            state = pickerState,
//            title = dialogTitle,
//            items = categories,
//            itemTitle = { "${it.emoji} ${it.title}" },
//            itemSelected = { it == currentCategory },
//            onSelect = onSelect
//        )
//    }
}

private fun LazyListScope.openAllItem(
    onOpenAll: () -> Unit
) = item {
    TextButton(
        modifier = Modifier
            .height(34.dp)
            .animateItem(),
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp
        ),
        colors = ButtonDefaults.textButtonColors(
            containerColor = AppColor.gray100
        ),
        onClick = onOpenAll
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Default.Menu,
            tint = AppColor.gray700,
            contentDescription = null
        )
        Text(
            text = "Все категории",
            style = MaterialTheme.typography.labelLarge,
            color = AppColor.gray700
        )
    }
}

private fun LazyListScope.textItems(
    categories: List<Category>,
    current: Category?,
    onSelect: (Category) -> Unit
) = items(categories) { category ->
    val isSelected = current?.id == category.id
    TextButton(
        modifier = Modifier
            .height(34.dp)
            .animateItem(),
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp
        ),
        colors = ButtonDefaults.textButtonColors(
            containerColor = when (isSelected) {
                true -> AppColor.blueGray700
                false -> AppColor.gray100
            }
        ),
        onClick = { onSelect(category) }
    ) {
        Text(
            text = "${category.emoji} ${category.title}",
            style = MaterialTheme.typography.labelLarge,
            color = when (isSelected) {
                true -> AppColor.blueGray100
                false -> AppColor.gray700
            }
        )
    }
}