package net.alienminds.ethnogram.ui.screens.session.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.ui.extentions.custom.Avatar
import net.alienminds.ethnogram.ui.extentions.custom.UserListItem
import net.alienminds.ethnogram.ui.screens.session.NavBarScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.list.SelectCategoryScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.list.components.FiltersCategories
import net.alienminds.ethnogram.ui.screens.session.map.entities.UserClusterItem
import net.alienminds.ethnogram.ui.screens.session.map.entities.id
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.utils.findActivity
import kotlin.random.Random

object MapScreen: NavBarScreen {

    override val position: Int
        get() = 1

    override val title: @Composable (() -> String)
        get() = { stringResource(R.string.search) }

    override val icon: @Composable (() -> Painter)
        get() = { painterResource(R.drawable.ic_map) }

    override val activeIcon: @Composable (() -> Painter)
        get() = { painterResource(R.drawable.ic_map_fill) }


    private val mapUiSettings
        get() = MapUiSettings(
            compassEnabled = false,
            indoorLevelPickerEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
        )


    private fun readResolve(): Any = MapScreen

    @OptIn(MapsComposeExperimentalApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() = Box{
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow
        val vm = navigator.rememberNavigatorScreenModel { MapModel{ navigator } }
        val state = getBottomSheetScaffoldTwoState()

        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            scaffoldState = state.state,
            sheetContent = { SheetContent(vm) },
            sheetPeekHeight = state.currentPartialHeight,
            sheetDragHandle = { DragHandler(
                modifier = Modifier.clickable{ state.dragHandlerClick() },
                height = state.hiddenHeight
            ) }
        ) {
            PrimaryContent(
                vm = vm,
                onDragMap = { /*if(state.isHidden.not()) scope.launch { state.hide() }*/ },
                onTapCluster = { if (state.isPartiallyExpanded.not()) scope.launch { state.partialExpand() } }
            )
        }
    }

    @Composable
    private fun DragHandler(
        modifier: Modifier = Modifier,
        height: Dp
    ) = Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline)
        )
        HorizontalDivider(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    @OptIn(MapsComposeExperimentalApi::class, ExperimentalPermissionsApi::class)
    @Composable
    private fun PrimaryContent(
        vm: MapModel,
        onDragMap: () -> Unit,
        onTapCluster: () -> Unit
    ) = Box(
        modifier = Modifier.fillMaxSize()
    ){
        val ctx = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val koreaCenter = LatLng(36.0, 128.0)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(koreaCenter, 7f)
        }
        var isMyLocationSynced by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(cameraPositionState.projection?.visibleRegion, cameraPositionState.isMoving) {
            if(vm.currentProjection?.visibleRegion != cameraPositionState.projection?.visibleRegion) {
                vm.currentProjection = cameraPositionState.projection
            }
        }
        LaunchedEffect(cameraPositionState.isMoving) {
            if(cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE){
                onDragMap()
            }
        }

        val gpsPerm = rememberMapPermissionsState()

        fun showMyLocation(){
            if(gpsPerm.allPermissionsGranted.not()){
                if(gpsPerm.shouldShowRationale){
                    Toast.makeText(ctx, "Для определения вашего местоположения необходимо предоставить разрешение на доступ к геоданным", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri? = Uri.fromParts("package", ctx.packageName, null)
                    intent.setData(uri)
                    ctx.startActivity(intent)
                } else {
                    gpsPerm.launchMultiplePermissionRequest()
                }
            } else if (isGpsEnabled(ctx).not()){
                enableGps(ctx)
            } else{
                scope.launch {
                    val location = getMyLocation(ctx)?: return@launch
                    val latLng = LatLng(location.latitude, location.longitude)
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(latLng, 10f)
                    )
                }

            }
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = gpsPerm.allPermissionsGranted,
            ),
            onMapLoaded = {
                if(isMyLocationSynced.not()){
                    showMyLocation()
                    isMyLocationSynced = true
                }
            },
            uiSettings = mapUiSettings,
            mapColorScheme = ComposeMapColorScheme.LIGHT,
            contentPadding = PaddingValues(
                bottom = 54.dp
            ),
            onMapClick = { vm.selectCluster(null) }
        ) {
            Clustering(
                items = vm.mapItems,
                onClusterClick = {
                    onTapCluster()
                    vm.selectCluster(it)
                },
                onClusterItemClick = vm::selectClusterItem,
                clusterContent = {
                    CountCluster(
                        count = it.items.size,
                        active = vm.currentClusterId == it.id
                    )
                },
                clusterItemContent = {
                    AvatarUserItem(it)
                }
            )
        }


//        ZoomButtons(
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(bottom = 64.dp)// Drag handle height
//                .padding(16.dp),
//            onZoomIn = { cameraPositionState.move(CameraUpdateFactory.zoomIn()) },
//            onZoomOut = { cameraPositionState.move(CameraUpdateFactory.zoomOut()) },
//        )
        Column(
            modifier = Modifier.statusBarsPadding()
        ) {
            CategoriesHeader(
                currentCategory = vm.currentCategory,
                currentSubCategory = vm.currentSubCategory,
                categories = vm.categories,
                subCategories = vm.subCategories,
                onSelectCategory = vm::selectCategory,
                onShowAllCategories = { navigator.push(SelectCategoryScreen()) }
            )
            MyLocationButton(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp),
                onClick = ::showMyLocation
            )
        }
    }

    @Composable
    private fun AvatarUserItem(
        item: UserClusterItem,
        avatarSize: Dp = 36.dp
    ){
        val density = LocalDensity.current
        val context = LocalContext.current
        val imageRequest = remember(item.user.image.firstOrNull()){
            ImageRequest.Builder(context)
                .data(item.user.image.firstOrNull())
                .allowHardware(false)// Coil issue workaround, if not set this flag, app may crash when loading images
                .build()
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
            ) {
                Avatar(
                    modifier = Modifier.size(avatarSize),
                    model = imageRequest,
                    initials = item.user.initials,
                    contentScale = ContentScale.Crop,
                    border = BorderStroke(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.background
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    background = MaterialTheme.colorScheme.surfaceContainerLow
                )
            }
            Text(
                text = item.user.fullName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun ColumnScope.SheetContent(vm: MapModel) = LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        item {
            if(vm.currentClusterId == null) {
                Text(
                    text = vm.currentSubCategory?.emojiTitle
                        ?: vm.currentCategory?.emojiTitle
                        ?:  stringResource(R.string.top_10_contacts),
                    autoSize = TextAutoSize.StepBased(16.sp, 24.sp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                )
                if(vm.currentCategory == null && vm.currentSubCategory == null) {
                    Text(
                        text = stringResource(R.string.top_10_locations),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else{
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.selected_contacts),
                        autoSize = TextAutoSize.StepBased(16.sp, 24.sp),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                    )
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ){
                        Text("${vm.sheetItems.size}")
                    }
                }
            }
        }

        if(vm.currentClusterId != null){
            items(vm.sheetItems){ user ->
                UserListItem(
                    modifier = Modifier.fillMaxWidth(),
                    user = user,
                    isFavorite = user.likes.any { vm.me?.uid == it },
                    allCategories = vm.allCategories,
                    allCities = vm.allCities,
                    clickable = vm.isAnonymous.not(),
                    onChangeFavorite = { isFavorite ->
                        user.uid?.let { userId ->
                            vm.changeFavorite(userId, isFavorite)
                        }
                    },
                    onClick = { vm.selectUser(user) }
                )
            }
        } else {
            items(vm.sheetItems.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sizeModifier = when (pair.size) {
                        1 -> Modifier.fillMaxWidth(0.5f)
                        2 -> Modifier.weight(1f, true)
                        else -> Modifier// should not happen
                    }.aspectRatio(0.75f)
                    pair.forEach {
                        UserCard(
                            modifier = sizeModifier,
                            user = it,
                            onClick = { vm.selectUser(it) }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun UserCard(
        modifier: Modifier = Modifier,
        user: User,
        onClick: () -> Unit
    ) = Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClick() }
    ){
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = user.image.firstOrNull(),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColor.black.copy(alpha = 0.1f),
                            AppColor.black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = user.fullName,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp),
                text = user.info?: user.bio.orEmpty(),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall,
                minLines = 2,
                maxLines = 2,
            )
        }

    }

    @Composable
    private fun MyLocationButton(
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) = Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
            )
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onPrimary)
            .border(
                width = 1.dp,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            .clickable(onClick = onClick)
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(8.dp)
                .fillMaxSize(),
            painter = painterResource(R.drawable.ic_my_location),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }

    @Composable
    private fun ZoomButtons(
        modifier: Modifier = Modifier,
        onZoomIn: () -> Unit,
        onZoomOut: () -> Unit,
        shape: Shape = MaterialTheme.shapes.medium
    ) = Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
            )
            .width(48.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.onPrimary)
            .border(
                width = 1.dp,
                shape = shape,
                color = MaterialTheme.colorScheme.outlineVariant
            ),
    ){
        Icon(
            modifier = Modifier
                .clickable(onClick = onZoomIn)
                .padding(8.dp)
                .fillMaxWidth()
                .aspectRatio(1f),
            painter = painterResource(R.drawable.ic_zoom_in),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Icon(
            modifier = Modifier
                .clickable(onClick = onZoomOut)
                .padding(8.dp)
                .fillMaxWidth()
                .aspectRatio(1f),
            painter = painterResource(R.drawable.ic_zoom_out),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }

    @Composable
    private fun CountCluster(
        modifier: Modifier = Modifier,
        count: Int,
        active: Boolean = false
    ) = Box(
        modifier = modifier
            .defaultMinSize(34.dp, 34.dp)
            .background(
                color = when (active) {
                    true -> MaterialTheme.colorScheme.primary
                    false -> MaterialTheme.colorScheme.onPrimary
                },
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = when (active) {
                    true -> MaterialTheme.colorScheme.onPrimary
                    false -> MaterialTheme.colorScheme.outline
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = when(active){
                true -> MaterialTheme.colorScheme.onPrimary
                false -> MaterialTheme.colorScheme.onBackground
            },
            fontWeight = FontWeight.SemiBold
        )
    }

    @Composable
    private fun CategoriesHeader(
        modifier: Modifier = Modifier,
        currentCategory: Category?,
        currentSubCategory: Category?,
        categories: List<Category>,
        subCategories: List<Category>,
        onSelectCategory: (Category) -> Unit,
        onShowAllCategories: () -> Unit
    ) = Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        val visibleSubCategories by remember(
            subCategories, currentCategory
        ){ derivedStateOf {
            subCategories.isNotEmpty() &&
            currentCategory != null
        } }

        FiltersCategories(
            showAllIcon = true,
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

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun rememberMapPermissionsState(): MultiplePermissionsState {
        val permission = rememberMultiplePermissionsState(listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
        return permission
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun getBottomSheetScaffoldTwoState(): BottomSheetScaffoldTwoState {
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current
        val screenHeight = with(density) {
            LocalWindowInfo.current.containerSize.height.toDp()
        }

        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded,
                skipHiddenState = false
            )
        )

        val state = remember { BottomSheetScaffoldTwoState(
            state = bottomSheetScaffoldState,
            hiddenHeight = 32.dp,
            partialHeight = screenHeight / 3,
            scope = scope,
        ) }

        val currentValue = bottomSheetScaffoldState.bottomSheetState.currentValue
        val targetValue = bottomSheetScaffoldState.bottomSheetState.targetValue

        if(currentValue == SheetValue.Expanded || targetValue == SheetValue.Expanded){
            state.currentPartialHeight = state.partialHeight
        }

        LaunchedEffect(currentValue, targetValue) {
            if(currentValue == SheetValue.Hidden){
                state.hide()
            }
        }

        return state
    }


    @OptIn(ExperimentalMaterial3Api::class)
    data class BottomSheetScaffoldTwoState (
        val state: BottomSheetScaffoldState,
        val hiddenHeight: Dp,
        val partialHeight: Dp,
        private val scope: CoroutineScope,
    ){
        private val mux = Mutex()
        var currentPartialHeight by mutableStateOf(partialHeight)

        val isExpanded: Boolean
            get() = state.bottomSheetState.currentValue == SheetValue.Expanded

        val isHidden: Boolean
            get() = (currentPartialHeight == hiddenHeight &&
                    state.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) ||
                    state.bottomSheetState.currentValue == SheetValue.Hidden

        val isPartiallyExpanded: Boolean
            get() = state.bottomSheetState.currentValue == SheetValue.PartiallyExpanded && currentPartialHeight == partialHeight

        suspend fun hide(){
            currentPartialHeight = hiddenHeight
            if(state.bottomSheetState.currentValue == SheetValue.PartiallyExpanded){
                return
            }
            state.bottomSheetState.partialExpand()
        }

        suspend fun partialExpand(){
            currentPartialHeight = partialHeight
            if(state.bottomSheetState.currentValue == SheetValue.PartiallyExpanded){
                return
            }
            state.bottomSheetState.partialExpand()
        }

        suspend fun expand(){
            if(state.bottomSheetState.currentValue == SheetValue.Expanded){
                return
            }
            state.bottomSheetState.expand()
        }

        fun dragHandlerClick(){
            scope.launch {
                val current = state.bottomSheetState.currentValue
                when (current) {
                    SheetValue.Hidden,
                    SheetValue.PartiallyExpanded -> animate(
                        typeConverter = Dp.VectorConverter,
                        initialValue = currentPartialHeight,
                        targetValue = when(currentPartialHeight == hiddenHeight){
                            true -> partialHeight
                            false -> hiddenHeight
                        }
                    ){ value, _ ->
                        currentPartialHeight = value
                    }
                    SheetValue.Expanded -> {
                        currentPartialHeight = partialHeight
                        state.bottomSheetState.partialExpand()
                    }
                }
            }
        }
    }

    fun isGpsEnabled(ctx: Context): Boolean = runCatching {
        val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    }.onFailure {
        it.printStackTrace()
    }.getOrNull() == true

    fun enableGps(ctx: Context) {
        fun tryEnableGps(ctx: Context, exception: ResolvableApiException) {
            runCatching {
                val activity = ctx.findActivity()?: return
                exception.startResolutionForResult(
                    /* activity = */ activity,
                    /* requestCode = */ Random.nextInt()
                )
            }.onFailure {
                it.printStackTrace()
            }
        }
        runCatching {
            val settingsRequest = LocationSettingsRequest.Builder()
//                .addLocationRequest(request)
                .setAlwaysShow(true)
                .build()

            val client = LocationServices.getSettingsClient(ctx)
            client.checkLocationSettings(settingsRequest).addOnSuccessListener {
                // GPS is already enabled
            }.addOnFailureListener {
                if (it is ResolvableApiException) {
                    tryEnableGps(ctx, it)
                } else{
                    it.printStackTrace()
                }
            }
        }
    }

    private suspend fun getMyLocation(ctx: Context): Location? = runCatching {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        } else {
            LocationServices.getFusedLocationProviderClient(ctx).lastLocation.await()
        }
    }.onFailure {
        it.printStackTrace()
    }.getOrNull()

}