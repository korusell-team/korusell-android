package net.alienminds.ethnogram.ui.extentions.custom.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.alienminds.ethnogram.R


@Composable
fun AppAlertDialog(
    modifier: Modifier = Modifier,
    state: AppDialogState,
    title: String,
    text: String,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    dismissColor: Color? = null,
    confirmColor: Color? = null,
    confirmText: String = stringResource(R.string.ok),
    dismissText: String? = null,
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
){
    if (state.visible) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = state::hide,
            containerColor = containerColor,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            confirmButton = { DialogButton(
                text = confirmText,
                color = confirmColor?: LocalContentColor.current,
                state = state,
                onClick = onConfirm
            ) },
            dismissButton = { dismissText?.let {
                DialogButton(
                    text = it,
                    color = dismissColor?: LocalContentColor.current,
                    state = state,
                    onClick = onDismiss
                )
            } },
            title = { Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            ) },
            text = { Text(text) },
        )
    }
}

@Composable
private fun DialogButton(
    text: String,
    color: Color,
    state: AppDialogState,
    onClick: (() -> Unit)?
) = TextButton(
    onClick = {
        onClick?.invoke()
        state.hide()
    },
    content = { Text(text) },
    colors = ButtonDefaults.textButtonColors(
        contentColor = color
    )
)
