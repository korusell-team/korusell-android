package net.alienminds.ethnogram.service.users.entities

import com.google.firebase.firestore.QueryDocumentSnapshot
import net.alienminds.ethnogram.service.base.entities.DBObject
import java.time.Instant

class User internal constructor(
    val querySnapshot: QueryDocumentSnapshot
): DBObject(querySnapshot){

    val uid: String = getValue(Fields.UID)
    val name: String? = getValue(Fields.NAME)
    val surname: String? = getValue(Fields.SURNAME)
    val bio: String? = getValue(Fields.BIO)
    val info: String? = getValue(Fields.INFO)
    val priority: Long? = getValue(Fields.PRIORITY)

    val phone: String? = getValue(Fields.PHONE)
    val telegram: String? = getValue(Fields.TELEGRAM)
    val instagram: String? = getValue(Fields.INSTAGRAM)
    val whatsApp: String? = getValue(Fields.WHATS_APP)
    var threads: String? = getValue(Fields.THREADS)
    var youtube: String? = getValue(Fields.YOUTUBE)
    var webSite: String? = getValue(Fields.WEB_SITE)
    var facebook: String? = getValue(Fields.FACEBOOK)
    var kakao: String? = getValue(Fields.KAKAO)
    var tiktok: String? = getValue(Fields.TIKTOK)
    var linkedIn: String? = getValue(Fields.LINKED_IN)
    var twitter: String? = getValue(Fields.TWITTER)

    val phoneIsAvailable: Boolean = getValue(Fields.PHONE_IS_AVAILABLE)
    val isPublic: Boolean = getValue(Fields.IS_PUBLIC)

    val image: List<String> = getValue(Fields.IMAGE)
    val imagePath: List<String> = getValue(Fields.IMAGE_PATH)
    val smallImage: String? = getValue(Fields.SMALL_IMAGE)
    val smallImagePath: String? = getValue(Fields.SMALL_IMAGE_PATH)

    val likes: List<String> = getValue(Fields.LIKES)
    val categories: List<Long> = getValue(Fields.CATEGORIES)
    val cities: List<Long> = getValue(Fields.CITIES)
    val blockedBy: List<String> = getValue(Fields.BLOCKED)//Users id who blocked this user
    val reports: List<String> = getValue(Fields.REPORTS)

    val created: Instant? = getValue(Fields.CREATED)
    val updated: Instant? = getValue(Fields.UPDATED)

    val fullName
        get() = buildString {
            name?.let { append("$it ") }
            surname?.let { append(it) }
        }

    val initials
        get() = buildString {
            name?.firstOrNull()?.let { append(it) }
            surname?.firstOrNull()?.let { append(it) }
        }

    val linksMap: Map<LinkType, String?>
        get() = mapOf(
            LinkType.INSTAGRAM to instagram,
            LinkType.TELEGRAM to telegram,
            LinkType.YOUTUBE to youtube,
            LinkType.WEB_SITE to webSite,
            LinkType.FACEBOOK to facebook,
            LinkType.TIKTOK to tiktok,
            LinkType.KAKAO to kakao,
            LinkType.WHATS_APP to whatsApp,
            LinkType.LINKED_IN to linkedIn,
            LinkType.THREADS to threads,
            LinkType.TWITTER to twitter
        )

    val activeLinks: Map<LinkType, String>
        get() = linksMap
            .mapValues { it.value.orEmpty() }
            .filter { it.value.isNotEmpty() }

    //Todo("migrate to activeLinks")
    val links: List<Link>
        get() = linksMap.mapNotNull{ pair ->
            pair.value.takeIf {
                it.isNullOrEmpty().not()
            }?.let {
                Link(it, pair.key)
            }
        }

    class Link internal constructor(
        val value: String,
        val type: LinkType
    )

    enum class LinkType{
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

    object Fields{
        val UID = Field<String>("uid"){ error("User ID can't be = null") }
        val NAME = Field<String?>("name", null)
        val SURNAME = Field<String?>("surname", null)
        val BIO = Field<String?>("bio", null)
        val INFO = Field<String?>("info", null)
        val IS_ONLINE = Field("isOnline", false)
        val PRIORITY = Field<Long?>("priority", null)

        internal val PHONE = Field<String?>("phone", null)
        val TELEGRAM = Field<String?>("telegram", null)
        val INSTAGRAM = Field<String?>("instagram", null)
        val WHATS_APP = Field<String?>("whatsApp", null)
        var THREADS = Field<String?>("threads", null)
        var YOUTUBE = Field<String?>("youtube", null)
        var WEB_SITE = Field<String?>("link", null)
        var FACEBOOK = Field<String?>("facebook", null)
        var KAKAO = Field<String?>("kakao", null)
        var TIKTOK = Field<String?>("tiktok", null)
        var LINKED_IN = Field<String?>("linkedIn", null)
        var TWITTER = Field<String?>("twitter", null)

        val PHONE_IS_AVAILABLE = Field("phoneIsAvailable", false)
        val IS_PUBLIC = Field("isPublic", false)
        val IMAGE = Field("image", emptyList<String>())
        val IMAGE_PATH = Field("imagePath", emptyList<String>())
        val SMALL_IMAGE = Field<String?>("smallImage", null)
        val SMALL_IMAGE_PATH = Field<String?>("smallImagePath", null)
        val LIKES = Field("likes", emptyList<String>())
        val CATEGORIES = Field("categories", emptyList<Long>())
        val CITIES = Field("cities", emptyList<Long>())
        val BLOCKED = Field("blockedBy", emptyList<String>())
        val REPORTS = Field("reports", emptyList<String>())
        internal val CREATED = Field<Instant?>("created", null)
        internal val UPDATED = Field<Instant?>("updated", null)
    }


    override fun toString(): String {
        return "(uid = $uid, name = $name, surname = $surname, phone = $phone)"
    }
    fun User.isProfileComplete(): Boolean {
        // Ваши критерии:
        return !name.isNullOrBlank() || !surname.isNullOrBlank() || !image.isNullOrEmpty()
    }
    
}