package net.alienminds.ethnogram.data.firestore.executors.base

import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.model.core.FetchState
import net.alienminds.ethnogram.data.model.core.GetRequestExecutor

internal class BaseGetRequestExecutor<T>(
    private val onGet: suspend (fetchMode: FetchMode.GetFetchMode) -> FetchState<T>
): GetRequestExecutor<T> {

    override suspend fun get(fetchMode: FetchMode.GetFetchMode): FetchState<T> =
        onGet(fetchMode)

}