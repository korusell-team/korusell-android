package net.alienminds.ethnogram.service.feed.entities

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant

/***
 * @param postValidUntil - До какого числа нужно показывать пост нужно отфильтровать те которые еще не устарели
 * @param promotedUntil - Дата окончания оплаты поста. После этой даты он становится обычным маленьким
 */
@Keep
data class Feed(
    val id: String?,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val author: FeedAuthor? = null,
    val authorPhone: String? = null,
    val likeList: List<String>? = null,
    val comments: List<FeedComment>? = null,
    val type: FeedType? = null,
    val promoDetails: PromoDetail? = null,
    val eventDetails: EventDetails? = null,
    val postValidUntil: Instant? = null,
    val promotedUntil: Instant? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {

    internal constructor(doc: DocumentSnapshot) : this(
        id = doc.id,
        title = doc.getString("title"),
        description = doc.getString("description"),
        imageUrl = doc.getString("imageUrl"),
        author = doc.parseAuthor(),
        authorPhone = doc.getString("authorPhone"),
        likeList = doc.parseLikes(),
        comments = doc.parseComments(),
        type = doc.parseType(),
        promoDetails = doc.parsePromo(),
        eventDetails = doc.parseEvent(),
        postValidUntil = doc.getTimestamp("postValidUntil")?.toInstant(),
        promotedUntil = doc.getTimestamp("promotedUntil")?.toInstant(),
        createdAt = doc.getTimestamp("created")?.toInstant(),
        updatedAt = doc.getTimestamp("updated")?.toInstant()
    )

    val isPromotedNow: Boolean
        get() {
            val now = Instant.now()
            return promotedUntil?.isAfter(now) == true
        }

    companion object{

        private fun DocumentSnapshot.parseType() =
            getLong("type")?.toInt()?.let(FeedType::fromId)

        private fun DocumentSnapshot.parseAuthor() =
            (get("author") as? Map<*, *>)?.let(::FeedAuthor)

        private fun DocumentSnapshot.parsePromo() =
            (get("promoDetails") as? Map<*, *>)?.let(::PromoDetail)

        private fun DocumentSnapshot.parseEvent() =
            (get("eventDetails") as? Map<*, *>)?.let(::EventDetails)

        private fun DocumentSnapshot.parseLikes() =
            (get("likelist") as? List<*>)?.mapNotNull {
                it as? String
            }

        private fun DocumentSnapshot.parseComments(): List<FeedComment> =
            (get("comments") as? HashMap<*, *>)
                ?.values?.mapNotNull {
                    (it as? HashMap<*, *>)
                        ?.let(::FeedComment)
                }.orEmpty()

    }

}

enum class FeedType(
    internal val id: Int
){
    NEWS(0),
    EVENT(1),
    PROMO(2);

    companion object {
        fun fromId(id: Int?): FeedType? = entries.find { it.id == id }
    }
}


data class PromoDetail(
    val discount: Long? = null,
    val validUntil: Instant? = null
){
    internal constructor(map: Map<*, *>?): this(
        discount = map?.get("discount") as? Long,
        validUntil = (map?.get("validUntil") as? Timestamp)?.toInstant()
    )
}

data class EventDetails(
    val location: String? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null
){
    internal constructor(map: Map<*, *>?): this(
        location = map?.get("location") as? String,
        startTime = (map?.get("startTime") as? Timestamp)?.toInstant(),
        endTime = (map?.get("endTime") as? Timestamp)?.toInstant()
    )
}

data class FeedAuthor(
    val avatarUrl: String? = null,
    val fullName: String? = null,
    val phone: String? = null
) {
    internal constructor(map: Map<*, *>?) : this(
        avatarUrl = map?.get("avatarUrl") as? String,
        fullName = map?.get("fullname") as? String,
        phone = map?.get("phone") as? String
    )
}

@Keep
data class FeedComment(
    val text: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val userAvatarUrl: String? = null,
    val createdAt: Instant? = null,
) {
    internal constructor(map: Map<*, *>?) : this(
        text = map?.get("text") as? String,
        userId = map?.get("userId") as? String,
        userName = map?.get("userName") as? String,
        userAvatarUrl = map?.get("userAvatarUrl") as? String,
        createdAt = (map?.get("createdAt") as? Timestamp)?.toInstant()
    )
}
