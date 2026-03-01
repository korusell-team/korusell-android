package net.alienminds.ethnogram.data.model.core

sealed class InputField<out V> {

    fun getOrNull(): V? = (this as? Present)?.value

    data class Present<V>(val value: V) : InputField<V>()

    object Absent : InputField<Nothing>()

    companion object {

        fun absent(): Absent = Absent

        fun <V> present(value: V): Present<V> = Present(value)

        fun <V : Any> presentIfNotNull(value: V?): InputField<V> = when(value == null) {
            true -> Absent
            false ->  Present(value)
        }
    }
}