package net.alienminds.ethnogram.ui.extentions

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import net.alienminds.ethnogram.ui.theme.AppColor

@Composable
fun shimmerBrush(
    colors: List<Color> = listOf(
        Color(0xFF007CFF),
        AppColor.indigo100,
        Color(0xFF007CFF)
    ),
    duration: Int = 2000
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")

    val shimmerTranslateX by transition.animateFloat(
        initialValue = -200f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer-translate"
    )

    return Brush.linearGradient(
        colors = colors,
        start = Offset(shimmerTranslateX, 0f),
        end = Offset(shimmerTranslateX + 200f, 200f)
    )
}