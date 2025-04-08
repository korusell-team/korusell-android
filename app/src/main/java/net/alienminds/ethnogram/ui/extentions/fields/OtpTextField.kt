package net.alienminds.ethnogram.ui.extentions.fields

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.job
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.ui.theme.EthnogramTheme
import net.alienminds.ethnogram.utils.shimmerEffect

@Composable
internal fun OtpTextField(
    modifier: Modifier = Modifier,
    value: String,
    load:MutableState<Boolean>,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.NumberPassword
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    length: Int = 6,
    autoFocus: Boolean = true,
    colors: OtpTextFieldColors = OtpTextFieldColors(),
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium,
){
    fun String.digitAndTrim() = filter { it.isDigit() }.take(length)

    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    val infinity = rememberInfiniteTransition()
    val cursorAlpha by infinity.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, 350),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        if (autoFocus){
            coroutineContext.job.invokeOnCompletion {
                focusRequester.requestFocus()
            }
        }
    }

    BasicTextField(
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused
            },
        value = TextFieldValue(
            text = value.digitAndTrim(),
            selection = TextRange(value.length)
        ),
        onValueChange = {
            onValueChange(it.text.digitAndTrim())
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)) {
                repeat(length) { index ->
                    Box(
                        modifier = Modifier
                            .shimmerEffect(load.value, MaterialTheme.shapes.medium)
                            .widthIn(max = 48.dp)
                            .weight(1f, true)
                            .aspectRatio(0.75f)
                            .background(
                                color = when(isFocused){
                                    true -> colors.focusedContainerColor
                                    false -> colors.unfocusedContainerColor
                                },
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ){
                        value.getOrNull(index)?.toString()?.let { text ->
                            Text(
                                text = text,
                                style = textStyle,
                                color = when(isFocused){
                                    true -> colors.focusedTextColor
                                    false -> colors.unfocusedTextColor
                                }
                            )
                        }?: run {
                            val density = LocalDensity.current
                            if (value.length == index && isFocused){
                                VerticalDivider(
                                    modifier = Modifier
                                        .height(with(density){ textStyle.fontSize.toDp() })
                                        .width(2.dp)
                                        .alpha(cursorAlpha),
                                    thickness = 2.dp,
                                    color = colors.cursorColor
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

data class OtpTextFieldColors(
    val focusedContainerColor: Color = AppColor.blueGray100,
    val unfocusedContainerColor: Color = focusedContainerColor,
    val focusedTextColor: Color = AppColor.gray800,
    val unfocusedTextColor: Color = focusedTextColor,
    val cursorColor: Color = AppColor.lightBlue800
)

@Preview
@Composable
private fun PreviewOtpTextField() = EthnogramTheme{
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ){
        var otp by remember { mutableStateOf("") }
        OtpTextField(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            value = otp,

            load = remember { mutableStateOf(false) },
            autoFocus = false,
            onValueChange = { otp = it }
        )
    }
}