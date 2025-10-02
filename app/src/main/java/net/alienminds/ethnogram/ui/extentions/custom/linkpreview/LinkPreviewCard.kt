package net.alienminds.ethnogram.ui.extentions.custom.linkpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.alienminds.ethnogram.ui.screens.session.feed.components.CoverImage
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.ui.theme.EthnogramTheme
import ru.iquack.linkpreview.compose.LinkPreviewState
import ru.iquack.linkpreview.compose.rememberLinkPreviewState
import java.net.URI

@Composable
fun LinkPreviewCard(
    modifier: Modifier = Modifier,
    url: String,
){
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(AppColor.white)
    ){
        val previewState = rememberLinkPreviewState(url)
        val meta = (previewState as? LinkPreviewState.Success)?.preview

        val image = meta?.openGraph?.image
        val title = meta?.openGraph?.title?: meta?.pageTitle
        val siteName = meta?.openGraph?.siteName?: runCatching {
            URI(url).host?.removePrefix("www.") ?: url
        }.getOrNull()?: url

        CoverImage(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            imageUrl = image,
            shape = RectangleShape
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColor.brown50)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(
                modifier = Modifier.weight(1f)
            ){

                Text(
                    text = title?: siteName,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColor.gray900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = siteName,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColor.gray700,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = AppColor.gray400
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLink() = EthnogramTheme {
    Box(modifier = Modifier.fillMaxSize()) {
        LinkPreviewCard(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)
                .fillMaxWidth()
                .aspectRatio(1f),
            url = "https://www.youtube.com/watch?v=ecBJrsqvSdg"
        )
    }
}