package net.alienminds.ethnogram.data.model.core

import kotlinx.coroutines.flow.Flow

interface GetRequestExecutor<T> {

    suspend fun get(
        fetchMode: FetchMode.GetFetchMode = FetchMode.CacheFirst
    ): FetchState<T>

}

interface ObserveRequestExecutor<T> {

    fun observe(
        fetchMode: FetchMode.ObserveFetchMode = FetchMode.CacheAndNetwork
    ): Flow<ObserveState<T>>

}

interface QueryRequestExecutor<T>: GetRequestExecutor<T>, ObserveRequestExecutor<T>

interface MutationRequestExecutor<T> {

    suspend fun execute(): FetchState<T>

}