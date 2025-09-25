//package net.alienminds.ethnogram.service.base.entities
//
//import com.google.firebase.firestore.QueryDocumentSnapshot
//import java.time.Instant
//
//abstract class DBObject internal constructor(
//    private val querySnapshot: QueryDocumentSnapshot
//){
//
//    protected val map: Map<String, Any>
//        get() = querySnapshot.data
//
//
//    protected inline fun <reified T>getValue(field: Field<T>): T{
//        return map.withDefault { field.defaultValue() }
//            .let { it[field.key] as? T? }
//            ?: field.defaultValue()
//    }
//
//    protected fun getValue(
//        field: Field<Instant?>
//    ): Instant? = querySnapshot
//        .getTimestamp(field.key)
//        ?.toInstant()
//        ?: field.defaultValue()
//
//
//}