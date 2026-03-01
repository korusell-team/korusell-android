package net.alienminds.ethnogram.ui.extentions.fields

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

// Forked from Material3 TextField with reduced minHeight (44.dp)
@Composable
fun CompactTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
    label: @Composable (TextFieldLabelScope.() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    scrollState: ScrollState? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    contentPadding: PaddingValues = compactContentPaddingByLabel(
        labelPosition = labelPosition,
        label = label
    ),
    interactionSource: MutableInteractionSource? = null,
) {
    val resolvedScrollState = scrollState?: rememberScrollState()
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    // If color is not provided via the text style, use content color as a default
    val textColor =
        textStyle.color.takeOrElse {
            val focused = interactionSource.collectIsFocusedAsState().value
            colors.textColor(enabled, isError, focused)
        }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            state = state,
            modifier =
                modifier
                    .defaultMinSize(
                        minWidth = TextFieldDefaults.MinWidth,
                        minHeight = TextFieldDefaults.MinHeight,
                    ),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(colors.cursorColor(isError)),
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            lineLimits = lineLimits,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            inputTransformation = inputTransformation,
            outputTransformation = outputTransformation,
            scrollState = resolvedScrollState,
            decorator =
                TextFieldDefaults.decorator(
                    state = state,
                    enabled = enabled,
                    lineLimits = lineLimits,
                    outputTransformation = outputTransformation,
                    interactionSource = interactionSource,
                    labelPosition = labelPosition,
                    label = label,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    isError = isError,
                    colors = colors,
                    contentPadding = contentPadding,
                    container = {
                        TextFieldDefaults.Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = shape,
                        )
                    },
                ),
        )
    }
}

private fun compactContentPaddingByLabel(
    labelPosition: TextFieldLabelPosition,
    label: @Composable (TextFieldLabelScope.() -> Unit)?,
) = when(label == null || labelPosition is TextFieldLabelPosition.Above) {
    true -> PaddingValues(12.dp)
    false -> PaddingValues(
        vertical = 12.dp,
        horizontal = 8.dp
    )
}

@Stable
private fun TextFieldColors.textColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
    when {
        !enabled -> disabledTextColor
        isError -> errorTextColor
        focused -> focusedTextColor
        else -> unfocusedTextColor
    }

@Stable
private fun TextFieldColors.cursorColor(isError: Boolean): Color =
    if (isError) errorCursorColor else cursorColor