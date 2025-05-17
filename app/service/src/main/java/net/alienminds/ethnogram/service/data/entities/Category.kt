package net.alienminds.ethnogram.service.data.entities

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentSnapshot

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
        id = doc.getLong("id")!!,
        parentId = doc.getLong("p_id")?: -1,
        emoji = doc.getString("emoji").orEmpty(),
        title = doc.getString("title").orEmpty(),
        tags = doc.getTags()
    )

    val isSubCategory
        get() = parentId != 0L

    val isCategory
        get() = parentId == 0L

    companion object{

        private fun DocumentSnapshot.getTags(): List<String> =
            (get("tags") as? List<*>)?.mapNotNull {
                it as? String
            }.orEmpty()

    }
}