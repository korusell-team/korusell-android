package net.alienminds.ethnogram.ui.extentions.fields

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.alienminds.ethnogram.ui.theme.AppColor

@Composable
fun textFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    focusedContainerColor = AppColor.blueGray100,
    unfocusedContainerColor = AppColor.blueGray100,
    disabledContainerColor = AppColor.blueGray100,
    errorContainerColor = AppColor.blueGray100,
    cursorColor = AppColor.lightBlue800
)
