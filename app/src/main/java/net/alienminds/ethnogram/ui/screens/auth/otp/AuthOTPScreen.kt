package net.alienminds.ethnogram.ui.screens.auth.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.ui.extentions.buttons.BackButton
import net.alienminds.ethnogram.ui.extentions.fields.OtpTextField
import net.alienminds.ethnogram.ui.extentions.rootOrThrow
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.utils.shimmerEffect

class AuthOTPScreen(
    private val verificationId: String,
) : PageTransitionScreen {

    override val position: Int
        get() = 1

    @Composable
    override fun Content() = Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        val navigator = LocalNavigator.rootOrThrow
        val vm = rememberScreenModel { AuthOTPViewModel(verificationId) }

        BackButton(
            modifier = Modifier.statusBarsPadding(),
            tint = AppColor.gray700
        )
        FieldContent(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            otpCode = vm.otpCode,
            loading = vm.loading,
            onOtpChange = { vm.otpCode = it }
        )
        FooterContent(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            enabled = vm.otpCode.length == 6,
            loading = vm.loading,
            onSignIn = { vm.signIn(navigator) },
            onResendCode = vm::resendCode
        )
    }

    @Composable
    private fun FieldContent(
        modifier: Modifier = Modifier,
        otpCode: String,
        loading: Boolean,
        onOtpChange: (String) -> Unit,
    ) = Column(
        modifier = modifier
    ) {
        Icon(
            modifier = Modifier
                .size(48.dp)
                .background(AppColor.blueGray100, CircleShape)
                .padding(8.dp),
            imageVector = Icons.Default.Lock,
            tint = AppColor.blueGray500,
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = stringResource(R.string.auth_otp_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        OtpTextField(
            value = otpCode,
            loading = loading,
            onValueChange = onOtpChange
        )
    }

    @Composable
    private fun FooterContent(
        modifier: Modifier = Modifier,
        enabled: Boolean,
        loading: Boolean,
        onSignIn: () -> Unit,
        onResendCode: () -> Unit,
    ) = Column(
        modifier = modifier.imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            modifier = Modifier
                .shimmerEffect(loading, MaterialTheme.shapes.large)
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.large,
            enabled = enabled,
            onClick = onSignIn
        ) {
            Text(stringResource(R.string.signin))
        }
        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.large,
            onClick = onResendCode
        ) {
            Text(
                text = stringResource(R.string.resend_code),
                color = AppColor.gray600
            )
        }
    }
}