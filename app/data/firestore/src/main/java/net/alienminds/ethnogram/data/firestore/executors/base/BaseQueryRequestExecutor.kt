package net.alienminds.ethnogram.data.firestore.executors.base

import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.model.core.FetchState
import net.alienminds.ethnogram.data.model.core.GetRequestExecutor
import net.alienminds.ethnogram.data.model.core.ObserveRequestExecutor
import net.alienminds.ethnogram.data.model.core.ObserveState
import net.alienminds.ethnogram.data.model.core.QueryRequestExecutor
import kotlinx.coroutines.flow.Flow

internal class BaseQueryRequestExecutor<T>(
    private val onGet: suspend (fetchMode: FetchMode.GetFetchMode) -> FetchState<T>,
    private val onObserve: (fetchMode: FetchMode.ObserveFetchMode) -> Flow<ObserveState<T>>
) : QueryRequestExecutor<T>,
    GetRequestExecutor<T> by BaseGetRequestExecutor(onGet),
    ObserveRequestExecutor<T> by BaseObserveRequestExecutor(onObserve)