package net.alienminds.ethnogram.service.data.entities

import com.google.firebase.firestore.DocumentSnapshot
import net.alienminds.ethnogram.service.base.entities.Field
import net.alienminds.ethnogram.service.utils.getValue

data class City(
    val id: Long,
    val en: String,
    val ko: String,
    val ru: String
){
    internal constructor(
        doc: DocumentSnapshot
    ): this(
        id = doc.getValue(Field.ID),
        en = doc.getValue(Field.EN),
        ko = doc.getValue(Field.KO),
        ru = doc.getValue(Field.RU)
    )

    private object Field{
        val ID = Field<Long>("id"){ error("id must not be null") }
        val EN = Field("en", "")
        val KO = Field("ko", "")
        val RU = Field("ru", "")
    }
}