package net.alienminds.ethnogram.service.base.entities

data class Field<T>(
    val key: String,
    val defaultValue: () -> T
){
    internal constructor(
        key: String,
        default: T
    ): this(
        key = key,
        defaultValue = { default }
    )
}

data class InputField<out T>(
    val field: Field<out T?>,
    val value: T
)