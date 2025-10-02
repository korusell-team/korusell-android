package net.alienminds.ethnogram.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random

abstract class AppScreenModel: ScreenModel, KoinComponent {

    private val loadingStack = mutableStateListOf<String>()//Run id

    var loading: Boolean
        get() = loadingStack.isNotEmpty()
        protected set(value) { when (value) {
            true -> loadingStack.add("manual")
            false -> loadingStack.remove("manual")
        } }

    var error by mutableStateOf<Throwable?>(null)
        protected set

    val errorMessage
        get() = error?.localizedMessage?: error?.message

    fun launchWithLoading(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) = screenModelScope.launch(context){
        val runId = getRunId()
        startLoading(runId)
        block()
        stopLoading(runId)
    }

    fun <T>Flow<T>.asState(initialValue: T): State<T> =
        asMutableState(initialValue)

    fun <T>Flow<T>.asMutableState(initialValue: T): MutableState<T>{
        val state = mutableStateOf(initialValue)
        screenModelScope.launch {
            collect{ state.value = it }
        }
        return state
    }

    fun <T>Flow<T>.asStateWithLoading(initialValue: T): State<T> =
        asMutableStateWithLoading(initialValue)

    fun <T>Flow<T>.asMutableStateWithLoading(initialValue: T): MutableState<T>{
        val state = mutableStateOf(initialValue)
        val runId = getRunId()
        screenModelScope.launch {
            catch {
                error = it
                stopLoading(runId)
            }.onCompletion {
                stopLoading(runId)
            }.onStart {
                startLoading(runId)
            }.collect{
                state.value = it
                stopLoading(runId)
            }
        }
        return state
    }

    protected fun getRunId() = Random.nextInt().toString()

    protected fun startLoading(runId: String){ loadingStack.add(runId) }
    protected fun stopLoading(runId: String){ loadingStack.remove(runId) }
    protected fun isLoading(runId: String){ loadingStack.contains(runId) }



}