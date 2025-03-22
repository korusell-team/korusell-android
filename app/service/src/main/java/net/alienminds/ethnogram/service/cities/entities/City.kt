package net.alienminds.ethnogram.service.cities.entities

import com.google.firebase.firestore.QueryDocumentSnapshot
import net.alienminds.ethnogram.service.base.entities.DBObject

class City internal constructor(
    querySnapshot: QueryDocumentSnapshot
): DBObject(querySnapshot){

    val id: Long by querySnapshot.data
    val en: String = getValue(Fields.EN)
    val ko: String = getValue(Fields.KO)
    val ru: String = getValue(Fields.RU)

    internal object Fields{
//        val ID = Field("id", 0)//TODO("id Field have bag replaced")
        val EN = Field("en", "")
        val KO = Field("ko", "")
        val RU = Field("ru", "")
    }

}