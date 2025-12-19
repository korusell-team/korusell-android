package net.alienminds.ethnogram.ui.extentions.custom.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val KAKAO_POSTCODE_URL = "https://appassets.androidplatform.net/assets/kakao_postcode.html"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressPickerDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    theme: PostcodeTheme = PostcodeTheme.default(),
    onDismissRequest: () -> Unit,
    onAddressPicked: (PostcodeAddress) -> Unit
) {
    if (visible){
        val scope = rememberCoroutineScope()
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val onClose: () -> Unit = {
            scope
                .launch { state.hide() }
                .invokeOnCompletion {
                    if (!state.isVisible) {
                        onDismissRequest()
                    }
                }
        }

        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            sheetState = state,
        ) {
            PostcodeContent(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxSize(),
                theme = theme,
                onAddress = {
                    onAddressPicked(it)
                    onClose()
                }
            )
        }
    }
}

@Composable
private fun PostcodeContent(
    modifier: Modifier = Modifier,
    theme: PostcodeTheme = PostcodeTheme.default(),
    onAddress: (PostcodeAddress) -> Unit,
) = Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
){
    CircularProgressIndicator()

    val webView = rememberWebView(theme, onAddress)
    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize()
    )
}

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
private fun rememberWebView(
    theme: PostcodeTheme,
    onAddress: (PostcodeAddress) -> Unit
): WebView {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    safeBrowsingEnabled = true
                }
                useWideViewPort = true
            }
            addJavascriptInterface(
                PostcodeInterface(onAddress),
                "Android"
            )
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )



            webViewClient = PostcodeWebClient(context)
            setOnTouchListener { v, event ->
                v.parent?.requestDisallowInterceptTouchEvent(true)
                v.onTouchEvent(event)
            }
            visibility = ViewGroup.INVISIBLE
            loadUrl("$KAKAO_POSTCODE_URL?theme=${theme.getBase64()}")
        }
    }

    return webView
}

private class PostcodeWebClient(context: Context): WebViewClient(){

    private val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .build()

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        view?.visibility = ViewGroup.VISIBLE
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        view?.visibility = ViewGroup.INVISIBLE
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return request?.url?.let {
            assetLoader.shouldInterceptRequest(it)
        }?: super.shouldInterceptRequest(view, request)
    }
}

private class PostcodeInterface(
    private val onAddress: (PostcodeAddress) -> Unit
) {

    @JavascriptInterface
    fun onSelect(data: String) {
        Log.d("PostcodeInterface", "onSelect called with: $data")
        try {
            val json = Json {
                ignoreUnknownKeys = true
            }
            val addressData = json.decodeFromString<PostcodeAddress>(data)
            onAddress(addressData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

/***
 * Адрес, полученный из страницы выбора почтового индекса
 * @param address Адрес, который отображается в первой строке результатов поиска, меняется в зависимости от типа поискового запроса (адрес улицы/название дороги)
 */
@Serializable
data class PostcodeAddress(//Дополнить при необходимости
    @SerialName("address") val address: String,
)


/***
 * Тема для страницы выбора почтового индекса
 * @param backgroundColor Цвет фона
 * @param searchBackgroundColor Цвет фона строки поиска
 * @param contentBackgroundColor Цвет фона основного текста
 * @param pageBackgroundColor Цвет фона страницы
 * @param textColor Цвет основного текста
 * @param queryTextColor Цвет текста поля поиска
 * @param postcodeTextColor Цвет текста почтового индекса
 * @param emphTextColor Цвет выделенного текста
 * @param outlineColor Цвет контура
 */
data class PostcodeTheme(
    val backgroundColor: Color,
    val searchBackgroundColor: Color,
    val contentBackgroundColor: Color,
    val pageBackgroundColor: Color,
    val textColor: Color,
    val queryTextColor: Color,
    val postcodeTextColor: Color,
    val emphTextColor: Color,
    val outlineColor: Color,
){
    //TODO: Use serialization library
    fun getJson() = """
{
  "bgColor": "${backgroundColor.toCssHex()}",
  "searchBgColor": "${searchBackgroundColor.toCssHex()}",
  "contentBgColor": "${contentBackgroundColor.toCssHex()}",
  "pageBgColor": "${pageBackgroundColor.toCssHex()}",
  "textColor": "${textColor.toCssHex()}",
  "queryTextColor": "${queryTextColor.toCssHex()}",
  "postcodeTextColor": "${postcodeTextColor.toCssHex()}",
  "emphTextColor": "${emphTextColor.toCssHex()}",
  "outlineColor": "${outlineColor.toCssHex()}"
}
""".trimIndent()

    private fun Color.toCssHex(): String {
        return String.format(
            "#%02X%02X%02X",
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt()
        )
    }

    fun getBase64(): String =
        Base64.encodeToString(getJson().toByteArray(), Base64.NO_WRAP)

    companion object{

        @Composable
        fun default(
            backgroundColor: Color = MaterialTheme.colorScheme.background,
            searchBackgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentBackgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
            pageBackgroundColor: Color = MaterialTheme.colorScheme.background,
            textColor: Color = contentColorFor(backgroundColor),
            queryTextColor: Color = contentColorFor(searchBackgroundColor),
            postcodeTextColor: Color = MaterialTheme.colorScheme.error,
            emphTextColor: Color = MaterialTheme.colorScheme.primary,
            outlineColor: Color = MaterialTheme.colorScheme.outline,
        ) = PostcodeTheme(
            backgroundColor = backgroundColor,
            searchBackgroundColor = searchBackgroundColor,
            contentBackgroundColor = contentBackgroundColor,
            pageBackgroundColor = pageBackgroundColor,
            textColor = textColor,
            queryTextColor = queryTextColor,
            postcodeTextColor = postcodeTextColor,
            emphTextColor = emphTextColor,
            outlineColor = outlineColor,
        )
    }
}