package net.alienminds.ethnogram.service.categories.entities

import com.google.firebase.firestore.QueryDocumentSnapshot
import net.alienminds.ethnogram.service.base.entities.DBObject

class Category internal constructor(
    querySnapshot: QueryDocumentSnapshot
): DBObject(querySnapshot){

    val id: Long by querySnapshot.data
    val p_id: Long by querySnapshot.data
    val emoji: String = getValue(Fields.EMOJI)
    val title: String = getValue(Fields.TITLE)
    val tags: List<String> = getValue(Fields.TAGS)

    val isSubCategory
        get() = p_id != 0L

    val isCategory
        get() = p_id == 0L


    internal object Fields{
//        val ID = Field<Int>("id"){
//            error("ID can't be = null")
//        }
//        val PARENT_ID = Field("p_id", 0)
        val EMOJI = Field("emoji", "")
        val TITLE = Field("title", "")
        val TAGS = Field("tags"){ emptyList<String>() }
    }

}