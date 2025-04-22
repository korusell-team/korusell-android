package net.alienminds.ethnogram.ui.screens.session.edit_profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.mappers.icon
import net.alienminds.ethnogram.mappers.localName
import net.alienminds.ethnogram.mappers.placeholder
import net.alienminds.ethnogram.mappers.title
import net.alienminds.ethnogram.service.users.entities.User
import net.alienminds.ethnogram.ui.extentions.buttons.ActionButton
import net.alienminds.ethnogram.ui.extentions.buttons.BackButton
import net.alienminds.ethnogram.ui.extentions.custom.PageIndicator
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.AppAlertDialog
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.ChipPickerSheet
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.rememberAppDialogState
import net.alienminds.ethnogram.ui.extentions.rememberPhotoPicker
import net.alienminds.ethnogram.ui.extentions.shimmerState
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.utils.AppConst
import kotlin.math.roundToInt

class EditProfileScreen: PageTransitionScreen {

    override val position: Int
        get() = 2

    @Composable
    override fun Content() = Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.current
        val vm = rememberScreenModel { EditProfileViewModel() }

        var isExpanded by remember { mutableStateOf(false) }

        val expandRatio by animateFloatAsState(
            targetValue = when(isExpanded){
                true -> 1f
                false -> 0f
            },
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow
            )
        )

        val scrollState = rememberScrollState()
        val pagerState = rememberPagerState { if (isExpanded) vm.images.size else 1 }

        val statusBarHeight = with(LocalDensity.current){ WindowInsets.statusBars.getTop(this).toDp() }

        val photoPicker = rememberPhotoPicker(
            maxPhoto = 5 - vm.images.size,
            onSelect = vm::addImage
        )

        val categoryPicker = rememberAppDialogState()
        val cityPicker = rememberAppDialogState()

        val alertPublic = rememberAppDialogState()
        val alertNotSave = rememberAppDialogState()


        LaunchedEffect(scrollState.canScrollBackward, vm.images.isEmpty()) {
            if (scrollState.canScrollBackward || vm.images.isEmpty()){
                isExpanded = false
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(
                    state = scrollState
                )
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AvatarBlock(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = statusBarHeight.plus(56.dp).times(1f - expandRatio))
                    .fillMaxWidth(0.4f.plus(0.6f * expandRatio)),
                images = vm.images,
                enabled = vm.loading.not(),
                shape = RoundedCornerShape(100-(100*expandRatio).roundToInt()),
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(
                        alpha = 1f - expandRatio
                    )
                ),
                isExpanded = isExpanded,
                pagerState = pagerState,
                onAddPhoto = { photoPicker.launch() },
                onRemovePhoto = { vm.removeImage() },
                onClick = {
                    if (vm.images.isEmpty()){
                        photoPicker.launch()
                    } else {
                        isExpanded = isExpanded.not()
                        scope.launch { scrollState.animateScrollTo(0) }
                    }
                }
            )

            PublicBlock(
                modifier = Modifier.padding(top = 16.dp),
                isPublic = vm.isPublic,
                loading = vm.loading,
                onCheck = { vm.isPublic = it }
            )

            PhoneNumberBlock(
                isAvailablePhone = vm.isAvailablePhone,
                phone = vm.phone,
                loading = vm.loading,
                onChange = { vm.isAvailablePhone = it }
            )

            NameBlock(
                name = vm.name.orEmpty(),
                surname = vm.surname.orEmpty(),
                isErrorName = vm.isErrorName,
                isErrorSurname = vm.isErrorSurname,
                loading = vm.loading,
                onChange = { name, surname ->
                    vm.name = name.take(30)
                    vm.surname = surname.take(30)
                }
            )

            CategoryCityBlock(
                category = vm.categories?.takeIf { it.isNotEmpty() }?.joinToString { it.title },
                city = vm.cities?.takeIf { it.isNotEmpty() }?.joinToString { it.localName },
                isErrorCategory = vm.isErrorCategory,
                onClickCategory = categoryPicker::show,
                onClickCity = cityPicker::show
            )

            BioBlock(
                bio = vm.bio.orEmpty(),
                loading = vm.loading,
                onChange = { vm.bio = it }
            )

            InfoBlock(
                info = vm.info.orEmpty(),
                loading = vm.loading,
                onChange = { vm.info = it }
            )

            SocialsBlock(
                linksMap = vm.linksMap,
                loading = vm.loading,
            )


            Spacer(
                Modifier
                    .navigationBarsPadding()
                    .height(48.dp)
            )
        }
        Toolbar(
            modifier = Modifier.statusBarsPadding(),
            canAdd = vm.images.size < AppConst.MAX_IMAGES,
            canRemove = vm.images.isNotEmpty(),
            edited = vm.edited,
            isExpanded = isExpanded,
            enabled = vm.loading.not(),
            onAddPhoto = { photoPicker.launch() },
            onRemovePhoto = { vm.removeImage(vm.images.getOrNull(pagerState.currentPage)) },
            onSavePressed = { when(vm.canSave){
                true -> vm.saveUser()
                false -> alertPublic.show()
            } },
            onBackPressed = {
                when(vm.edited){
                    true -> alertNotSave.show()
                    false -> navigator?.pop()
                }
            }
        )


        ChipPickerSheet(
            modifier = Modifier.statusBarsPadding(),
            state = categoryPicker,
            itemsMap = vm.allCategoriesGrouped,
            groupTitle = { "${it.emoji} ${it.title}" },
            itemTitle = { "${it.emoji} ${it.title}" },
            itemSelected = { vm.categories?.contains(it)?: false },
            onSelect = { _, item ->
                vm.selectCategory(item)
            }
        )
        ChipPickerSheet(
            modifier = Modifier.statusBarsPadding(),
            state = cityPicker,
            itemsMap = mapOf("" to vm.allCities.orEmpty()),
            itemTitle = { it.localName },
            itemSelected = { vm.cities?.run { any { it.id == 0L } || contains(it) } == true },
            onSelect = { _, item ->
                vm.selectCity(item)
            }
        )
        AppAlertDialog(
            state = alertPublic,
            title = stringResource(R.string.alert_public_title),
            text = stringResource(R.string.alert_public_body),
        )
        AppAlertDialog(
            state = alertNotSave,
            title = "Выйти без сохранения?",
            text = "Все данные будут утеряны. Вы уверены что хотите выйти?",
            confirmColor = MaterialTheme.colorScheme.error,
            confirmText = "Да, Выйти",
            dismissText = "Остаться",
            onConfirm = { navigator?.pop() }
        )
    }

    @Composable
    private fun Toolbar(
        modifier: Modifier = Modifier,
        edited: Boolean,
        canAdd: Boolean,
        canRemove: Boolean,
        isExpanded: Boolean,
        enabled: Boolean,
        onRemovePhoto: () -> Unit,
        onAddPhoto: () -> Unit,
        onSavePressed: () -> Unit,
        onBackPressed: () -> Unit
    ) = Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(48.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        BackButton(
            onClick = onBackPressed,
            handleSystemButton = true,
            enabled = enabled
        )
        AnimatedContent(
            isExpanded
        ) {
            if (it) {
                var showMenu by remember { mutableStateOf(false) }
                ActionButton(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.MoreVert,
                    onClick = { showMenu = true }
                )
                ActionMenu(
                    visibility = showMenu && enabled,
                    showSave = true,
                    canAdd = canAdd,
                    canRemove = canRemove,
                    canSave = edited,
                    onAddPhoto = onAddPhoto,
                    onRemovePhoto = onRemovePhoto,
                    onSave = onSavePressed,
                    onDismissRequest = { showMenu = false },
                )
            } else {
                TextButton(
                    onClick = onSavePressed,
                    enabled = edited && enabled
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun AvatarBlock(
        modifier: Modifier = Modifier,
        images: List<String>,
        enabled: Boolean,
        shape: Shape = CircleShape,
        border: BorderStroke,
        isExpanded: Boolean,
        pagerState: PagerState,
        onAddPhoto: () -> Unit,
        onRemovePhoto: () -> Unit,
        onClick: () -> Unit
    ){
        var visibleMenu by remember { mutableStateOf(false)}

        Box(
            modifier = modifier
                .clip(shape)
                .border(border, shape)
                .aspectRatio(1f)
                .combinedClickable(
                    enabled = enabled,
                    onLongClick = { visibleMenu = true && isExpanded.not() && images.isNotEmpty() },
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ){
            if (images.isEmpty()) {
                Icon(
                    modifier = Modifier.fillMaxSize(0.3f),
                    painter = painterResource(R.drawable.ic_add_photo),
                    tint = MaterialTheme.colorScheme.outline,
                    contentDescription = null
                )
            }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = images.getOrNull(it),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            if (isExpanded){
                PageIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    pagerState = pagerState
                )
            }
            ActionMenu(
                modifier = Modifier.fillMaxWidth(0.4f),
                visibility = visibleMenu && enabled,
                canAdd = images.size < AppConst.MAX_IMAGES,
                canRemove = images.isNotEmpty(),
                onAddPhoto = onAddPhoto,
                onRemovePhoto = onRemovePhoto,
                onDismissRequest = { visibleMenu = false }
            )
        }
    }

    @Composable
    private fun ActionMenu(
        modifier: Modifier = Modifier,
        showSave: Boolean = false,
        canSave: Boolean = true,
        canAdd: Boolean = true,
        canRemove: Boolean = true,
        visibility: Boolean,
        onAddPhoto: () -> Unit,
        onRemovePhoto: () -> Unit,
        onSave: () -> Unit = {  },
        onDismissRequest: () -> Unit
    ) = DropdownMenu(
        modifier = modifier,
        offset = DpOffset(0.dp, 8.dp),
        expanded = visibility,
        onDismissRequest = onDismissRequest
    ) {

        @Composable
        fun Item(
            title: String,
            icon: Painter,
            enable: Boolean,
            onClick: () -> Unit
        ) = DropdownMenuItem(
            text = {  Text(title) },
            leadingIcon = { Icon(
                painter = icon,
                contentDescription = null
            ) },
            enabled = enable,
            onClick = {
                onDismissRequest()
                onClick()
            }
        )

        Item(
            title = stringResource(R.string.add_photo),
            icon = painterResource(R.drawable.ic_add_photo),
            enable = canAdd,
            onClick = onAddPhoto
        )
        Item(
            title = stringResource(R.string.remove_photo),
            icon = rememberVectorPainter(Icons.Default.Delete),
            enable = canRemove,
            onClick = onRemovePhoto
        )
        if (showSave) {
            Item(
                title = stringResource(R.string.save),
                icon = rememberVectorPainter(Icons.Default.Done),
                enable = canSave,
                onClick = onSave
            )
        }
    }

    @Composable
    private fun PublicBlock(
        modifier: Modifier = Modifier,
        isPublic: Boolean,
        loading: Boolean,
        onCheck: (Boolean) -> Unit
    ) = Column(
        modifier = modifier
    ){
        DefaultCard(
            modifier = Modifier.shimmerState(loading)
        ){
            SwitchItem(
                modifier = Modifier,
                title = stringResource(R.string.public_account),
                checked = isPublic,
                enabled = loading.not(),
                onCheckedChange = onCheck,
                icon = when(isPublic){
                    true -> painterResource(R.drawable.ic_visibility_on)
                    false -> painterResource(R.drawable.ic_visibility_off)
                }
            )


        }
        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 36.dp)
                .fillMaxWidth(),
            text = stringResource(R.string.public_account_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    @Composable
    private fun PhoneNumberBlock(
        modifier: Modifier = Modifier,
        isAvailablePhone: Boolean,
        loading: Boolean,
        phone: String,
        onChange: (Boolean) -> Unit
    ) = Column(
        modifier = modifier
    ){
        DefaultCard(
            modifier = Modifier.shimmerState(loading)
        ){
            SwitchItem(
                title = stringResource(R.string.show_phone_number),
                icon = rememberVectorPainter(Icons.Default.Phone),
                checked = isAvailablePhone,
                enabled = loading.not(),
                onCheckedChange = { onChange(it) }
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            OutlinedTextField(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                value = phone,
                enabled = loading.not(),
                readOnly = true,
                onValueChange = {  },
                label = { Text(stringResource(R.string.phone_number)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                )
            )
        }

        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 36.dp)
                .fillMaxWidth(),
            text = stringResource(R.string.show_phone_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    @Composable
    private fun NameBlock(
        modifier: Modifier = Modifier,
        name: String,
        surname: String,
        loading: Boolean,
        isErrorName: Boolean,
        isErrorSurname: Boolean,
        onChange: (name: String, surname: String) -> Unit
    ) = Column(
        modifier = modifier
    ){
        DefaultCard(
            modifier = Modifier.shimmerState(loading)
        ){
            OutlinedTextField(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                value = name,
                onValueChange = { onChange(it, surname) },
                label = { Text(stringResource(R.string.name)) },
                isError = isErrorName,
                enabled = loading.not(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            OutlinedTextField(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                value = surname,
                onValueChange = { onChange(name, it) },
                label = { Text(stringResource(R.string.surname)) },
                isError = isErrorSurname,
                enabled = loading.not(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )
        }
        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 36.dp)
                .fillMaxWidth(),
            text = stringResource(R.string.input_name_and_surname),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    @Composable
    private fun CategoryCityBlock(
        modifier: Modifier = Modifier,
        category: String?,
        city: String?,
        isErrorCategory: Boolean,
        onClickCategory: () -> Unit,
        onClickCity: () -> Unit
    ) = Column(
        modifier = modifier
    ){
        DefaultCard {
            ClickableItem(
                title = stringResource(R.string.category),
                value = category?: stringResource(R.string.not_specified),
                isError = isErrorCategory,
                onClick = onClickCategory
            )
            HorizontalDivider()
            ClickableItem(
                title = stringResource(R.string.city),
                value = city?: stringResource(R.string.not_specified),
                onClick = onClickCity
            )
        }
        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 36.dp)
                .fillMaxWidth(),
            text = stringResource(R.string.category_city_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    @Composable
    private fun BioBlock(
        modifier: Modifier = Modifier,
        bio: String,
        loading: Boolean,
        onChange: (String) -> Unit
    ) = Column(
        modifier = modifier
    ){
        DefaultCard(
            modifier = Modifier.shimmerState(loading)
        ){
            OutlinedTextField(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                value = bio,
                onValueChange = {
                    if (it.length <= 80) {
                        onChange(it)
                    }
                },
                enabled = loading.not(),
                label = { Text(stringResource(R.string.bio)) },
                placeholder = { Text(stringResource(R.string.bio_placeholder)) }
            )
        }
        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 36.dp)
                .fillMaxWidth(),
            text = stringResource(R.string.bio_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    @Composable
    private fun InfoBlock(
        modifier: Modifier = Modifier,
        info: String,
        loading: Boolean,
        onChange: (String) -> Unit
    ) = Column(
        modifier = modifier
    ){
        DefaultCard(
            modifier = Modifier.shimmerState(loading)
        ){
            OutlinedTextField(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                value = info,
                onValueChange = onChange,
                minLines = 5,
                maxLines = 10,
                enabled = loading.not(),
                label = { Text(stringResource(R.string.info)) },
                placeholder = { Text(stringResource(R.string.info_placeholder)) }
            )
        }
        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 36.dp)
                .fillMaxWidth(),
            text = stringResource(R.string.info_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    @Composable
    private fun SocialsBlock(
        modifier: Modifier = Modifier,
        linksMap: SnapshotStateMap<User.LinkType, String?>,
        loading: Boolean,
    ) = Column(
        modifier = modifier
    ){
        val sortedLinks by remember { derivedStateOf {
            linksMap.toSortedMap(compareBy { it.ordinal })
        } }


        DefaultCard(
            modifier = Modifier.shimmerState(loading)
        ){
            sortedLinks.forEach { (type, value) ->
                LinkItem(
                    title = type.title,
                    icon = type.icon,
                    placeholder = type.placeholder,
                    value = value.orEmpty(),
                    enabled = loading.not(),
                    onValueChange = { linksMap[type] = it }
                )
                if (type != sortedLinks.keys.last()) {
                    HorizontalDivider()
                }
            }
        }

        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 36.dp)
                .fillMaxWidth(),
            text = stringResource(R.string.socials_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun LinkItem(
        icon: Painter,
        title: String,
        placeholder: String,
        value: String,
        enabled: Boolean,
        onValueChange: (String) -> Unit
    ) = Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Image(
            modifier = Modifier
                .padding(start = 26.dp)
                .height(24.dp),
            painter = icon,
            contentScale = ContentScale.FillHeight,
            contentDescription = null
        )

        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        )

        BasicTextField(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .imeNestedScroll(),
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            singleLine = true,
            enabled = enabled,
            decorationBox = { content ->
                Box(
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColor.gray400
                        )
                    }
                    content()
                }
            }
        )
    }
    
    @Composable
    private fun ClickableItem(
        modifier: Modifier = Modifier,
        title: String,
        value: String,
        isError: Boolean = false,
        onClick: () -> Unit
    ) = DefaultItem(
        modifier = modifier.clickable(onClick = onClick),
        title = title,
        supportingContent = { Text(
            text = value,
            color = when(isError){
                true -> MaterialTheme.colorScheme.error
                false -> LocalContentColor.current
            }
        ) },
        trailingContent = { Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null
        ) }
    )

    @Composable
    private fun SwitchItem(
        modifier: Modifier = Modifier,
        title: String,
        checked: Boolean,
        enabled: Boolean = true,
        icon: Painter? = null,
        onCheckedChange: (Boolean) -> Unit
    ) = DefaultItem(
        modifier = modifier,
        title = title,
        leadingContent = {
            icon?.let {
                Icon(
                    painter = icon,
                    contentDescription = null
                )
            }
                         },
        trailingContent = {
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange
            )
        }
    )

    @Composable
    private fun DefaultItem(
        modifier: Modifier,
        title: String,
        overlineContent: @Composable (() -> Unit)? = null,
        supportingContent: @Composable (() -> Unit)? = null,
        leadingContent: @Composable (() -> Unit)? = null,
        trailingContent: @Composable (() -> Unit)? = null
    ) = ListItem(
        modifier = modifier,
        headlineContent = { Text(title) },
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,

        )
    )

    @Composable
    private fun DefaultCard(
        modifier: Modifier = Modifier,
        content: @Composable ColumnScope.() -> Unit
    ) = ElevatedCard(
        modifier = modifier.padding(horizontal = 24.dp),
        content = content
    )

}