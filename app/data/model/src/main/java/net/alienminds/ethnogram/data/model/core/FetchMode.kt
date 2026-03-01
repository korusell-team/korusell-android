package net.alienminds.ethnogram.data.model.core

sealed interface FetchMode{

    sealed interface ObserveFetchMode: FetchMode
    sealed interface GetFetchMode: FetchMode

    object CacheOnly: ObserveFetchMode, GetFetchMode
    object NetworkOnly: ObserveFetchMode, GetFetchMode
    object CacheFirst: ObserveFetchMode, GetFetchMode
    object NetworkFirst: ObserveFetchMode, GetFetchMode
    object CacheAndNetwork: ObserveFetchMode

}