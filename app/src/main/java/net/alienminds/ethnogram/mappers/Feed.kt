package net.alienminds.ethnogram.mappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.feed.entities.FeedType

val FeedType.displayName
    @Composable get() = when(this){
        FeedType.NEWS -> stringResource(R.string.feed)
        FeedType.EVENT -> stringResource(R.string.event)
        FeedType.PROMO -> stringResource(R.string.ads)
    }

val FeedType.color
    get() = when(this){
        FeedType.NEWS -> Color(0xFF0C79F4)
        FeedType.EVENT -> Color(0xFFFF9700)
        FeedType.PROMO -> Color(0xFF39C561)
    }

val FeedType.emoji
    get() = when(this){
        FeedType.NEWS -> "\uD83D\uDDDE\uFE0F"
        FeedType.EVENT -> "\uD83D\uDCC5"
        FeedType.PROMO -> "\uD83D\uDCE2"
    }