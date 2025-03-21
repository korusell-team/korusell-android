package net.alienminds.ethnogram.ui.extentions.custom

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import net.alienminds.ethnogram.ui.extentions.shimmerState
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.ui.theme.EthnogramTheme

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    model: Any?,
    initials: String = "",
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    shape: Shape = CircleShape,
    border: BorderStroke = BorderStroke(0.dp, Color.Transparent),
    onClick: (() -> Unit)? = null
) = SubcomposeAsyncImage(
    modifier = modifier
        .clip(shape)
//        .border(border, shape)
        .clickable(
            enabled = onClick != null,
            onClick = { onClick?.invoke() }
        ),
    model = model,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    contentDescription = null
){
    val state by painter.state.collectAsState()
    val derState by remember { derivedStateOf { state } }
    Box(
        modifier = Modifier
            .shimmerState(derState is AsyncImagePainter.State.Loading),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = derState,
            modifier = Modifier.align(Alignment.Center), label = ""
        ) {
            if (it is AsyncImagePainter.State.Success && it.painter.intrinsicSize.isEmpty().not()) {
                this@SubcomposeAsyncImage.SubcomposeAsyncImageContent(
                    modifier = Modifier.fillMaxSize()
                )
            } else if (initials.isEmpty()) {
                Icon(
                    Icons.Rounded.AccountCircle,
                    contentDescription = "avatar view",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    text = initials,
                    style = textStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AvatarPreview(){
    EthnogramTheme {
        Avatar(
            modifier = Modifier
                .padding(16.dp)
                .size(64.dp),
            model = null,
            initials = "ЕK",
            border = BorderStroke(1.dp, AppColor.white)
        )
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ){

        }
    }
}