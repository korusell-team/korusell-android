package net.alienminds.ethnogram.data.firestore.fields

internal object UserFields {
    const val UID = "uid"
    const val TYPE = "isCompany"
    const val IS_PUBLIC = "isPublic"
    const val NAME = "name"
    const val SURNAME = "surname"
    const val BIO = "bio"
    const val INFO = "info"
    const val PHONE = "phone"
    const val PHONE_IS_AVAILABLE = "phoneIsAvailable"

    const val IMAGE = "image"
    const val IMAGE_PATH = "imagePath"
    const val SMALL_IMAGE = "smallImage"
    const val SMALL_IMAGE_PATH = "smallImagePath"

    const val LIKES = "likes"
    const val LIKES_COUNT = "likesCount"
    const val CATEGORIES = "categories"
    const val CITIES = "cities"
    const val BLOCKED = "blockedBy"
    const val REPORTS = "reports"

    const val AVG_RATING = "avgRating"

    const val PRIORITY = "priority"
    const val SPONSORED_EXP_DATE = "sponsoredExpDate"

    const val ADDRESS = "address"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"

    const val CREATED = "created"
    const val UPDATED = "updated"
}