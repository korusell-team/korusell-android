package net.alienminds.ethnogram.ui.screens.auth.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.ui.extentions.rootOrThrow
import net.alienminds.ethnogram.ui.extentions.shimmerState
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.theme.AppColor
import org.koin.core.component.KoinComponent

class OnboardingScreen(
) : PageTransitionScreen, KoinComponent {

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
        val vm = rememberScreenModel { OnboardingModel{ navigator } }

        Spacer(Modifier.statusBarsPadding())
        Spacer(Modifier.weight(1f))

        var imageWidthPx by remember { mutableIntStateOf(0) }
        val density = LocalDensity.current
        val context = LocalContext.current
        val imageModifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val width = coordinates.size.width
                if (width > 0 && width != imageWidthPx) {
                    imageWidthPx = width
                }
            }
        val imageRequest = remember(imageWidthPx) {
            val width = if (imageWidthPx > 0) imageWidthPx else with(density) { 360.dp.roundToPx() }
            ImageRequest.Builder(context)
                .data(R.drawable.onboarding_people)
                .size(width)
                .build()
        }
        SubcomposeAsyncImage(
            modifier = imageModifier,
            model = imageRequest,
            contentScale = ContentScale.FillWidth,
            contentDescription = null
        )
        Spacer(Modifier.size(24.dp))
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = AppColor.gray900
        )
        Spacer(Modifier.size(8.dp))
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = stringResource(R.string.onboarding_body),
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
                .fillMaxWidth()
                .shimmerState(vm.loading),
            shape = MaterialTheme.shapes.large,
            onClick = { vm.goNext() },
            enabled = vm.loading.not(),
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

        Spacer(Modifier
            .navigationBarsPadding()
            .height(48.dp))

    }


}