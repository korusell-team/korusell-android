package net.alienminds.ethnogram.ui

import android.Manifest
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import net.alienminds.ethnogram.service.utils.FCMService
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.ui.theme.EthnogramTheme
import net.alienminds.ethnogram.utils.InAppUpdateManager
import net.alienminds.ethnogram.utils.UpdateStatus

@Composable
internal fun RootContent(
    startScreen: Screen,
    updateManager: InAppUpdateManager
){
    val updateState by updateManager.status.collectAsState()
    val snackHostState = remember { SnackbarHostState() }

    SetupNotifications()
    CheckAppUpdate(
        updateManager = updateManager,
        updateState = updateState,
        snackHostState = snackHostState
    )

    EthnogramTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Navigator(startScreen)
            UpdateProgressBar(updateState)
            SnackbarHost(
                modifier = Modifier.statusBarsPadding(),
                hostState = snackHostState,
                snackbar = {
                    Snackbar(
                        snackbarData = it,
                        shape = MaterialTheme.shapes.medium,
                        containerColor = AppColor.brown100,
                        contentColor = AppColor.gray900,
                        actionColor = AppColor.blue900,
                        dismissActionContentColor = AppColor.gray900
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun SetupNotifications(){
    val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null
    LaunchedEffect(perms?.status) {
        if (perms?.status?.isGranted != true){
            perms?.launchPermissionRequest()
        }
        FCMService.subscribeNotifications()
    }
}

@Composable
private fun CheckAppUpdate(
    updateManager: InAppUpdateManager,
    updateState: UpdateStatus,
    snackHostState: SnackbarHostState
){
    LaunchedEffect(updateState) {
        if (updateState is UpdateStatus.ReadyToInstall) {
            val result = snackHostState.showSnackbar(
                message = "Обновление загружено. Установить?",
                actionLabel = "Да",
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                updateManager.confirmInstall()
            }
        }
    }
}

@Composable
fun UpdateProgressBar(updateState: UpdateStatus) {
    if (updateState is UpdateStatus.ProgressFlexible) {
        val percentage = if (updateState.totalBytes > 0) {
            updateState.bytesDownloaded.toFloat() / updateState.totalBytes
        } else 0f
        val safeFraction = if (percentage.isNaN() || percentage.isInfinite()) 0f else percentage.coerceIn(0f, 1f)
        val fraction by animateFloatAsState(
            targetValue = safeFraction,
            animationSpec = tween(100)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .background(Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(AppColor.brown500.copy(alpha = 0.05f))
            )
        }
    }
}