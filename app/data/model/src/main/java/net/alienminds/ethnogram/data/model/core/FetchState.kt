package net.alienminds.ethnogram.data.model.core

sealed class FetchState<out T>{
    data class Success<T>(val data: T): FetchState<T>()
    data class Error(val error: Throwable): FetchState<Nothing>()

    fun getOrNull(): T? = when(this){
        is Success -> data
        is Error -> null
    }

    fun exceptionOrNull(): Throwable? = when(this){
        is Success -> null
        is Error -> error
    }

    fun getOrFail(): T = when(this){
        is Success -> data
        is Error -> throw error
    }

    inline fun fold(
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) = apply { when(this){
        is Success -> onSuccess(data)
        is Error -> onError(error)
    } }

    inline fun onError(
        onError: (Throwable) -> Unit
    ) = apply { fold(
        onSuccess = {},
        onError = onError
    )  }

    inline fun onSuccess(
        onSuccess: (T) -> Unit
    ) = apply { fold(
        onSuccess = onSuccess,
        onError = {}
    ) }

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error
}