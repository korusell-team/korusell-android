package net.alienminds.ethnogram.data.firestore.executors.base

import net.alienminds.ethnogram.data.model.core.FetchState
import net.alienminds.ethnogram.data.model.core.MutationRequestExecutor

internal class BaseMutationRequestExecutor<T>(
    private val onExecute: suspend () -> FetchState<T>
) : MutationRequestExecutor<T> {

    override suspend fun execute(): FetchState<T> = onExecute()

}