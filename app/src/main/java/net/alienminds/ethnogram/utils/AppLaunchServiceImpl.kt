package net.alienminds.ethnogram.utils

import android.content.Context
import androidx.core.content.edit

class AppLaunchServiceImpl(private val context: Context) : AppLaunchService {
    private val prefs = context.getSharedPreferences("app_launch_prefs", Context.MODE_PRIVATE)

    override val isFirstLaunch: Boolean
        get() = prefs.getBoolean("is_first_launch", true)

    override fun markLaunched() {
        prefs.edit() { putBoolean("is_first_launch", false) }
    }
}
