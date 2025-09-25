package net.alienminds.ethnogram.service.utils

import android.content.Context

internal fun Context.clearAppCache() = runCatching {
    cacheDir.deleteRecursively()
}.onFailure {
    it.printStackTrace()
}