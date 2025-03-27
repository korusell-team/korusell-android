package net.alienminds.ethnogram.mappers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.users.entities.User
import net.alienminds.ethnogram.service.users.entities.User.Fields
import net.alienminds.ethnogram.service.users.entities.User.LinkType
import net.alienminds.ethnogram.utils.openLinkExternal
import net.alienminds.ethnogram.utils.openLinkInApp

val User.Link.title
    @Composable get() = type.title

val LinkType.title
    @Composable get() = when(this){
        LinkType.TELEGRAM -> stringResource(R.string.telegram)
        LinkType.INSTAGRAM -> stringResource(R.string.instagram)
        LinkType.WHATS_APP -> stringResource(R.string.whatsapp)
        LinkType.THREADS -> stringResource(R.string.threads)
        LinkType.YOUTUBE -> stringResource(R.string.youtube)
        LinkType.WEB_SITE -> stringResource(R.string.web_site)
        LinkType.FACEBOOK -> stringResource(R.string.facebook)
        LinkType.KAKAO -> stringResource(R.string.kakao)
        LinkType.TIKTOK -> stringResource(R.string.tiktok)
        LinkType.LINKED_IN -> stringResource(R.string.linked_in)
        LinkType.TWITTER -> stringResource(R.string.twitter)
    }

val User.Link.placeholder
    @Composable get() = type.placeholder

val LinkType.placeholder
    @Composable get() = when(this){
        LinkType.INSTAGRAM,
        LinkType.THREADS,
        LinkType.TWITTER -> stringResource(R.string.nickname)
        LinkType.YOUTUBE -> stringResource(R.string.channel)
        LinkType.WEB_SITE -> stringResource(R.string.link_placeholder)
        LinkType.FACEBOOK,
        LinkType.TIKTOK,
        LinkType.KAKAO -> stringResource(R.string.id)
        LinkType.TELEGRAM -> stringResource(R.string.telegram_placeholder)
        LinkType.LINKED_IN -> stringResource(R.string.profile)
        LinkType.WHATS_APP -> stringResource(R.string.phone_placeholder)
    }

val User.Link.roundIcon
    @Composable get() = type.roundIcon

val LinkType.roundIcon
    @Composable get() = when(this){
        LinkType.TELEGRAM -> painterResource(R.drawable.ic_telegram_round)
        LinkType.INSTAGRAM -> painterResource(R.drawable.ic_instagram_round)
        LinkType.WHATS_APP -> painterResource(R.drawable.ic_whatsapp_round)
        LinkType.THREADS -> painterResource(R.drawable.ic_threads_round)
        LinkType.YOUTUBE -> painterResource(R.drawable.ic_youtube_round)
        LinkType.WEB_SITE -> painterResource(R.drawable.ic_link_round)
        LinkType.FACEBOOK -> painterResource(R.drawable.ic_facebook_round)
        LinkType.KAKAO -> painterResource(R.drawable.ic_kakao_round)
        LinkType.TIKTOK -> painterResource(R.drawable.ic_tiktok_round)
        LinkType.LINKED_IN -> painterResource(R.drawable.ic_linkedin_round)
        LinkType.TWITTER -> painterResource(R.drawable.ic_twitter_round)
    }


val User.Link.icon
    @Composable get() = type.icon

val LinkType.icon
    @Composable get() = when(this){
        LinkType.TELEGRAM -> painterResource(R.drawable.ic_telegram)
        LinkType.INSTAGRAM -> painterResource(R.drawable.ic_instagram)
        LinkType.WHATS_APP -> painterResource(R.drawable.ic_whatsapp)
        LinkType.THREADS -> painterResource(R.drawable.ic_threads)
        LinkType.YOUTUBE -> painterResource(R.drawable.ic_youtube)
        LinkType.WEB_SITE -> painterResource(R.drawable.ic_www)
        LinkType.FACEBOOK -> painterResource(R.drawable.ic_facebook)
        LinkType.KAKAO -> painterResource(R.drawable.ic_kakao)
        LinkType.TIKTOK -> painterResource(R.drawable.ic_tiktok)
        LinkType.LINKED_IN -> painterResource(R.drawable.ic_linkedin)
        LinkType.TWITTER -> painterResource(R.drawable.ic_twitter)
    }

val User.Link.displayValue
    get() = when(type){
        LinkType.TELEGRAM,
        LinkType.INSTAGRAM -> "@$value"
        else -> value
    }

val LinkType.field
    get() = when(this){
        LinkType.INSTAGRAM -> Fields.INSTAGRAM
        LinkType.TELEGRAM -> Fields.TELEGRAM
        LinkType.YOUTUBE -> Fields.YOUTUBE
        LinkType.WEB_SITE -> Fields.WEB_SITE
        LinkType.FACEBOOK -> Fields.FACEBOOK
        LinkType.TIKTOK -> Fields.TIKTOK
        LinkType.KAKAO -> Fields.KAKAO
        LinkType.WHATS_APP -> Fields.WHATS_APP
        LinkType.LINKED_IN -> Fields.LINKED_IN
        LinkType.THREADS -> Fields.THREADS
        LinkType.TWITTER -> Fields.TWITTER
    }

fun User.Link.copyToClipboard(context: Context){
    context.copyToClipboard(type.name, displayValue)
}

fun User.Link.open(context: Context) = with(context){
    when(type){
        LinkType.INSTAGRAM -> openInstagram(value)
        LinkType.TELEGRAM -> openTelegram(value)
        LinkType.YOUTUBE -> openYoutube(value)
        LinkType.WEB_SITE -> openWebLink(value)
        LinkType.FACEBOOK -> openFacebook(value)
        LinkType.TIKTOK -> openTiktok(value)
        LinkType.WHATS_APP -> openWhatsApp(value)
        LinkType.LINKED_IN -> openLinkedIn(value)
        LinkType.THREADS -> openThreads(value)
        LinkType.TWITTER -> openTwitter(value)
        else -> copyToClipboard(type.name, value)
    }
}


private fun Context.openTwitter(value: String) = openLinkInApp(
    link = "https://twitter.com/$value",
    appPackage = "com.twitter.android"
)

private fun Context.openThreads(value: String) = openLinkInApp(
    link = "https://www.threads.net/@$value",
    appPackage = "com.instagram.barcelona"
)

private fun Context.openLinkedIn(value: String) = openLinkInApp(
    link = "https://www.linkedin.com/in/$value",
    appPackage = "com.linkedin.android"
)

private fun Context.openTiktok(value: String) = openLinkInApp(
    link = "https://www.tiktok.com/@$value",
    appPackage = "com.zhiliaoapp.musically"
)

private fun Context.openWebLink(value: String) = openLinkExternal(
    url = "https://$value"
)

private fun Context.openYoutube(value: String) = openLinkInApp(
    link = "https://www.youtube.com/@$value",
    appPackage = "com.google.android.youtube",
)

private fun Context.openFacebook(value: String) = openLinkInApp(
    link = "fb://page/$value",
    webLink = "https://facebook.com/$value",
    appPackage = "com.facebook.katana"
)

private fun Context.openInstagram(value: String) = openLinkInApp(
    link = "http://instagram.com/_u/$value",
    webLink = "http://instagram.com/$value",
    appPackage = "com.instagram.android"
)

private fun Context.openTelegram(value: String) = openLinkInApp(
    link = "https://t.me/$value",
    appPackage = "org.telegram.messenger"
)

private fun Context.openWhatsApp(value: String) = openLinkInApp(
    link = "https://api.whatsapp.com/send?phone=$value",
    webLink = "https://wa.me/${value.replace("+", "")}",
    appPackage = "com.whatsapp"
)



private fun Context.copyToClipboard(label: String, link: String){
    val clipboard: ClipboardManager? = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
    val clip = ClipData.newPlainText(label, link)
    clipboard?.setPrimaryClip(clip)
}
