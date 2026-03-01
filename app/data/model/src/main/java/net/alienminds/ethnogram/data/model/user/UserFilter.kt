package net.alienminds.ethnogram.data.model.user

import net.alienminds.ethnogram.data.model.common.ID
import net.alienminds.ethnogram.data.model.core.InputField

data class UserFilter(
    val cityId: InputField<ID> = InputField.absent(),
    val categoryId: InputField<ID> = InputField.absent(),
    val searchQuery: InputField<String> = InputField.absent(),
)
