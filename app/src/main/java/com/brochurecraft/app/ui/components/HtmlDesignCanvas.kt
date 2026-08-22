package com.brochurecraft.app.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import android.util.Log
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlin.math.roundToInt

/**
 * Matches a `<meta ... name="viewport" ...>` tag regardless of attribute order/quote style,
 * so we can rewrite it to whatever pixel width the current canvas preview needs.
 */
private val viewportMetaRegex = Regex(
    "<meta[^>]*viewport[^>]*>",
    setOf(RegexOption.IGNORE_CASE)
)
private val headOpenRegex = Regex("<head[^>]*>", RegexOption.IGNORE_CASE)

/**
 * Bakes an exact CSS viewport width into the template's <head> *before* it's ever loaded
 * into the WebView. Doing this up front (rather than patching the DOM asynchronously after
 * onPageFinished) guarantees the page's very first layout pass already uses the correct
 * width, and lets us key WebView reloads off this string so a sheet-size change always
 * produces a fresh, correctly-sized render.
 */
private fun withViewportWidth(html: String, widthPx: Int): String {
    val metaTag = "<meta name=\"viewport\" content=\"width=$widthPx\">"
    return when {
        viewportMetaRegex.containsMatchIn(html) -> viewportMetaRegex.replace(html, metaTag)
        headOpenRegex.containsMatchIn(html) -> headOpenRegex.replace(html, "$0$metaTag")
        else -> metaTag + html
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlDesignCanvas(
    htmlContent: String,
    jsCommands: SharedFlow<String>,
    captureRequest: SharedFlow<Unit>? = null,
    onCaptured: ((Bitmap) -> Unit)? = null,
    onElementSelected: (String?) -> Unit,
    onHtmlUpdated: (String) -> Unit,
    onPageFinished: () -> Unit = {},
    isReadOnly: Boolean = false,
    forceDesktop: Boolean = false,
    viewportWidth: Int = 0, // 0 means don't force a breakpoint; otherwise exact CSS px to lay out at
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var webViewWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    // Single source of truth for "what should currently be loaded in the WebView".
    // Recomputed whenever the raw template OR the target viewport width changes, so
    // switching between A4/A3/A5/Mobile/Tablet/Desktop always produces a fresh load
    // with the correct baked-in viewport - no stale/async DOM patch involved.
    val resolvedHtml = remember(htmlContent, viewportWidth) {
        if (viewportWidth > 0) withViewportWidth(htmlContent, viewportWidth) else htmlContent
    }

    // THE ACTUAL FIX: instead of guessing what CSS pixel width the WebView ends up
    // rendering at and trying to pre-compute an external Compose-side scale to compensate
    // (which was the root cause of every previous "doesn't fill the canvas" report - the
    // guess and WebView's real internal rendering width didn't agree), we hand the exact
    // zoom job to the WebView engine itself via setInitialScale(). WebView always knows,
    // with total precision, both (a) the CSS width it laid the page out at (viewportWidth,
    // via the meta tag) and (b) its own real pixel width (webViewWidthPx, measured by
    // Compose) - so it can compute the correct zoom with zero guesswork on our side. This
    // also means the WebView's Android View can simply fillMaxSize() of its container with
    // NO separate scaling layer, NO transformOrigin, and NO risk of ever overflowing its
    // bounds (a plain View can't paint outside its own laid-out area).
    LaunchedEffect(webViewWidthPx, viewportWidth, density) {
        val webView = webViewInstance ?: return@LaunchedEffect
        if (webViewWidthPx <= 0 || viewportWidth <= 0) return@LaunchedEffect
        val widthDp = with(density) { webViewWidthPx.toDp().value }
        val percent = ((widthDp / viewportWidth) * 100f).roundToInt().coerceIn(5, 2000)
        webView.setInitialScale(percent)
    }

    LaunchedEffect(jsCommands) {
        jsCommands.collect { command ->
            webViewInstance?.post {
                webViewInstance?.evaluateJavascript(command, null)
            }
        }
    }

    if (captureRequest != null && onCaptured != null) {
        LaunchedEffect(captureRequest) {
            captureRequest.collect {
                webViewInstance?.post {
                    val webView = webViewInstance ?: return@post
                    if (webView.width > 0 && webView.height > 0) {
                        val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        webView.draw(canvas)
                        onCaptured(bitmap)
                    }
                }
            }
        }
    }

    AndroidView(
        modifier = modifier.onSizeChanged { size -> webViewWidthPx = size.width },
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true

                // Speed optimization
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                settings.textZoom = 100

                // We drive zoom explicitly and deterministically via setInitialScale above -
                // disable the user's own pinch/double-tap zoom so it can never fight that,
                // and so switching sizes always looks the same regardless of prior gestures.
                settings.setSupportZoom(false)
                settings.builtInZoomControls = false
                settings.displayZoomControls = false

                updateSettingsForLayout(forceDesktop)

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (!isReadOnly) {
                            injectEditorBridge(view)
                        }
                        onPageFinished()
                    }
                }

                if (!isReadOnly) {
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onElementSelected(json: String?) {
                            onElementSelected(json)
                        }

                        @JavascriptInterface
                        fun onContentChanged(html: String) {
                            onHtmlUpdated(html)
                        }
                    }, "AndroidBridge")
                }

                webViewInstance = this
                loadDataWithBaseURL("file:///android_asset/templates/", resolvedHtml, "text/html", "UTF-8", null)
                tag = resolvedHtml
            }
        },
        update = { webView ->
            if (webViewInstance != webView) webViewInstance = webView

            webView.updateSettingsForLayout(forceDesktop)

            // Reload whenever the fully-resolved HTML (template + baked-in viewport width)
            // differs from what's currently loaded. This fires both when the template
            // changes AND when the user switches sheet size (A4/Mobile/Tablet/...), since
            // resolvedHtml embeds viewportWidth.
            if (webView.tag != resolvedHtml) {
                webView.tag = resolvedHtml
                webView.loadDataWithBaseURL("file:///android_asset/templates/", resolvedHtml, "text/html", "UTF-8", null)
            }
        }
    )
}

/**
 * Only the explicit "Desktop" preview should emulate a desktop browser. Every other size
 * (Mobile/Tablet/A4/A3/A5) renders as a real mobile WebView would - relying on the baked-in
 * viewport width (see [withViewportWidth]) to drive the template's own responsive CSS.
 * `loadWithOverviewMode` is deliberately left off: it only auto-zooms in ONE direction
 * (shrinks wide content to fit), and only conditionally. We need exact, symmetric zoom in
 * both directions every time, which is why that job is handled explicitly via
 * setInitialScale() above instead.
 */
private fun WebView.updateSettingsForLayout(forceDesktop: Boolean) {
    settings.apply {
        if (forceDesktop) {
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        } else {
            userAgentString = null
        }
        useWideViewPort = true
        loadWithOverviewMode = false
    }
}

private fun injectEditorBridge(webView: WebView?) {
    try {
        val inputStream = webView?.context?.assets?.open("editor_bridge.js")
        val script = inputStream?.bufferedReader()?.use { it.readText() }
        if (script != null) {
            webView.evaluateJavascript(script, null)
        }
    } catch (e: Exception) {
        Log.e("HtmlDesignCanvas", "Failed to inject editor bridge", e)
    }
}
