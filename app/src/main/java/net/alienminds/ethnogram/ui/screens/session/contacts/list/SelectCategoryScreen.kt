package net.alienminds.ethnogram.ui.screens.session.contacts.list

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.ui.extentions.buttons.BackButton
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.list.entities.UserGrouper
import net.alienminds.ethnogram.ui.theme.AppColor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class SelectCategoryScreen(
    private val searchMode: Boolean = false,
    private val searchText: String = ""
): PageTransitionScreen, KoinComponent {

    override val key: ScreenKey
        get() = super.key + ":$searchMode:$searchText"

    override val position: Int
        get() = 2

    private val appCtx by inject<Context>()
    private val grouper by lazy { UserGrouper(appCtx) }

    companion object {

        private var allCategories: List<Category> by mutableStateOf(emptyList())

        var currentCategory by mutableStateOf<Category?>(null)
        var currentSubCategory by mutableStateOf<Category?>(null)

        fun selectCategory(category: Category){
            when(category.isCategory){
                true -> currentCategory = category.takeUnless { it == currentCategory }
                false -> currentSubCategory = category.takeUnless { it == currentSubCategory }?.apply {
                    currentCategory = allCategories.find { it.id == parentId }
                }
            }
            if (category.isCategory){
                currentSubCategory = null
            }
        }

        fun setupAllCategories(categories: List<Category>){
            allCategories = categories
        }
    }


    @Composable
    override fun Content() = Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        val navigator = LocalNavigator.currentOrThrow
        val filteredCategories by remember { derivedStateOf {
            grouper.filteredCategories(allCategories, searchMode, searchText)
        } }
        val categories = filteredCategories.filter { it.isCategory }

        CategoryToolbar(
            modifier = Modifier.statusBarsPadding()
        )
        LazyColumn(
            modifier = Modifier.navigationBarsPadding()
        ){
            items(
                items = categories,
                key = { it.id }
            ){ category ->
                val selected = currentCategory == category
                val subCategories = allCategories.filter { it.parentId == category.id }
                CategoryItem(
                    modifier = Modifier.fillMaxWidth(),
                    category = category,
                    selected = selected,
                    onSelect = { selectCategory(category) }
                )
                AnimatedVisibility(selected && subCategories.isNotEmpty()) {
                    Column(
                        modifier = Modifier.background(AppColor.gray300)
                    ){
                        subCategories.forEach { subCat ->
                            CategoryItem(
                                modifier = Modifier.fillMaxWidth(),
                                category = subCat,
                                selected = currentSubCategory == subCat,
                                onSelect = {
                                    selectCategory(subCat)
                                    navigator.pop()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CategoryItem(
        modifier: Modifier = Modifier,
        category: Category,
        selected: Boolean,
        onSelect: (Category) -> Unit
    ){
        val isSub = category.isSubCategory
        val containerAlpha by animateFloatAsState(when(selected){
            true -> 1f
            false -> 0f
        })
        val containerColor = AppColor.gray400.copy(containerAlpha)
        val textColor by animateColorAsState(when (selected) {
            true -> MaterialTheme.colorScheme.onSurface
            false -> MaterialTheme.colorScheme.onBackground
        })
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(48.dp)
                .background(containerColor)
                .clickable{ onSelect(category) },
            contentAlignment = Alignment.CenterStart
        ) {
            HorizontalDivider(
                modifier = Modifier.align(Alignment.TopCenter)
            )
            Text(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = when(isSub){
                        true -> 32.dp
                        false -> 16.dp
                    }),
                text = "${category.emoji} ${category.title}",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }

    @Composable
    private fun CategoryToolbar(
        modifier: Modifier = Modifier
    ) = Box(
        modifier = modifier
            .heightIn(44.dp)
            .fillMaxWidth()
    ) {
        BackButton(
            modifier = Modifier.align(Alignment.CenterStart),
            text = stringResource(R.string.contacts),
            tint = AppColor.blue600
        )
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(R.string.categories),
            style = MaterialTheme.typography.titleMedium,
            color = AppColor.gray900,
            fontWeight = FontWeight.SemiBold
        )
    }

}