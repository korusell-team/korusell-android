package net.alienminds.ethnogram.mappers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.user.entities.UserSocial
import net.alienminds.ethnogram.service.user.entities.UserSocialType
import net.alienminds.ethnogram.service.user.entities.UserType
import net.alienminds.ethnogram.utils.openLinkExternal
import net.alienminds.ethnogram.utils.openLinkInApp

//val User.Link.title
//    @Composable get() = type.title

val UserSocialType.title
    @Composable get() = when(this){
        UserSocialType.TELEGRAM -> stringResource(R.string.telegram)
        UserSocialType.INSTAGRAM -> stringResource(R.string.instagram)
        UserSocialType.WHATS_APP -> stringResource(R.string.whatsapp)
        UserSocialType.THREADS -> stringResource(R.string.threads)
        UserSocialType.YOUTUBE -> stringResource(R.string.youtube)
        UserSocialType.WEB_SITE -> stringResource(R.string.web_site)
        UserSocialType.FACEBOOK -> stringResource(R.string.facebook)
        UserSocialType.KAKAO -> stringResource(R.string.kakao)
        UserSocialType.TIKTOK -> stringResource(R.string.tiktok)
        UserSocialType.LINKED_IN -> stringResource(R.string.linked_in)
        UserSocialType.TWITTER -> stringResource(R.string.twitter)
    }

//val User.Link.placeholder
//    @Composable get() = type.placeholder

val UserSocialType.placeholder
    @Composable get() = when(this){
        UserSocialType.INSTAGRAM,
        UserSocialType.THREADS,
        UserSocialType.TWITTER -> stringResource(R.string.nickname)
        UserSocialType.YOUTUBE -> stringResource(R.string.channel)
        UserSocialType.WEB_SITE -> stringResource(R.string.link_placeholder)
        UserSocialType.FACEBOOK,
        UserSocialType.TIKTOK,
        UserSocialType.KAKAO -> stringResource(R.string.id)
        UserSocialType.TELEGRAM -> stringResource(R.string.telegram_placeholder)
        UserSocialType.LINKED_IN -> stringResource(R.string.profile)
        UserSocialType.WHATS_APP -> stringResource(R.string.phone_placeholder)
    }

//val User.Link.roundIcon
//    @Composable get() = type.roundIcon

val UserSocialType.roundIcon
    @Composable get() = when(this){
        UserSocialType.TELEGRAM -> painterResource(R.drawable.ic_telegram_round)
        UserSocialType.INSTAGRAM -> painterResource(R.drawable.ic_instagram_round)
        UserSocialType.WHATS_APP -> painterResource(R.drawable.ic_whatsapp_round)
        UserSocialType.THREADS -> painterResource(R.drawable.ic_threads_round)
        UserSocialType.YOUTUBE -> painterResource(R.drawable.ic_youtube_round)
        UserSocialType.WEB_SITE -> painterResource(R.drawable.ic_link_round)
        UserSocialType.FACEBOOK -> painterResource(R.drawable.ic_facebook_round)
        UserSocialType.KAKAO -> painterResource(R.drawable.ic_kakao_round)
        UserSocialType.TIKTOK -> painterResource(R.drawable.ic_tiktok_round)
        UserSocialType.LINKED_IN -> painterResource(R.drawable.ic_linkedin_round)
        UserSocialType.TWITTER -> painterResource(R.drawable.ic_twitter_round)
    }


//val User.Link.icon
//    @Composable get() = type.icon

val UserSocialType.icon
    @Composable get() = when(this){
        UserSocialType.TELEGRAM -> painterResource(R.drawable.ic_telegram)
        UserSocialType.INSTAGRAM -> painterResource(R.drawable.ic_instagram)
        UserSocialType.WHATS_APP -> painterResource(R.drawable.ic_whatsapp)
        UserSocialType.THREADS -> painterResource(R.drawable.ic_threads)
        UserSocialType.YOUTUBE -> painterResource(R.drawable.ic_youtube)
        UserSocialType.WEB_SITE -> painterResource(R.drawable.ic_www)
        UserSocialType.FACEBOOK -> painterResource(R.drawable.ic_facebook)
        UserSocialType.KAKAO -> painterResource(R.drawable.ic_kakao)
        UserSocialType.TIKTOK -> painterResource(R.drawable.ic_tiktok)
        UserSocialType.LINKED_IN -> painterResource(R.drawable.ic_linkedin)
        UserSocialType.TWITTER -> painterResource(R.drawable.ic_twitter)
    }

fun UserSocialType.displayValue(
    value: String?
) = when(this){
    UserSocialType.TELEGRAM,
    UserSocialType.INSTAGRAM -> "@$value"
    else -> value.orEmpty()
}


val UserSocialType.field
    get() = when(this){
        UserSocialType.INSTAGRAM -> UserSocial.Field.INSTAGRAM
        UserSocialType.TELEGRAM -> UserSocial.Field.TELEGRAM
        UserSocialType.YOUTUBE -> UserSocial.Field.YOUTUBE
        UserSocialType.WEB_SITE -> UserSocial.Field.WEB_SITE
        UserSocialType.FACEBOOK -> UserSocial.Field.FACEBOOK
        UserSocialType.TIKTOK -> UserSocial.Field.TIKTOK
        UserSocialType.KAKAO -> UserSocial.Field.KAKAO
        UserSocialType.WHATS_APP -> UserSocial.Field.WHATS_APP
        UserSocialType.LINKED_IN -> UserSocial.Field.LINKED_IN
        UserSocialType.THREADS -> UserSocial.Field.THREADS
        UserSocialType.TWITTER -> UserSocial.Field.TWITTER
    }


fun UserSocialType.openInApp(
    context: Context,
    value: String
) = with(context){
    when(this@openInApp){
        UserSocialType.INSTAGRAM -> openInstagram(value)
        UserSocialType.TELEGRAM -> openTelegram(value)
        UserSocialType.YOUTUBE -> openYoutube(value)
        UserSocialType.WEB_SITE -> openWebLink(value)
        UserSocialType.FACEBOOK -> openFacebook(value)
        UserSocialType.TIKTOK -> openTiktok(value)
        UserSocialType.WHATS_APP -> openWhatsApp(value)
        UserSocialType.LINKED_IN -> openLinkedIn(value)
        UserSocialType.THREADS -> openThreads(value)
        UserSocialType.TWITTER -> openTwitter(value)
        else -> copyToClipboard(this@openInApp.name, displayValue(value))
    }
}

val UserType.titleId
    @StringRes get() = when(this){
        UserType.PERSONAL -> R.string.personal_profile
        UserType.BUSINESS -> R.string.business_profile
    }

val UserType.title
    @Composable get() = stringResource(titleId)


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



fun Context.copyToClipboard(label: String, link: String){
    val clipboard: ClipboardManager? = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
    val clip = ClipData.newPlainText(label, link)
    clipboard?.setPrimaryClip(clip)
}
