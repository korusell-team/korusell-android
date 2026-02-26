package net.alienminds.ethnogram.ui.screens.session.account

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import net.alienminds.ethnogram.BuildConfig
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.service.user.entities.User
import net.alienminds.ethnogram.ui.extentions.custom.Avatar
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.AppAlertDialog
import net.alienminds.ethnogram.ui.extentions.custom.dialogs.rememberAppDialogState
import net.alienminds.ethnogram.ui.screens.session.NavBarScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.delete.DeleteAccountScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.profile.ProfileScreen
import net.alienminds.ethnogram.ui.theme.AppColor

object AccountScreen: NavBarScreen {

    private fun readResolve(): Any = AccountScreen

    override val title: @Composable (() -> String)
        get() = { stringResource(R.string.nav_profile) }

    override val icon: @Composable (() -> Painter)
        get() = { painterResource(R.drawable.ic_account) }

    override val activeIcon: @Composable (() -> Painter)
        get() = { painterResource(R.drawable.ic_account_filled) }

    override val position: Int
        get() = 1

    @Composable
    override fun Content() = Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        val vm = rememberScreenModel { AccountModel() }
        val navigator = LocalNavigator.current
        val ctx = LocalContext.current
        val tabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        val logoutDialogState = rememberAppDialogState()

        AppAlertDialog(
            state = logoutDialogState,
            title = stringResource(R.string.logout),
            text = stringResource(R.string.confirm_exit_app),
            confirmColor = MaterialTheme.colorScheme.error,
            confirmText = stringResource(R.string.logout),
            dismissText = stringResource(R.string.cancel),
            onConfirm = { vm.logout(navigator) }
        )

        Text(
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.Start)
                .padding(16.dp),
            text = stringResource(R.string.my_profile),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ){
            AnimatedVisibility(
                visible = vm.user != null
            ) {
                ProfilePreviewCard(
                    modifier = Modifier.fillMaxWidth(),
                    user = vm.user?: User(),
                    onClick = {
                        navigator?.push(ProfileScreen(null))
                    }
                )
            }
            ItemsBlockContainer(
                modifier = Modifier.padding(top = 24.dp)
            ){
                val privacyPoliceUrl = stringResource(R.string.terms_confidentiality_link).toUri()
                val privacyPoliceTitle = stringResource(R.string.privacy_police)
                val prohibitedContentPolicyUrl = stringResource(R.string.terms_objectionable_link).toUri()
                val prohibitedContentPolicyTitle = stringResource(R.string.prohibited_content_policy)
                val childSafetyUrl = stringResource(R.string.terms_child_safety_link).toUri()
                val childSafetyTitle = stringResource(R.string.child_safety_policy_descr)

                ClickableItem(
                    icon = painterResource(R.drawable.ic_back_hand),
                    title = privacyPoliceTitle,
                    description = stringResource(R.string.privacy_police_descr),
                    onClick = { tabsIntent.launchUrl(ctx, privacyPoliceUrl) }
                )
                HorizontalDivider()
                ClickableItem(
                    icon = painterResource(R.drawable.ic_report),
                    title = prohibitedContentPolicyTitle,
                    description = stringResource(R.string.prohibited_content_policy_descr),
                    onClick = { tabsIntent.launchUrl(ctx, prohibitedContentPolicyUrl) }
                )
                HorizontalDivider()
                ClickableItem(
                    icon = painterResource(R.drawable.ic_child_care),
                    title = childSafetyTitle,
                    description = stringResource(R.string.terms_child_safety_text),
                    onClick = { tabsIntent.launchUrl(ctx, childSafetyUrl) }
                )
                HorizontalDivider()
                ClickableItem(
                    icon = painterResource(R.drawable.ic_update),
                    title = stringResource(R.string.updates),
                    description = stringResource(R.string.updates_descr, BuildConfig.VERSION_NAME),
                    onClick = { vm.updateApp(ctx) }
                )
            }

            ItemsBlockContainer(
                modifier = Modifier.padding(top = 24.dp)
            ){
                ClickableItem(
                    icon = painterResource(R.drawable.ic_logout),
                    title = when(vm.isAnonymous) {
                        true -> stringResource(R.string.signin_account)
                        false -> stringResource(R.string.logout_with_account)
                    },
                    description = null,
                    onClick = { when(vm.isAnonymous){
                        true -> vm.logout(navigator)
                        false -> logoutDialogState.show()
                    } }
                )
            }

            if (vm.isAnonymous.not()){
                ItemsBlockContainer(
                    modifier = Modifier.padding(top = 24.dp)
                ){
                    ClickableItem(
                        icon = painterResource(R.drawable.ic_delete),
                        title = stringResource(R.string.delete_account),
                        description = stringResource(R.string.delete_account_descr),
                        accent = MaterialTheme.colorScheme.error,
                        onClick = { navigator?.push(DeleteAccountScreen) }
                    )
                }
            }

        }

    }

    @Composable
    private fun ClickableItem(
        modifier: Modifier = Modifier,
        icon: Painter,
        title: String,
        description: String? = null,
        accent: Color = MaterialTheme.colorScheme.primary,
        onClick: () -> Unit,
    ) = Row(
        modifier = modifier
            .heightIn(64.dp)
            .clickable { onClick() }
            .padding(
                vertical = 8.dp,
                horizontal = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ){
        Icon(
            modifier = Modifier.size(24.dp),
            painter = icon,
            contentDescription = null,
            tint = accent
        )

        Column(
            modifier = Modifier.weight(1f)
        ){
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accent.copy(0.5f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }
        }

    }

    @Composable
    private fun ItemsBlockContainer(
        modifier: Modifier = Modifier,
        content: @Composable ColumnScope.() -> Unit
    ) = Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        content = content
    )


    @Composable
    private fun ProfilePreviewCard(
        modifier: Modifier = Modifier,
        user: User,
        onClick: () -> Unit
    ) = Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClick() }
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Avatar(
            modifier = Modifier.size(48.dp),
            model = user.image.firstOrNull(),
            initials = user.initials,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            ),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
        ){
            Text(
                text = user.fullName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.edit_profile_descr),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            modifier = Modifier.height(32.dp),
            painter = painterResource(R.drawable.ic_arrow_forward),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }

}