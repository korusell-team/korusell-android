package net.alienminds.ethnogram.service.feedback.entities

import com.google.firebase.firestore.DocumentSnapshot
import net.alienminds.ethnogram.service.base.entities.Field
import net.alienminds.ethnogram.service.utils.getInstant
import net.alienminds.ethnogram.service.utils.getValue
import java.time.Instant

data class UserFeedback(
    val id: String? = null,
    val comment: String? = null,
    val toUserId: String? = null,
    val fromUserId: String? = null,
    val fromUserPhone: String? = null,
    val rating: Double = 0.0,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
){

    internal constructor(
        doc: DocumentSnapshot,
        toUserId: String? = null
    ) : this(
        id = doc.id,
        toUserId = toUserId,
        comment = doc.getValue(Field.COMMENT),
        fromUserId = doc.getValue(Field.FROM_USER_ID),
        fromUserPhone = doc.getValue(Field.FROM_USER_PHONE),
        rating = doc.getValue(Field.RATING),
        createdAt = doc.getInstant(Field.CREATED_AT),
        updatedAt = doc.getInstant(Field.UPDATED_AT)
    )

    object Field{
        val COMMENT = Field<String?>("comment", null)
        val RATING = Field("rating", 0.0)

        // Not editable fields
        internal val CREATED_AT = Field<Instant?>("createdAt", null)
        internal val UPDATED_AT = Field<Instant?>("updatedAt", null)
        internal val FROM_USER_ID = Field<String?>("fromUserId", null)
        internal val FROM_USER_PHONE = Field<String?>("fromUserPhone", null)
    }

}