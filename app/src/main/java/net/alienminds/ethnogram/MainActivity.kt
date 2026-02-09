package net.alienminds.ethnogram

import android.app.ComponentCaller
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.ui.RootContent
import net.alienminds.ethnogram.ui.screens.session.contacts.profile.ProfileScreen
import net.alienminds.ethnogram.utils.AppContextWrapper
import net.alienminds.ethnogram.utils.InAppUpdateManager
import net.alienminds.ethnogram.utils.IntentProvider
import net.alienminds.ethnogram.utils.UserStateProvider
import net.alienminds.ethnogram.utils.getScreen
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val userStateProvider by inject<UserStateProvider>()
    private val updateManager by inject<InAppUpdateManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var suitableScreen by mutableStateOf<Screen?>(null)
        splashscreen.setKeepOnScreenCondition { suitableScreen == null }
        setupEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) {
                val userState = userStateProvider.getUserState()
                suitableScreen = userState.getScreen(false)
            }
            suitableScreen?.let {
                RootContent(it, updateManager)
            }
        }

        IntentProvider.setIntent(intent)

        lifecycleScope.launch {
            updateManager.getUpdate().getOrNull()?.let { update ->
                updateManager.startUpdate(update, this@MainActivity)
            }
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        handleIntent(intent)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(AppContextWrapper.wrap(base))
    }

    private fun setupEdgeToEdge() = enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Black.toArgb()),
        navigationBarStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Black.toArgb())
    )

    private fun handleIntent(intent: Intent){
        IntentProvider.setIntent(intent)
    }

}