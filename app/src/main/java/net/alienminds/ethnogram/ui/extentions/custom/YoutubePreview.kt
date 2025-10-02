package net.alienminds.ethnogram.ui.extentions.custom

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import net.alienminds.ethnogram.ui.theme.AppColor

/***
 * Компонент для предпросмотра YouTube видео
 *
 * @param modifier Модификатор для настройки компонента
 * @param video Youtube video ID or full link
 * @param params Параметры для настройки предпросмотра YouTube видео
 */
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun YoutubePreview(
    modifier: Modifier = Modifier,
    video: String,//Youtube video ID or full link
    params: YoutubePreviewParams = YoutubePreviewParams(),
    onError: (errorCode: Int) -> Unit = { }
) {
    val videoId = when(video.startsWith("https://www.youtube.com/embed")) {
        true -> extractYoutubeVideoId(video)?: video
        false -> video
    }
    val errorHandler = remember { YoutubePreviewErrorHandler(onError) }
    AndroidView(
        modifier = modifier
            .background(AppColor.red500),
        factory = { context ->
            WebView(context).apply {
                webViewClient = YoutubePreviewWebViewClient()
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                addJavascriptInterface(errorHandler, "client")
                settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36"
                loadData(youtubePreviewHtml(videoId, params), "text/html", "utf-8")
            }
        },
        update = {
            it.loadData(youtubePreviewHtml(videoId, params), "text/html", "utf-8")
        }
    )
}

private class YoutubePreviewErrorHandler(
    private val onErrorHandle: (errorCode: Int) -> Unit
){

    @JavascriptInterface
    fun onError(errorCode: Int) {
        onErrorHandle(errorCode)
    }

}

private class YoutubePreviewWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        val context = view?.context ?: return false
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
        return true // Не загружать ссылку в WebView
    }
}

private fun extractYoutubeVideoId(url: String): String? {
    val regex = Regex("(?:v=|\\/embed\\/|youtu\\.be\\/|\\/v\\/|\\/watch\\?v=)([\\w-]{11})")
    return regex.find(url)?.groups?.get(1)?.value
}

private fun youtubePreviewHtml(videoId: String, params: YoutubePreviewParams): String {

    fun Boolean.toInt() = if (this) 1 else 0

    val paramsMap = mapOf(
        "playsinline" to params.playsInline?.toInt(),
        "autoplay" to params.autoplay?.toInt(),
        "mute" to params.autoplay?.toInt(), // mute=1 нужно для автозапуска
        "controls" to params.controls?.rawValue?.toString(),
        "disablekb" to params.disableKb?.toInt(),
        "enablejsapi" to params.enableJsApi?.toInt(),
        "fs" to params.fullscreenButton?.toInt(),
        "rel" to params.showRelated?.toInt(),
        "loop" to params.loop?.toInt(),
        "start" to params.start?.toString(),
        "end" to params.end?.toString(),
        "cc_lang_pref" to params.ccLangPref,
        "cc_load_policy" to params.ccLoadPolicy?.toInt(),
        "color" to params.color?.rawColor,
        "iv_load_policy" to params.ivLoadPolicy?.rawValue?.toString(),
        "list" to params.list,
        "listType" to params.listType?.rawType,
        "origin" to params.origin,
        "playlist" to params.playlist,
        "hl" to params.interfaceLang,
        "widget_referrer" to params.widgetReferrer
    )
    val playerVars = buildString {
        paramsMap.forEach { (key, value) ->
            value?.let {
                append("'$key': '$value', ")
            }
        }
    }.trim().trimEnd(',')

    return """
<!DOCTYPE html>
<html>
  <head>
    <style>
      body, html {
        margin: 0;
        padding: 0;
        height: 100%;
      }
    </style>
  </head>
  <body>
    <div id="player"></div>
    <script>
      var tag = document.createElement('script');
      tag.src = "https://www.youtube.com/iframe_api";
      var firstScriptTag = document.getElementsByTagName('script')[0];
      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
      var player;
      function onYouTubeIframeAPIReady() {
        player = new YT.Player('player', {
          height: '100%',
          width: '100%',
          videoId: '$videoId',
          playerVars: { $playerVars },
          events: {
            'onError': function(event) {
              var errorCode = event.data;
              window.client.onError(errorCode);
            }
          }
        });
      }
    </script>
  </body>
</html>
""".trimIndent()
}

/***
 * Параметры для настройки предпросмотра YouTube видео
 *
 * @param autoplay Автовоспроизведение видео
 * @param ccLangPref Язык субтитров (ISO 639-1)
 * @param ccLoadPolicy true - субтитры включены по умолчанию
 * @param color Цвет полосы воспроизведения
 * @param controls Элементы управления
 * @param disableKb Отключить управление с клавиатуры
 * @param enableJsApi Управление через JS API
 * @param end Время окончания видео (секунды)
 * @param fullscreenButton true - показать кнопку fullscreen
 * @param interfaceLang Язык интерфейса
 * @param ivLoadPolicy Аннотации
 * @param list Идентификатор плейлиста/канала
 * @param listType Тип списка
 * @param loop Зациклить видео/плейлист
 * @param origin Домен для безопасности
 * @param playlist Список видео через запятую
 * @param playsInline Встроенное воспроизведение
 * @param showRelated true - показывать похожие видео
 * @param start Время начала видео (секунды)
 * @param widgetReferrer URL-адрес виджета
 */
data class YoutubePreviewParams(
    val autoplay: Boolean? = null,
    val ccLangPref: String? = null,
    val ccLoadPolicy: Boolean? = null,
    val color: YoutubePreviewColor? = null,
    val controls: YoutubePreviewControls? = null,
    val disableKb: Boolean? = null,
    val enableJsApi: Boolean? = null,
    val end: Int? = null,
    val fullscreenButton: Boolean? = null,
    val interfaceLang: String? = null,
    val ivLoadPolicy: YoutubePreviewIvLoadPolicy? = null,
    val list: String? = null,
    val listType: YoutubePreviewListType? = null,
    val loop: Boolean? = null,
    val origin: String? = null,
    val playlist: String? = null,
    val playsInline: Boolean? = null,
    val showRelated: Boolean? = null,
    val start: Int? = null,
    val widgetReferrer: String? = null
)

enum class YoutubePreviewControls(val rawValue: Int) {
    NONE(0),
    FULL(1),
    MINIMAL(2)
}

enum class YoutubePreviewColor(val rawColor: String) {
    RED("red"),
    WHITE("white")
}

enum class YoutubePreviewListType(val rawType: String) {
    PLAYLIST("playlist"),
    USER_UPLOADS("user_uploads")
}

enum class YoutubePreviewIvLoadPolicy(val rawValue: Int) {
    DEFAULT(1), // Аннотации включены
    DISABLED(3) // Аннотации отключены
}
