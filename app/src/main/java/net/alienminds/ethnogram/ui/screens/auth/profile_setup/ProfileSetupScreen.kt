package net.alienminds.ethnogram.ui.screens.auth.profile_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.AsyncImage
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.AppAlertDialog
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.rememberAppDialogState
import net.alienminds.ethnogram.ui.extentions.fields.textFieldColors
import net.alienminds.ethnogram.ui.extentions.rememberPhotoPicker
import net.alienminds.ethnogram.ui.extentions.rootOrThrow
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.screens.session.SessionScreen
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.utils.shimmerEffect

class ProfileSetupScreen(
) : PageTransitionScreen {

    override val position: Int
        get() = 1

    @Composable
    override fun Content() = Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        val navigator = LocalNavigator.rootOrThrow
        val vm = rememberScreenModel { ProfileSetupViewModel() }
        val alertNotSave = rememberAppDialogState()
        vm.onLoginSuccess()
        var loadState by remember { mutableStateOf(false) }

        val photoPicker = rememberPhotoPicker(
            maxPhoto = 5 - vm.images.size,
            onSelect = vm::addImage
        )

        Spacer(Modifier.statusBarsPadding())
        Spacer(Modifier.weight(1f))


        SquareTextCircle(modifier =  Modifier.shimmerEffect(loadState, CircleShape)
            ,vm.images) {
            photoPicker.launch()
        }

        Spacer(Modifier.size(16.dp))

        Text(
            text = stringResource(id = R.string.your_profile),
            style = MaterialTheme.typography.headlineMedium,
            color = AppColor.black
        )
        Spacer(Modifier.size(4.dp))

        TextTitle(
            stringResource(id = R.string.fio_title))

        Spacer(Modifier.size(8.dp))

        InfoField(
            modifier =  Modifier.shimmerEffect(loadState, MaterialTheme.shapes.large),
            vm.name,
            stringResource(R.string.input_name)
        )
        Spacer(Modifier.size(8.dp))
        InfoField(
            modifier =  Modifier.shimmerEffect(loadState, MaterialTheme.shapes.large),
            vm.surname,
            stringResource(R.string.input_surname)
        )
        Spacer(Modifier.size(24.dp))

        TextTitle(
            stringResource(id = R.string.your_bio))
        Spacer(Modifier.size(8.dp))
        InfoField(
            modifier =  Modifier.shimmerEffect(loadState, MaterialTheme.shapes.large),
            vm.bio, stringResource(R.string.your_bio_description))
        Spacer(Modifier.size(8.dp))
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = "Пример: 27yo, UI/UX дизайнер с 3+ лет опыта работы",
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.gray500
        )
        Button(
            modifier = Modifier
                .padding(vertical = 24.dp)
                .shimmerEffect(loadState, MaterialTheme.shapes.large)
                .shadow(
                    elevation = 0.dp,
                    shape = MaterialTheme.shapes.large
                )
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            onClick = {
                if (!loadState){
                loadState = true
                when (vm.canSave) {
                    true -> {
                        vm.saveUser{
                            navigator.replaceAll(SessionScreen())
                            loadState = false
                        }
                    }

                    false -> alertNotSave.show()
                }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColor.blueGray800,
                contentColor = AppColor.white
            )
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = stringResource(R.string.save),
                style = MaterialTheme.typography.bodyLarge,
            )
        }


        Spacer(Modifier.weight(1f))
        Spacer(
            Modifier
                .navigationBarsPadding()
                .height(48.dp)
        )

        AppAlertDialog(
            state = alertNotSave,
            title = stringResource(R.string.alert_onboarding_title),
            text = stringResource(R.string.alert_onboarding_body),
            confirmColor = MaterialTheme.colorScheme.error,
            confirmText = stringResource(R.string.signin),
            dismissText = stringResource(R.string.stay),
            onConfirm = { navigator.replaceAll(SessionScreen()) }
        )
    }

    @Composable
    private fun TextTitle(stringResource: String) {
        Text(
            text = stringResource,
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.gray500
        )
    }

    @Composable
    fun SquareTextCircle(modifier: Modifier,images: List<String?>, click: () -> Unit = {}) {
        Box(
            modifier = modifier
                .size(82.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    click()
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(AppColor.blueGray100),
                contentAlignment = Alignment.Center
            ) {
                if (images.isNotEmpty()) {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = images.first(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "+",
                        style = TextStyle(
                            fontSize = 50.sp,
                            lineHeight = 50.sp,
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        color = AppColor.blueGray700
                    )
                }
            }

            Text(
                text = "📷",
                fontSize = 24.sp,
                color = AppColor.blueGray900,
                modifier = Modifier
            )
        }


    }


    @Composable
    private fun InfoField(modifier: Modifier,text: MutableState<String?>, placeholder: String) {
        TextField(
            value = text.value.orEmpty(),
            onValueChange = { newValue ->
                text.value = newValue
            },
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = AppColor.gray900
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColor.gray500,
                    fontWeight = FontWeight.Medium
                )
            },
            colors = textFieldColors(),
            singleLine = true,
        )
    }


}