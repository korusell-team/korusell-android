package net.alienminds.ethnogram.ui.screens.session.contacts.all_feedbacks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.feed.entities.Author
import net.alienminds.ethnogram.service.feedback.entities.UserFeedback
import net.alienminds.ethnogram.ui.extentions.buttons.BackButton
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.screens.session.feed.components.AuthorContent
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.utils.rememberRelativeTime
import kotlin.math.roundToInt

internal class AllFeedbacksScreen(
    private val userId: String
): PageTransitionScreen {

    override val position: Int
        get() = 3

    @Composable
    override fun Content() = Column(
        modifier = Modifier.fillMaxSize()
    ){
        val vm = rememberScreenModel { AllFeedbacksModel(userId) }
        BackButton(
            modifier = Modifier.statusBarsPadding(),
            tint = AppColor.blue600,
            text = stringResource(R.string.back),
        )

        HorizontalDivider(Modifier.padding(16.dp))

        LazyColumn {
            ratingSummary(
                avgRating = vm.user?.avgRating,
                feedbacksCount = vm.feedbacks.size
            )

            feedbackItems(
                feedbacks = vm.feedbacks,
                authors = vm.authors
            )
        }

    }

    private fun LazyListScope.ratingSummary(
        avgRating: Double?,
        feedbacksCount: Int,
    ) = item{
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = buildAnnotatedString {
                append(stringResource(R.string.rating))
                append(": ")
                withStyle(SpanStyle(
                    color = AppColor.gray700,
                    fontWeight = FontWeight.Medium
                )){
                    if (feedbacksCount > 0) {
                        avgRating?.roundToInt()?.toString()?.let{ append("⭐\uFE0F$it") }
                    }
                    append(stringResource(R.string.feedbacks_count, feedbacksCount))
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = AppColor.gray900,
            fontSize = 16.sp
        )
    }

    private fun LazyListScope.feedbackItems(
        feedbacks: List<UserFeedback>,
        authors: Map<String, Author>,
    ) = items(feedbacks){ feedback ->
        val author = authors[feedback.fromUserId]
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row {
                AuthorContent(
                    modifier = Modifier.weight(1f),
                    author = author,
                    avatarSize = 32.dp,
                    textStyle = MaterialTheme.typography.titleSmall,
                    textColor = AppColor.gray900
                ) {
                    Text(
                        text = "⭐\uFE0F ${feedback.rating.roundToInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColor.gray900,
                        fontWeight = FontWeight.Bold
                    )
                }
                (feedback.updatedAt?: feedback.createdAt)?.let { date ->
                    val relativeTime by date.rememberRelativeTime()
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColor.gray500,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = feedback.comment.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppColor.gray700
            )
            HorizontalDivider()
        }
    }

}