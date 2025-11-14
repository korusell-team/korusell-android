package net.alienminds.ethnogram.service.user.entities

import com.google.firebase.firestore.DocumentSnapshot
import net.alienminds.ethnogram.service.base.entities.Field
import net.alienminds.ethnogram.service.utils.getInstant
import net.alienminds.ethnogram.service.utils.getValue
import java.time.Instant


data class User(
    val uid: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val bio: String? = null,
    val info: String? = null,
    val priority: Long? = null,
    val phone: String? = null,
    val social: UserSocial = UserSocial(),
    val phoneIsAvailable: Boolean? = null,
    val isPublic: Boolean? = null,
    val image: List<String> = emptyList(),
    val imagePath: List<String> = emptyList(),
    val smallImage: String? = null,
    val smallImagePath: String? = null,
    val likes: List<String> = emptyList(),
    val categories: List<Long> = emptyList(),
    val cities: List<Long> = emptyList(),
    val blockedBy: List<String> = emptyList(),
    val reports: List<String> = emptyList(),
    val sponsoredExpDate: Instant? = null,
    val created: Instant? = null,
    val updated: Instant? = null,
    val avgRating: Double = 0.0
){

    internal constructor(
        doc: DocumentSnapshot
    ) : this(
        uid = doc.getValue(Field.UID),
        name = doc.getValue(Field.NAME),
        surname = doc.getValue(Field.SURNAME),
        bio = doc.getValue(Field.BIO),
        info = doc.getValue(Field.INFO),
        priority = doc.getValue(Field.PRIORITY),
        phone = doc.getValue(Field.PHONE),
        phoneIsAvailable = doc.getValue(Field.PHONE_IS_AVAILABLE),
        isPublic = doc.getValue(Field.IS_PUBLIC),
        image = doc.getValue(Field.IMAGE),
        imagePath = doc.getValue(Field.IMAGE_PATH),
        smallImage = doc.getValue(Field.SMALL_IMAGE),
        smallImagePath = doc.getValue(Field.SMALL_IMAGE_PATH),
        likes = doc.getValue(Field.LIKES),
        categories = doc.getValue(Field.CATEGORIES),
        cities = doc.getValue(Field.CITIES),
        blockedBy = doc.getValue(Field.BLOCKED),
        reports = doc.getValue(Field.REPORTS),
        sponsoredExpDate = doc.getInstant(Field.SPONSORED_EXP_DATE),
        created = doc.getInstant(Field.CREATED),
        updated = doc.getInstant(Field.UPDATED),
        social = UserSocial(doc),
        avgRating = doc.getValue(Field.AVG_RATING)
    )

    val fullName
        get() = "${name.orEmpty()} ${surname.orEmpty()}".trim()

    val initials
        get() = buildString {
            name?.firstOrNull()?.let { append(it) }
            surname?.firstOrNull()?.let { append(it) }
        }

    val isProfileComplete
        get() = name.isNullOrEmpty().not() ||
                surname.isNullOrEmpty().not() ||
                image.isNotEmpty()

    object Field{
        val UID = Field<String>("uid"){ error("User ID can't be = null") }
        val NAME = Field<String?>("name", null)
        val SURNAME = Field<String?>("surname", null)
        val BIO = Field<String?>("bio", null)
        val INFO = Field<String?>("info", null)
        val PRIORITY = Field<Long?>("priority", null)
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
        val AVG_RATING = Field("avgRating", 0.0)

        //Not editable fields
        internal val PHONE = Field<String?>("phone", null)
        internal val SPONSORED_EXP_DATE = Field<Instant?>("sponsoredExpDate", null)
        internal val CREATED = Field<Instant?>("created", null)
        internal val UPDATED = Field<Instant?>("updated", null)
    }
}




