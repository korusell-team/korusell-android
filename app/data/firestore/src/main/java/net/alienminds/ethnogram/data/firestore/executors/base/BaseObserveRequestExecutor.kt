package net.alienminds.ethnogram.data.firestore.executors.base

import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.model.core.ObserveRequestExecutor
import net.alienminds.ethnogram.data.model.core.ObserveState
import kotlinx.coroutines.flow.Flow


internal class BaseObserveRequestExecutor<T>(
    private val onObserve: (fetchMode: FetchMode.ObserveFetchMode) -> Flow<ObserveState<T>>
) : ObserveRequestExecutor<T> {

    override fun observe(fetchMode: FetchMode.ObserveFetchMode): Flow<ObserveState<T>> =
        onObserve(fetchMode)
}