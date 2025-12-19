package net.alienminds.ethnogram.utils

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object IntentProvider {

    private val _intent = MutableStateFlow<Intent?>(null)
    val intent = _intent.asStateFlow()

    internal fun setIntent(newIntent: Intent?) {
        _intent.value = newIntent
    }

    fun consumedIntent(): Intent? {
        val currentIntent = _intent.value
        _intent.value = null
        return currentIntent
    }

    fun clearIntent() {
        _intent.value = null
    }

}