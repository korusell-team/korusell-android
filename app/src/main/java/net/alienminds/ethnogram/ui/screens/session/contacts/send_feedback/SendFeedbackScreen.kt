package net.alienminds.ethnogram.ui.screens.session.contacts.send_feedback

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.ui.extentions.buttons.BackButton
import net.alienminds.ethnogram.ui.extentions.shimmerState
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.theme.AppColor

internal class SendFeedbackScreen(
    private val userId: String,
    private val currentRating: Double? = null,
    private val currentComment: String? = null,
): PageTransitionScreen {

    override val position: Int
        get() = 3

    private val editMode
        get() = currentComment.isNullOrEmpty().not() && currentRating != null

    @Composable
    override fun Content(){
        val ctx = LocalContext.current
        val navigator = LocalNavigator.current
        val vm = rememberScreenModel { SendFeedbackModel(
            userId = userId,
            currentComment = currentComment,
            currentRating = currentRating
        ) }
        BackHandler(vm.loading){
            Toast.makeText(
                ctx,
                ctx.getString(R.string.please_wait_load_feedback),
                Toast.LENGTH_SHORT
            ).show()
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                modifier = Modifier.statusBarsPadding()
            ){
                BackButton(
                    tint = AppColor.blue600,
                    text = stringResource(R.string.back),
                    enabled = vm.loading.not()
                )
                Spacer(Modifier.weight(1f))
                if (editMode) {
                    TextButton(
                        onClick = { vm.removeFeedback { navigator?.pop() } }
                    ) {
                        Text(
                            text = "Удалить",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.share_your_expirence),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp),
                text = stringResource(R.string.send_feedback_descr),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
            Column {
                RatingPicker(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .fillMaxWidth(),
                    rating = vm.rating,
                    onChangeRating = {
                        vm.rating = it
                        vm.errorRating = false
                    },
                    enabled = vm.loading.not()
                )

                AnimatedVisibility(vm.errorRating) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        text = stringResource(R.string.err_rating_empty),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            OutlinedTextField(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                value = vm.comment,
                onValueChange = {
                    vm.comment = it
                    vm.errorComment = false
                },
                minLines = 4,
                maxLines = 6,
                readOnly = vm.loading,
                isError = vm.errorComment,
                shape = MaterialTheme.shapes.medium,
                placeholder = {
                    Text(stringResource(R.string.write_your_comment))
                },
                supportingText = {
                    AnimatedVisibility(vm.errorComment) {
                        Text(stringResource(R.string.err_comment_empty))
                    }
                }
            )
            Button(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .shimmerState(vm.loading),
//                enabled = vm.loading.not(),
                onClick = { if (vm.loading.not()) vm.sendFeedback { navigator?.pop() } },
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = stringResource(R.string.send)
                )
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }

    @Composable
    private fun RatingPicker(
        modifier: Modifier = Modifier,
        rating: Double?,//0..5
        onChangeRating: (Double?) -> Unit,
        enabled: Boolean = true
    ) = Row(
        modifier = modifier
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        mapOf(
            "\uD83E\uDD2C" to 1.0,
            "\uD83D\uDE1E" to 2.0,
            "\uD83D\uDE10" to 3.0,
            "\uD83D\uDE42" to 4.0,
            "\uD83D\uDE0D" to 5.0,
        ).forEach { (emoji, value) ->
            val selected = rating == value
            val ratColor = value.ratingColor()
            val bgColor1 by animateColorAsState(
                when (selected) {
                    true -> ratColor.first
                    false -> AppColor.gray200
                }
            )
            val bgColor2 by animateColorAsState(
                when (selected) {
                    true -> ratColor.second
                    false -> AppColor.gray200
                }
            )
            val scaleDp by animateDpAsState(
                when (selected) {
                    true -> 8.dp
                    false -> 0.dp
                }
            )
            Box(
                modifier = Modifier
                    .size(40.dp + scaleDp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(bgColor1, bgColor2)))
                    .clickable(enabled) { onChangeRating(value.takeIf { rating != value }) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp
                )
            }
        }
    }

    private fun Double.ratingColor() = when(this){
        in 0.0..1.0 -> AppColor.red400 to AppColor.orange400
        in 1.1..2.0 -> AppColor.orange400 to AppColor.deepOrange400
        in 2.1..3.0 -> AppColor.deepOrange400 to AppColor.yellow400
        in 3.1..4.0 -> AppColor.yellow400 to AppColor.green400
        in 4.1..5.0 -> AppColor.green400 to AppColor.orange100
        else -> AppColor.gray400 to AppColor.gray600
    }

}