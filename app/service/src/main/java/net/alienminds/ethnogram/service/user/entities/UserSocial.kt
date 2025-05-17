package net.alienminds.ethnogram.service.user.entities

import com.google.firebase.firestore.DocumentSnapshot
import net.alienminds.ethnogram.service.base.entities.Field
import net.alienminds.ethnogram.service.utils.getValue

data class UserSocial(
    val telegram: String? = null,
    val instagram: String? = null,
    val whatsApp: String? = null,
    val threads: String? = null,
    val youtube: String? = null,
    val webSite: String? = null,
    val facebook: String? = null,
    val kakao: String? = null,
    val tiktok: String? = null,
    val linkedIn: String? = null,
    val twitter: String? = null,
){

    internal constructor(
        doc: DocumentSnapshot
    ): this(
        telegram = doc.getValue(Field.TELEGRAM),
        instagram = doc.getValue(Field.INSTAGRAM),
        whatsApp = doc.getValue(Field.WHATS_APP),
        threads = doc.getValue(Field.THREADS),
        youtube = doc.getValue(Field.YOUTUBE),
        webSite = doc.getValue(Field.WEB_SITE),
        facebook = doc.getValue(Field.FACEBOOK),
        kakao = doc.getValue(Field.KAKAO),
        tiktok = doc.getValue(Field.TIKTOK),
        linkedIn = doc.getValue(Field.LINKED_IN),
        twitter = doc.getValue(Field.TWITTER)
    )

    object Field{
        val TELEGRAM = Field<String?>("telegram", null)
        val INSTAGRAM = Field<String?>("instagram", null)
        val WHATS_APP = Field<String?>("whatsApp", null)
        val THREADS = Field<String?>("threads", null)
        val YOUTUBE = Field<String?>("youtube", null)
        val WEB_SITE = Field<String?>("link", null)
        val FACEBOOK = Field<String?>("facebook", null)
        val KAKAO = Field<String?>("kakao", null)
        val TIKTOK = Field<String?>("tiktok", null)
        val LINKED_IN = Field<String?>("linkedIn", null)
        val TWITTER = Field<String?>("twitter", null)
    }

    val socialMap = mapOf(
        UserSocialType.INSTAGRAM to instagram,
        UserSocialType.TELEGRAM to telegram,
        UserSocialType.YOUTUBE to youtube,
        UserSocialType.WEB_SITE to webSite,
        UserSocialType.FACEBOOK to facebook,
        UserSocialType.TIKTOK to tiktok,
        UserSocialType.KAKAO to kakao,
        UserSocialType.WHATS_APP to whatsApp,
        UserSocialType.LINKED_IN to linkedIn,
        UserSocialType.THREADS to threads,
        UserSocialType.TWITTER to twitter,
    )

    val activeLinks: Map<UserSocialType, String>
        get() = socialMap
            .mapValues { it.value.orEmpty() }
            .filter { it.value.isNotEmpty() }


    fun getByType(type: UserSocialType): String? = socialMap[type]

}

enum class UserSocialType{
    INSTAGRAM,
    TELEGRAM,
    YOUTUBE,
    WEB_SITE,
    FACEBOOK,
    TIKTOK,
    KAKAO,
    WHATS_APP,
    LINKED_IN,
    THREADS,
    TWITTER,
}