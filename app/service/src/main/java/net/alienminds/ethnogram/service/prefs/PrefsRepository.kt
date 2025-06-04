package net.alienminds.ethnogram.service.prefs

import android.content.Context
import net.alienminds.ethnogram.service.prefs.delegates.BooleanPreference

class PrefsRepository internal constructor(
    context: Context
){

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isFirstLaunch: Boolean by BooleanPreference(prefs, KEY_FIRST_LAUNCH, true)

    companion object{
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
    }

}