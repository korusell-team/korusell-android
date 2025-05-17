package net.alienminds.ethnogram.service.data.entities

import com.google.firebase.firestore.DocumentSnapshot

data class City(
    val id: Long,
    val en: String,
    val ko: String,
    val ru: String
){
    internal constructor(
        doc: DocumentSnapshot
    ): this(
        id = doc.getLong("id")?: -1,
        en = doc.getString("en").orEmpty(),
        ko = doc.getString("ko").orEmpty(),
        ru = doc.getString("ru").orEmpty()
    )
}