package net.alienminds.ethnogram.service.data.entities

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentSnapshot
import net.alienminds.ethnogram.service.base.entities.Field
import net.alienminds.ethnogram.service.utils.getValue

@Keep
data class Category(
    val id: Long,
    val parentId: Long,
    val emoji: String,
    val title: String,
    val tags: List<String>
){
    internal constructor(
        doc: DocumentSnapshot
    ): this(
        id = doc.getValue(Field.ID),
        parentId = doc.getValue(Field.PARENT_ID),
        emoji = doc.getValue(Field.EMOJI),
        title = doc.getValue(Field.TITLE),
        tags = doc.getValue(Field.TAGS)
    )

    val isCategory get() = parentId == 0L
    val isSubCategory get() = parentId != 0L

    val emojiTitle
        get() = "$emoji $title"


    private object Field{
        val ID = Field<Long>("id"){ error("id must not be null") }
        val PARENT_ID = Field<Long>("p_id", -1)
        val EMOJI = Field("emoji", "")
        val TITLE = Field("title", "")
        val TAGS = Field<List<String>>("tags", emptyList())
    }
}