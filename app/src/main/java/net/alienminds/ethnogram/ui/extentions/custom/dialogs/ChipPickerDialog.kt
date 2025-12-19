package net.alienminds.ethnogram.ui.extentions.custom.dialogs

import android.view.Gravity
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import net.alienminds.ethnogram.ui.theme.AppColor


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T>ChipPickerDialog(
    state: AppDialogState,
    gravity: Int = Gravity.TOP,
    title: String,
    items: List<T>,
    itemTitle: (T) -> String,
    itemSelected: (T) -> Boolean,
    onSelect: (T) -> Unit
){
    if (state.visible) {
        Dialog(
            onDismissRequest = state::hide,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            val dialogProvider = LocalView.current.parent as? DialogWindowProvider
            dialogProvider?.window?.setGravity(gravity)


            Column(
                modifier = Modifier
                    .padding(top = 48.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .background(AppColor.brown50, MaterialTheme.shapes.extraLarge)
            ) {
                Title(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(48.dp)
                        .padding(horizontal = 16.dp),
                    title = title,
                    onClose = state::hide
                )
                FlowRow(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { item ->
                        ChipItem(
                            title = itemTitle(item),
                            selected = itemSelected(item),
                            onClick = { onSelect(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Title(
    modifier: Modifier,
    title: String,
    onClose: () -> Unit
) = Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
){
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground
    )
    Icon(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .clickable { onClose() },
        imageVector = Icons.Default.Close,
        tint = MaterialTheme.colorScheme.onBackground,
        contentDescription = null
    )
}


@Composable
private fun ChipItem(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
){
    val backgroundColor by animateColorAsState(when (selected) {
        true -> AppColor.blueGray700
        false -> MaterialTheme.colorScheme.surfaceContainerLow
    })
    val textColor by animateColorAsState(when (selected) {
        true -> AppColor.blueGray100
        false -> MaterialTheme.colorScheme.onSurface
    })
    TextButton(
        modifier = modifier.height(34.dp),
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp
        ),
        colors = ButtonDefaults.textButtonColors(
            containerColor = backgroundColor
        ),
        onClick = onClick
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}