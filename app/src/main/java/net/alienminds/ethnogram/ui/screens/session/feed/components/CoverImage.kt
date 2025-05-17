package net.alienminds.ethnogram.ui.screens.session.feed.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import net.alienminds.ethnogram.ui.extentions.shimmerState
import net.alienminds.ethnogram.ui.theme.AppColor

@Composable
internal fun CoverImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
){
    imageUrl?.let {
        val image = rememberAsyncImagePainter(it)
        val imageState by image.state.collectAsState()
        if (imageState !is AsyncImagePainter.State.Error) {
            Image(
                modifier = modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(AppColor.gray100)
                    .shimmerState(imageState is AsyncImagePainter.State.Loading),
                painter = image,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}