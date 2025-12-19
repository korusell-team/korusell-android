package net.alienminds.ethnogram.ui.extentions

import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.alienminds.ethnogram.ui.theme.AppColor

@Composable
fun CircularTextBadge(
    modifier: Modifier = Modifier,
    text: String,
    radiusDp: Dp = 32.dp,
    strokeWidthDp: Dp = 8.dp,
    textAngleDeg: Float = 0f,
    brush: Brush = Brush.linearGradient(
        colors = listOf(
            Color(230f / 255f, 232f / 255f, 235f / 255f),
            Color(200f / 255f, 202f / 255f, 205f / 255f),
            Color(245f / 255f, 247f / 255f, 250f / 255f),
            Color(180f / 255f, 182f / 255f, 185f / 255f)
        )
    ),
    textColor: Color = AppColor.blueGray600
) {
    val radiusPx = with(LocalDensity.current) { radiusDp.toPx() }
    val strokeWidthPx = with(LocalDensity.current) { strokeWidthDp.toPx() }


    Canvas(modifier = modifier.size(radiusDp * 2)) {
        drawCircle(
            brush = brush,
            radius = radiusPx,
            style = Stroke(width = strokeWidthPx)
        )

        val textRadius = radiusPx - strokeWidthPx / 2f

        val path = Path().apply {
            addCircle(center.x, center.y, textRadius, Path.Direction.CW)
        }

        drawContext.canvas.nativeCanvas.apply {
            val textPaint = Paint().apply {
                color = textColor.toArgb()
                textSize = strokeWidthDp.toPx()*0.8f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }

            val circumference = (2 * Math.PI * textRadius).toFloat()
            val hOffset = (circumference * (textAngleDeg / 360f))
            val vOffset = -textPaint.descent()

            drawTextOnPath(text, path, hOffset, vOffset, textPaint)
        }
    }
}