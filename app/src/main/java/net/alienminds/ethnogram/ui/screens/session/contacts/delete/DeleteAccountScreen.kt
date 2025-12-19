package net.alienminds.ethnogram.ui.screens.session.contacts.delete

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.ui.extentions.buttons.BackButton
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.AppAlertDialog
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.rememberAppDialogState
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.ui.theme.EthnogramTheme

object DeleteAccountScreen: PageTransitionScreen {

    private fun readResolve(): Any = DeleteAccountScreen

    override val position: Int
        get() = 2

    @Composable
    override fun Content() = Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        val navigator = LocalNavigator.current
        val vm = rememberScreenModel { DeleteAccountModel() }
        val deleteAccountDialogState = rememberAppDialogState()

        AppAlertDialog(
            state = deleteAccountDialogState,
            title = stringResource(R.string.delete_account_q),
            text = stringResource(R.string.confirm_delete_account),
            confirmColor = MaterialTheme.colorScheme.error,
            confirmText = stringResource(R.string.delete),
            dismissText = stringResource(R.string.cancel),
            onConfirm = { vm.deleteAccount(navigator) }
        )

        Toolbar(
            modifier = Modifier.statusBarsPadding()
        )

        Icon(
            modifier = Modifier.size(74.dp),
            painter = painterResource(R.drawable.ic_warning_fill),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = stringResource(R.string.delete_account_q),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 32.dp),
            text = stringResource(R.string.delete_account_descr2),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(0.5f))

        ElevatedCard(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
        ) {
            CardItem(
                icon = painterResource(R.drawable.ic_person),
                title = stringResource(R.string.profile_and_personal_data),
                accent = AppColor.blue400
            )
            CardItem(
                icon = painterResource(R.drawable.ic_docs),
                title = stringResource(R.string.all_publications),
                accent = AppColor.orange400
            )
            CardItem(
                icon = painterResource(R.drawable.ic_star),
                title = stringResource(R.string.feedbacks_and_ratings),
                accent = AppColor.yellow600
            )
        }

        Spacer(Modifier.weight(0.5f))

        Button(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            onClick = { deleteAccountDialogState.show() },
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = null,
            )
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = stringResource(R.string.delete_accout_btn),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Button(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            onClick = { navigator?.pop() },
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = stringResource(R.string.cancel),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(Modifier.weight(1f))

    }

    @Composable
    private fun CardItem(
        modifier: Modifier = Modifier,
        icon: Painter,
        title: String,
        accent: Color
    ) = Row(
        modifier = modifier.padding(
            horizontal = 16.dp,
            vertical = 8.dp
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Icon(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = accent.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .padding(8.dp),
            painter = icon,
            contentDescription = title,
            tint = accent
        )
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(R.drawable.ic_close),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
        )
    }

    @Composable
    private fun Toolbar(
        modifier: Modifier = Modifier
    ) = Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ){
        BackButton()
    }
}

@Preview(device = "spec:width=1080px,height=2340px,dpi=440,isRound=true",
    uiMode = Configuration.UI_MODE_TYPE_NORMAL, showSystemUi = true
)
@Composable
private fun PreviewDeleteAccountScreen() = EthnogramTheme{
    DeleteAccountScreen.Content()
}