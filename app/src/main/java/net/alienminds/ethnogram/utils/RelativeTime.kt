package net.alienminds.ethnogram.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import org.ocpsoft.prettytime.PrettyTime
import java.time.Instant
import java.util.Locale


private val prettyTime = PrettyTime(Locale("ru"))

val Instant.relativeTime: String
    get() = prettyTime.format(this)

@Composable
fun Instant.rememberRelativeTime(
    sensitive: Long = 10_000L
): State<String> = produceState(relativeTime, this) {
    while (true) {
        value = relativeTime
        delay(sensitive)
    }
}