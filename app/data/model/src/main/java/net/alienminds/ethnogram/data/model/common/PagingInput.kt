package net.alienminds.ethnogram.data.model.common

data class PagingInput(
    val cursor: Any? = null,
    val pageLimit: Int = 10
)
