package net.alienminds.ethnogram

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import net.alienminds.ethnogram.ui.RootContent
import net.alienminds.ethnogram.utils.AppContextWrapper
import net.alienminds.ethnogram.utils.AppLaunchServiceImpl

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Black.toArgb()),
            navigationBarStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Black.toArgb())
        )
        setContent { RootContent() }
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(AppContextWrapper.wrap(base))
    }

}