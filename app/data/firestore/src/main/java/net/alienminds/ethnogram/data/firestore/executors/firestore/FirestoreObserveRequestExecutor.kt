package net.alienminds.ethnogram.data.firestore.executors.firestore

import android.util.Log
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SnapshotListenOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.model.core.FetchState
import net.alienminds.ethnogram.data.model.core.ObserveRequestExecutor
import net.alienminds.ethnogram.data.model.core.ObserveState

internal class FirestoreObserveRequestExecutor<T>(
    private val resolveCall: suspend () -> Query,
    private val onEach: suspend (QuerySnapshot) -> Unit = { },
    private val mapper: (QuerySnapshot) -> T,
    private val observeSource: ListenSource
): ObserveRequestExecutor<T> {

    override fun observe(
        fetchMode: FetchMode.ObserveFetchMode
    ): Flow<ObserveState<T>> = channelFlow{
        send(ObserveState.loading())
        val query = runCatching { resolveCall() }.onFailure {
            Log.e("FirestoreObserveRequestExecutor", "Error", it)
            send(ObserveState.error(it))
        }.getOrNull()?: return@channelFlow
        val getExecutor = FirestoreGetRequestExecutor(
            resolveCall = { query },
            onEach = onEach,
            mapper = mapper
        )

        // Initial data
        when(fetchMode){
            is FetchMode.GetFetchMode -> send(ObserveState.Data(getExecutor.get(fetchMode)))
            FetchMode.CacheAndNetwork -> {
                val cache = getExecutor.get(FetchMode.CacheOnly)
                if (cache is FetchState.Success) {
                    send(ObserveState.Data(cache))
                }
                val network = getExecutor.get(FetchMode.NetworkOnly)
                if (network is FetchState.Error && cache is FetchState.Error) {
                    send(ObserveState.error(network.error))
                } else if(network is FetchState.Success){
                    send(ObserveState.Data(network))
                }
            }
        }

        runCatching {
            // Subscribe to changes
            val listenerOptions = SnapshotListenOptions.Builder()
                .setMetadataChanges(MetadataChanges.INCLUDE)
                .setSource(observeSource)
                .build()

            val registration = query.addSnapshotListener(listenerOptions) { snapshot, error ->
                if (error != null) {
                    launch { send(ObserveState.error(error)) }
                }
                if (snapshot != null) {
                    launch {
                        onEach(snapshot)
                        send(ObserveState.success(mapper(snapshot)))
                    }
                }
            }
            awaitClose { registration.remove() }
        }.onFailure {
            Log.e("FirestoreObserveRequestExecutor", "Error", it)
            send(ObserveState.error(it))
        }
    }

}