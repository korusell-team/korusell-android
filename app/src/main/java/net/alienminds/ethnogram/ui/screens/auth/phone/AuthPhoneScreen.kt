package net.alienminds.ethnogram.ui.screens.auth.phone

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.ui.theme.EthnogramTheme
import net.alienminds.ethnogram.utils.UniversalPhoneVisualTransformation
import net.alienminds.ethnogram.utils.openLink
import net.alienminds.ethnogram.utils.phoneToFbPhone

internal class AuthPhoneScreen: PageTransitionScreen {

    override val position: Int
        get() = 0

    @Composable
    override fun Content() = Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ){
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        var phone by remember { mutableStateOf("") }

        val vm = rememberScreenModel { AuthPhoneViewModel() }
        val state by vm.state.collectAsState()

        Spacer(Modifier
            .fillMaxWidth()
            .height(1.dp))

        FieldContent(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            error = state.errorMessage,
            phone = phone,
            onPhoneChange = { phone = it }
        )

        FooterContent(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            enabled = phone.length >= 9,
            onClick = {
                vm.signIn(context, navigator,   phoneToFbPhone(phone))
            }
        )

    }



    @Composable
    private fun FieldContent(
        modifier: Modifier = Modifier,
        error: String?,
        phone: String,
        onPhoneChange: (String) -> Unit
    ) = Column(
        modifier = modifier
    ){
        Icon(
            modifier = Modifier
                .size(48.dp)
                .background(AppColor.blueGray100, CircleShape)
                .padding(8.dp),
            painter = painterResource(R.drawable.ic_smartphone),
            tint = AppColor.blueGray500,
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = stringResource(R.string.auth_phone_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )


        TextField(
            value = phone,
            onValueChange = { newValue ->
                onPhoneChange(newValue.filter { it.isDigit() }.take(12))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = AppColor.gray900
            ),
            placeholder = {
                Text(
                    text = stringResource(R.string.phone_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColor.gray500,
                    fontWeight = FontWeight.Medium
                )
            },
            visualTransformation = UniversalPhoneVisualTransformation(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = AppColor.blueGray100,
                unfocusedContainerColor = AppColor.blueGray100,
                disabledContainerColor = AppColor.blueGray100,
                errorContainerColor = AppColor.blueGray100,
                cursorColor = AppColor.lightBlue800
            ),
            singleLine = true,
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = error.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }

    @Composable
    private fun FooterContent(
        modifier: Modifier = Modifier,
        enabled: Boolean,
        onClick: () -> Unit
    ) = Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        val ctx = LocalContext.current

        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = buildAnnotatedString {
                append(stringResource(R.string.auth_terms_1))
                appendTermsLink(TermsLinks.OBJECTIONABLE_CONTENT, ctx)
                append(stringResource(R.string.auth_terms_2))
                appendTermsLink(TermsLinks.CONFIDENTIALITY, ctx)
            },
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = AppColor.gray500
        )
        Button(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .imePadding()
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.large,
            enabled = enabled,
            onClick = onClick
        ){
            Text(stringResource(R.string.next))
        }
    }


    private fun AnnotatedString.Builder.appendTermsLink(
        link: TermsLinks,
        context: Context
    ) = withLink(
        LinkAnnotation.Url(
            url = context.getString(link.urlId),
            styles = TextLinkStyles(SpanStyle(
                fontWeight = FontWeight.Bold,
                color = AppColor.gray600
            )),
            linkInteractionListener = {
                context.openLink(context.getString(link.urlId))
            }
        ),
        block = { append(context.getString(link.textId)) }
    )

    private enum class TermsLinks(
        @StringRes val urlId: Int,
        @StringRes val textId: Int
    ){
        OBJECTIONABLE_CONTENT(R.string.terms_objectionable_link, R.string.terms_objectionable_text),
        CONFIDENTIALITY(R.string.terms_confidentiality_link, R.string.terms_confidentiality_text)
    }
}


@Preview
@Composable
private fun PreviewAuthPhoneScreen(){
    EthnogramTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            remember { AuthPhoneScreen() }.Content()
        }
    }
}