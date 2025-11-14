package net.alienminds.ethnogram.ui.screens.session.feed.list

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.onEach
import net.alienminds.ethnogram.service.auth.AuthRepository
import net.alienminds.ethnogram.service.feed.FeedRepository
import net.alienminds.ethnogram.service.feed.entities.Author
import net.alienminds.ethnogram.service.feed.entities.EventDetails
import net.alienminds.ethnogram.service.feed.entities.Feed
import net.alienminds.ethnogram.service.feed.entities.FeedType
import net.alienminds.ethnogram.service.user.UserRepository
import net.alienminds.ethnogram.utils.AppScreenModel
import org.koin.core.component.inject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private typealias Tabs = FeedListScreen.Tabs
private typealias EventGroup = FeedListScreen.EventGroup

internal class FeedListModel: AppScreenModel() {

    private val authRepo by inject<AuthRepository>()
    private val userRepo by inject<UserRepository>()
    private val feedRepo by inject<FeedRepository>()


    private val _feeds by feedRepo.getFeedsFlow()
        .onEach { it?.fetchAuthors() }
        .asStateWithLoading(emptyList())

    val myId by userRepo.myIdFlow.asState(null)

    var authors by mutableStateOf<Map<String, Author>>(emptyMap())
        private set

    var currentTab by mutableStateOf(FeedListScreen.Tabs.MAIN)

    val mainFeeds by derivedStateOf {
        _feeds?.filter { it.type == FeedType.NEWS || it.type == FeedType.PROMO }.orEmpty()
    }

    val eventFeeds by derivedStateOf {
        _feeds?.filter { it.type == FeedType.EVENT }.orEmpty().groupAndSortEvents()
    }


    fun changeFavorite(
        feedId: String,
        isFavorite: Boolean
    ){
        if (authRepo.isAnonymous){
            return
        }
        launchWithLoading{
            feedRepo.favoriteFeed(feedId, isFavorite)
        }
    }

    private fun List<Feed>.groupAndSortEvents(): List<EventGroup> {
        val newList = mutableListOf<Triple<Instant, String, MutableList<Feed>>>()// date instant, title, list feeds
        val periodFormater = DateTimeFormatter.ofPattern("dd LLLL")
        val singleDayFormater = DateTimeFormatter.ofPattern("dd LLLL / cccc")

        fun LocalDate.remapText(isSingle: Boolean): String{
            val now = LocalDate.now()
            return when{
                this == now -> "Сегодня".plus(" / ${this.formatDayOfMonth()}".takeIf { isSingle }.orEmpty())
                this == now.plusDays(1) -> "Завтра".plus(" / ${this.formatDayOfMonth()}".takeIf { isSingle }.orEmpty())
                else -> when(isSingle) {
                    true -> singleDayFormater.format(this)
                    false -> periodFormater.format(this)
                }
            }
        }

        for (element in this) {
            val eventDetails = element.eventDetails?: continue
            val key = when(eventDetails.isPeriod){
                true -> {
                    val startLocal = eventDetails.startTime?.let {
                        LocalDate.ofInstant(it, ZoneId.systemDefault())
                    }?: continue
                    val endLocal = eventDetails.endTime?.let {
                        LocalDate.ofInstant(it, ZoneId.systemDefault())
                    }?: continue
                    val start = startLocal.remapText(false)
                    val end = endLocal.remapText(false)
                    "$start - $end"
                }
                false -> {
                    val local = eventDetails.startTime?.let {
                        LocalDate.ofInstant(it, ZoneId.systemDefault())
                    }?: continue
                    local.remapText(true)
                }
            }
            newList.find { it.second == key }?.third?.add(element)?: run{
                val day = eventDetails.startTime?.truncatedTo(ChronoUnit.DAYS)?: continue
                val data = Triple(
                    first = day,
                    second = key,
                    third = mutableListOf(element)
                )
                newList.add(data)
            }
        }

        return newList
            .sortedBy { it.first }
            .map { EventGroup(
                title = it.second,
                feeds = it.third
            ) }
    }

    private fun LocalDate.formatDayOfMonth() =
        DateTimeFormatter
            .ofPattern("cccc")
            .format(this)


    private val EventDetails.isPeriod: Boolean
        get(){
            if(startTime != null && endTime != null){
                val startLocal = LocalDate.ofInstant(startTime, ZoneId.systemDefault())
                val endLocal = LocalDate.ofInstant(endTime, ZoneId.systemDefault())
                val startDaysOfEpoch = startLocal.year * startLocal.dayOfYear
                val endDaysOfEpoch = endLocal.year * endLocal.dayOfYear
                return startDaysOfEpoch != endDaysOfEpoch
            } else{
                return false
            }
        }

    private suspend fun List<Feed>.fetchAuthors() = mapNotNull { it.authorId }
        .toTypedArray()
        .also {
            authors = userRepo.getAuthors(authorIds = it).getOrNull().orEmpty()
        }
}