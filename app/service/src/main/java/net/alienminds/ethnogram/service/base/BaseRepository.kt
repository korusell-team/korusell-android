package net.alienminds.ethnogram.service.base

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import net.alienminds.ethnogram.service.base.entities.ServiceResult
import kotlin.coroutines.CoroutineContext

abstract class BaseRepository internal constructor(){

    protected val logTag
        get() = "Service/${javaClass.simpleName}"


    protected suspend fun <T>apiQuery(
        block: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        runCatching { block() }.onFailure {
            Log.e(logTag, "Api Query Failed", it)
        }
    }

    protected suspend fun <T>MutableStateFlow<T>.awaitState(
        timeoutMillis: Long = 60_000L,
        condition: (T) -> Boolean = { it != null }
    ): T = withTimeout(timeoutMillis) {
        value.takeIf { condition(it) }
            ?: first { condition(it) }
    }

    @Deprecated("Use apiQuery")
    protected suspend fun <T>withSave(
        context: CoroutineContext = Dispatchers.IO,
        block: suspend () -> T
    ): ServiceResult<T> = withContext(context) {
        try {
            ServiceResult(
                data = block(),
            )
        } catch (e: Exception) {
            Log.e(logTag, "Service request error", e)
            ServiceResult(
                error = e
            )
        }
    }

}