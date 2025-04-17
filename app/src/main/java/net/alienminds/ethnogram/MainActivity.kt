package net.alienminds.ethnogram

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.ui.RootContent
import net.alienminds.ethnogram.utils.AppContextWrapper
import net.alienminds.ethnogram.utils.getSuitableScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var suitableScreen by mutableStateOf<Screen?>(null)
        splashscreen.setKeepOnScreenCondition { suitableScreen == null }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Black.toArgb()),
            navigationBarStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Black.toArgb())
        )

        setContent {
            LaunchedEffect(Unit) {
                suitableScreen = getSuitableScreen(this@MainActivity)
            }
            suitableScreen?.let {
                RootContent(it)
            }
        }
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(AppContextWrapper.wrap(base))
    }

}