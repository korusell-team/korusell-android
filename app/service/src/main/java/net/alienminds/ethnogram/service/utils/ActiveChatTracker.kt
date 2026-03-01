package net.alienminds.ethnogram.service.utils

object ActiveChatTracker {
    @Volatile
    var currentChatId: String? = null
}