package com.brochurecraft.app.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.util.Log
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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
    val metaTag = "<meta name=\"viewport\" content=\"width=$widthPx, initial-scale=1.0, maximum-scale=1.0\">"
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
    isReadOnly: Boolean = false,
    forceDesktop: Boolean = false,
    viewportWidth: Int = 0, // 0 means use container width; otherwise exact CSS px to render at
    onContentMeasured: ((widthDp: Int, heightDp: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Single source of truth for "what should currently be loaded in the WebView".
    // Recomputed whenever the raw template OR the target viewport width changes, so
    // switching between A4/A3/A5/Mobile/Tablet/Desktop always produces a fresh load
    // with the correct baked-in viewport - no stale/async DOM patch involved.
    val resolvedHtml = remember(htmlContent, viewportWidth) {
        if (viewportWidth > 0) withViewportWidth(htmlContent, viewportWidth) else htmlContent
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
        modifier = modifier,
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

                updateSettingsForLayout(forceDesktop)

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Always measure real content size, edit mode or not, so the
                        // canvas can be scaled to fit the ACTUAL template - not a guess.
                        injectContentSizeObserver(view)
                        if (!isReadOnly) {
                            injectEditorBridge(view)
                        }
                    }
                }

                // Registered unconditionally: onContentMeasured must fire in every mode
                // (edit + export preview), while the edit-only callbacks simply go unused
                // in read-only mode since editor_bridge.js is never injected there.
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onElementSelected(json: String?) {
                        onElementSelected(json)
                    }

                    @JavascriptInterface
                    fun onContentChanged(html: String) {
                        onHtmlUpdated(html)
                    }

                    @JavascriptInterface
                    fun onContentMeasured(widthDp: Int, heightDp: Int) {
                        onContentMeasured?.invoke(widthDp, heightDp)
                    }
                }, "AndroidBridge")

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
            // resolvedHtml embeds viewportWidth - fixing the "switching size does nothing"
            // bug caused by the old content-only comparison.
            if (webView.tag != resolvedHtml) {
                webView.tag = resolvedHtml
                webView.loadDataWithBaseURL("file:///android_asset/templates/", resolvedHtml, "text/html", "UTF-8", null)
            }
        }
    )
}

/**
 * Only the explicit "Desktop" preview should emulate a desktop browser. Every other size
 * (Mobile/Tablet/A4/A3/A5) renders as a real mobile WebView would - relying purely on the
 * baked-in viewport width (see [withViewportWidth]) to drive the template's own responsive
 * CSS, rather than faking a desktop UA/wide-viewport zoom-to-fit trick which fights against
 * an exact target width.
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

/**
 * Measures the template's REAL rendered content size (not the assumed/forced viewport
 * size) and reports it back through AndroidBridge.onContentMeasured, so Compose can scale
 * the canvas to fit the actual content instead of guessing from a paper aspect ratio.
 *
 * A single measurement right after page load isn't enough: these templates pull in
 * Tailwind's CDN build (which injects styles asynchronously) and web fonts (which reflow
 * text once loaded), both of which can change content height well after onPageFinished.
 * A ResizeObserver on <html> keeps re-reporting whenever that settles, with a timer-based
 * fallback for older WebView builds that lack ResizeObserver.
 */
private fun injectContentSizeObserver(webView: WebView?) {
    val script = """
        (function() {
            function reportSize() {
                try {
                    var w = document.documentElement.scrollWidth;
                    var h = document.documentElement.scrollHeight;
                    if (window.AndroidBridge && AndroidBridge.onContentMeasured) {
                        AndroidBridge.onContentMeasured(w, h);
                    }
                } catch (e) {}
            }
            requestAnimationFrame(function() { requestAnimationFrame(reportSize); });
            if (window.ResizeObserver) {
                if (window.__vcResizeObserver) { window.__vcResizeObserver.disconnect(); }
                var ro = new ResizeObserver(function() { reportSize(); });
                ro.observe(document.documentElement);
                window.__vcResizeObserver = ro;
            } else {
                window.addEventListener('load', reportSize);
                setTimeout(reportSize, 500);
                setTimeout(reportSize, 1500);
            }
        })();
    """.trimIndent()
    webView?.evaluateJavascript(script, null)
}