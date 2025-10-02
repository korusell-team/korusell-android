package net.alienminds.ethnogram.ui.screens.session.feed.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import net.alienminds.ethnogram.ui.extentions.shimmerState
import net.alienminds.ethnogram.ui.theme.AppColor

@Composable
internal fun CoverImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    shape: Shape = MaterialTheme.shapes.large
){
    imageUrl?.let {
        val image = rememberAsyncImagePainter(it)
        val imageState by image.state.collectAsState()
        if (imageState !is AsyncImagePainter.State.Error) {
            CoverImage(
                modifier = modifier.shimmerState(imageState is AsyncImagePainter.State.Loading),
                painter = image,
                shape = shape
            )
        }
    }
}

@Composable
internal fun CoverImage(
    modifier: Modifier = Modifier,
    painter: Painter,
    shape: Shape = MaterialTheme.shapes.large
) = Box(
    modifier = modifier
        .clip(shape)
        .background(AppColor.gray100)
){
    Image(
        modifier = Modifier
            .matchParentSize()
            .blur(25.dp),
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
    Image(
        modifier = Modifier.matchParentSize(),
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Fit
    )
}