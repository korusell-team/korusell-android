package net.alienminds.ethnogram.service.prefs.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class BooleanPreference(
    private val prefs: SharedPreferences,
    private val key: String,
    private val defaultValue: Boolean
): ReadWriteProperty<Any?, Boolean>{

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
        prefs.getBoolean(key, defaultValue)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) =
        prefs.edit { putBoolean(key, value) }
}