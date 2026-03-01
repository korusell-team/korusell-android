package net.alienminds.ethnogram.data.model.user

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
    val avgRating: Double = 0.0,
    val type: UserType = UserType.PERSONAL, // isCompany field
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
