package net.alienminds.ethnogram.utils

interface AppLaunchService {
    val isFirstLaunch: Boolean
    fun markLaunched()
}