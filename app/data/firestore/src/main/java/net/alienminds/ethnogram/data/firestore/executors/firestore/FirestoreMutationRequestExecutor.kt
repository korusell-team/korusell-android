package net.alienminds.ethnogram.data.firestore.executors.firestore

import net.alienminds.ethnogram.data.model.core.FetchState
import net.alienminds.ethnogram.data.model.core.MutationRequestExecutor

class FirestoreMutationRequestExecutor<T>(
    private val onExecute: suspend () -> T
): MutationRequestExecutor<T> {

    override suspend fun execute(): FetchState<T> = try{
        FetchState.Success(onExecute())
    } catch (e: Exception){
        FetchState.Error(e)
    }

}