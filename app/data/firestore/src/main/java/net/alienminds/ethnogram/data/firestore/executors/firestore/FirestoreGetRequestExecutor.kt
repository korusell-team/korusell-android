package net.alienminds.ethnogram.data.firestore.executors.firestore

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import net.alienminds.ethnogram.data.model.core.FetchMode
import net.alienminds.ethnogram.data.model.core.FetchState
import net.alienminds.ethnogram.data.model.core.GetRequestExecutor

internal class FirestoreGetRequestExecutor<T>(
    private val resolveCall: suspend () -> Query,
    private val onEach: suspend (QuerySnapshot) -> Unit = { },
    private val mapper: (QuerySnapshot) -> T,
): GetRequestExecutor<T>{

    override suspend fun get(
        fetchMode: FetchMode.GetFetchMode
    ): FetchState<T> = try {
        val query = resolveCall()
        val response = when(fetchMode){
            FetchMode.CacheFirst -> runCatching {
                getFromSource(query, Source.CACHE)
            }.getOrElse {
                getFromSource(query, Source.SERVER)
            }
            FetchMode.CacheOnly -> getFromSource(query, Source.CACHE)
            FetchMode.NetworkFirst -> getFromSource(query, Source.DEFAULT)
            FetchMode.NetworkOnly -> getFromSource(query, Source.SERVER)
        }
        onEach(response)
        FetchState.Success(mapper(response))
    } catch (e: Exception){
        Log.e("FirestoreGetRequestExecutor", "Error", e)
        FetchState.Error(e)
    }

    private suspend fun getFromSource(query: Query, source: Source): QuerySnapshot {
        val result = query.get(source).await()
        if (result.isEmpty){
            throw IllegalStateException("data is empty")
        }
        return result
    }

}