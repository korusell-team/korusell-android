package net.alienminds.ethnogram.data.firestore.executors.firestore

import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.model.core.FetchState
import net.alienminds.ethnogram.data.model.core.ObserveState
import net.alienminds.ethnogram.data.model.core.QueryRequestExecutor

class FirestoreQueryRequestExecutor<T>(
    private val resolveCall: suspend () -> Query,
    private val onEach: suspend (QuerySnapshot) -> Unit = { },
    private val mapper: (QuerySnapshot) -> T,
    private val observeSource: ListenSource = ListenSource.CACHE
): QueryRequestExecutor<T> {

    private val getExecutor by lazy { FirestoreGetRequestExecutor(
        resolveCall = resolveCall,
        onEach = onEach,
        mapper = mapper
    ) }

    private val observeExecutor by lazy { FirestoreObserveRequestExecutor(
        resolveCall = resolveCall,
        onEach = onEach,
        mapper = mapper,
        observeSource = observeSource
    ) }

    override suspend fun get(
        fetchMode: FetchMode.GetFetchMode
    ): FetchState<T> = getExecutor.get(fetchMode)

    override fun observe(
        fetchMode: FetchMode.ObserveFetchMode
    ): Flow<ObserveState<T>> = observeExecutor.observe(fetchMode)

}