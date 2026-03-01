package net.alienminds.ethnogram.data.model.core

sealed class ObserveState<out T>{

    object Loading: ObserveState<Nothing>()

    data class Data<T>(
        val value: FetchState<T>
    ): ObserveState<T>()

    inline fun fold(
        onLoading: () -> Unit,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) = when(this){
        Loading -> onLoading()
        is Data -> value.fold(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    val isLoading: Boolean
        get() = this is Loading

    val isSuccess: Boolean
        get() = this is Data && value is FetchState.Success

    val isError: Boolean
        get() = this is Data && value is FetchState.Error

    fun dataOrNull() =
        (this as? Data<out T>)?.value

    fun errorOrNull() =
        (this as? Data<out T>)?.value as? FetchState.Error

    companion object{
        fun <T> loading(): ObserveState<T> = Loading
        fun <T> success(value: T): ObserveState<T> = Data(FetchState.Success(value))
        fun <T> error(error: Throwable): ObserveState<T> = Data(FetchState.Error(error))
    }
}