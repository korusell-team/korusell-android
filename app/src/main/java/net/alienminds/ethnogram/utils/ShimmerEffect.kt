package net.alienminds.ethnogram.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize

fun Modifier.shimmerEffect(
    enabled: Boolean,
    shape: Shape = RectangleShape
): Modifier = composed {
    if (!enabled) return@composed this

    var size by remember { mutableStateOf(IntSize.Zero) }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "shimmerOffset"
    )

    this
        .onGloballyPositioned { size = it.size }
        .drawWithContent {
//            drawContent() // основной UI

            if (size.width == 0 || size.height == 0) return@drawWithContent

            val gradientWidth = size.width.toFloat() / 2
            val startX = startOffsetX * size.width

            val brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE6EBF2),
                    Color(0xFFF6F7F8),
                    Color(0xFFE6EBF2)
                ),
                start = Offset(startX, 0f),
                end = Offset(startX + gradientWidth, size.height.toFloat())
            )

            // Важное место: создание обводки по shape
            val outline = shape.createOutline(
                size = size.toSize(),
                layoutDirection = layoutDirection,
                density = this
            )

            drawOutline(
                outline = outline,
                brush = brush,
                blendMode = BlendMode.SrcIn
            )
        }
}
