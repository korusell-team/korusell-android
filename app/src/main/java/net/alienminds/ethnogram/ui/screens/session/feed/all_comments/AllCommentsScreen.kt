package net.alienminds.ethnogram.ui.screens.session.feed.all_comments

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.feed.entities.Author
import net.alienminds.ethnogram.service.feed.entities.FeedComment
import net.alienminds.ethnogram.ui.extentions.buttons.BackButton
import net.alienminds.ethnogram.ui.extentions.custom.Avatar
import net.alienminds.ethnogram.ui.extentions.shimmerState
import net.alienminds.ethnogram.ui.screens.session.feed.components.AuthorContent
import net.alienminds.ethnogram.utils.rememberRelativeTime
import java.time.Instant

internal class AllCommentsScreen(
    private val feedId: String
): Screen {

    @Composable
    override fun Content() = Column(
        modifier = Modifier.fillMaxSize()
    ){
        val vm = rememberScreenModel { AllCommentsModel(feedId) }
        Toolbar(
            modifier = Modifier.statusBarsPadding()
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
           commentsList(
                comments = vm.comments,
                authors = vm.authors
           )
        }
        HorizontalDivider()
        CommentInput(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .shimmerState(vm.loading),
            value = vm.myCommentInput,
            onValueChange = { vm.myCommentInput = it },
            avatarUrl = vm.me?.image?.firstOrNull(),
            initials = vm.me?.initials,
            enabled = vm.loading.not() && vm.isAnonymous.not(),
            onSendClick = vm::sendComment,
        )
    }

    @Composable
    private fun CommentInput(
        modifier: Modifier = Modifier,
        value: String,
        onValueChange: (String) -> Unit,
        avatarUrl: String?,
        initials: String?,
        enabled: Boolean,
        onSendClick: () -> Unit,
    ){
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ){
            Avatar(
                modifier = Modifier.size(32.dp),
                model = avatarUrl,
                initials = initials.orEmpty(),
                contentScale = ContentScale.Crop
            )
            OutlinedTextField(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f),
                value = value,
                onValueChange = onValueChange,
                shape = MaterialTheme.shapes.large,
                readOnly = enabled.not(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.your_comment),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                maxLines = 4,
            )
            IconButton(
                onClick = onSendClick,
                enabled = enabled && value.trim().isNotEmpty(),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                )
            }

        }
    }


    private fun LazyListScope.commentsList(
        comments: List<FeedComment>,
        authors: Map<String, Author>
    ){
        items(comments){ comment ->
            val author = authors[comment.userId]?: Author(
                name = comment.userName,
                avatarUrl = comment.userAvatarUrl
            )
            Column{
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    AuthorContent(
                        modifier = Modifier.weight(1f, false),
                        author = author,
                        avatarSize = 32.dp,
                        textStyle = MaterialTheme.typography.titleSmall,
                        textColor = MaterialTheme.colorScheme.onBackground
                    )
                    RelativeTime(
                        instant = comment.updatedAt?: comment.createdAt
                    )
                }
                Text(
                    modifier = Modifier
                        .padding(start = 40.dp)
                        .fillMaxWidth(),
                    text = comment.text.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

    @Composable
    private fun RelativeTime(
        modifier: Modifier = Modifier,
        instant: Instant?
    ){
        instant?.let { createdAt ->
            val relativeTime by createdAt.rememberRelativeTime()
            Text(
                modifier = modifier,
                text = relativeTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }



    @Composable
    private fun Toolbar(
        modifier: Modifier = Modifier,
    ) = Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        BackButton(
            modifier = Modifier.align(Alignment.CenterStart),
            text = stringResource(R.string.back),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.comments),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
    }

}