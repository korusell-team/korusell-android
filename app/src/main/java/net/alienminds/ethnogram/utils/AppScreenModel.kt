package net.alienminds.ethnogram.utils

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class AppScreenModel: ScreenModel, KoinComponent {

    var loading by mutableStateOf(false)
        protected set

    var error by mutableStateOf<Throwable?>(null)
        protected set

    val errorMessage
        get() = error?.localizedMessage?: error?.message

    fun launchWithLoading(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) = screenModelScope.launch(context){
        loading = true
        block()
        loading = false
    }

    fun <T>Flow<T>.asState(initialValue: T): State<T>{
        val state = mutableStateOf(initialValue)
        screenModelScope.launch {
            collect{ state.value = it }
        }
        return state
    }

}