package net.alienminds.ethnogram.ui

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.ui.theme.EthnogramTheme
import net.alienminds.ethnogram.utils.InAppUpdateManager

@Composable
internal fun RootContent(
    startScreen: Screen,
    updateManager: InAppUpdateManager
){
    val updateState by remember { derivedStateOf { updateManager.updateState } }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(updateState) {
        if (updateState is InAppUpdateManager.UpdateAppUiState.Ready) {
            val result = snackbarHostState.showSnackbar(
                message = "Обновление загружено. Установить?",
                actionLabel = "Да",
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                updateManager.completeFlexibleUpdate()
            }
        }
    }

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
                hostState = snackbarHostState,
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

@Composable
fun UpdateProgressBar(updateState: InAppUpdateManager.UpdateAppUiState) {
    if (updateState is InAppUpdateManager.UpdateAppUiState.Downloading) {
        val fraction by animateFloatAsState(
            targetValue = updateState.percentage.coerceIn(0f, 1f),
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