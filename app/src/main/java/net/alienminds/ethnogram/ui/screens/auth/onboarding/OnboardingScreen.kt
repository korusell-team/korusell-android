package net.alienminds.ethnogram.ui.screens.auth.onboarding

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import net.alienminds.ethnogram.ui.screens.auth.phone.AuthPhoneScreen
import net.alienminds.ethnogram.ui.screens.session.SessionScreen
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.utils.AppLaunchServiceImpl
import net.alienminds.ethnogram.utils.shimmerEffect

class OnboardingScreen(
) : PageTransitionScreen {

    override val position: Int
        get() = 1

    @Composable
    override fun Content() = Column(
        modifier = Modifier.background(color =AppColor.white)
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        val navigator = LocalNavigator.rootOrThrow
        AppLaunchServiceImpl(LocalContext.current).markLaunched()


        Spacer(Modifier.statusBarsPadding())
        Spacer(Modifier.weight(1f))

        Image(
            modifier = Modifier
                .fillMaxWidth(),
            painter = painterResource(R.drawable.onboarding_people),
            contentScale = ContentScale.FillWidth,
            contentDescription = null
        )
        Spacer(Modifier.size(24.dp))
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = stringResource(id= R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = AppColor.gray900
        )
        Spacer(Modifier.size(8.dp))
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = stringResource(id= R.string.onboarding_body),
            style = MaterialTheme.typography.titleMedium,
            color = AppColor.gray900
        )
        Spacer(Modifier.size(28.dp))

        Button(
            modifier = Modifier
                .padding(vertical = 24.dp)
                .shadow(
                    elevation = 0.dp,
                    shape = MaterialTheme.shapes.large
                )
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            onClick = {
                navigator.replaceAll(AuthPhoneScreen())

            },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColor.blueGray800,
                contentColor = AppColor.white
            )
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = stringResource(R.string.go_onboarding),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(
            Modifier
                .navigationBarsPadding()
                .height(48.dp)
        )

    }


}