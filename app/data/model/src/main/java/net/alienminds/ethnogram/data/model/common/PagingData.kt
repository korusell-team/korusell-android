package net.alienminds.ethnogram.data.model.common

data class PagingData<T>(
    val items: List<T>,
    val meta: PagingMeta
)

data class PagingMeta(
    val hasNext: Boolean,
    val cursor: Any?
)