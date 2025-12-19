package net.alienminds.ethnogram.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


@Composable
fun EthnogramTheme(
    isDarkTheme: Boolean = false,
    enableDynamic: Boolean = false,
    content: @Composable () -> Unit,

) {
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build. VERSION_CODES.S && enableDynamic

    val colorScheme = when {
        supportsDynamicColor && isDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
        supportsDynamicColor && !isDarkTheme -> dynamicLightColorScheme(LocalContext.current)
        isDarkTheme -> darkColorScheme()
        else -> lightScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
//    MaterialTheme(
//        colorScheme = LightColorScheme,
//        typography = AppTypography,
//        content = content
//    )
}