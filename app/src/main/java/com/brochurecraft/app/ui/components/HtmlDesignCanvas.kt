package com.brochurecraft.app.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.util.Log
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlDesignCanvas(
    htmlContent: String,
    jsCommands: SharedFlow<String>,
    onElementSelected: (String?) -> Unit,
    onHtmlUpdated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var webViewInstance: WebView? = null

    LaunchedEffect(jsCommands) {
        jsCommands.collect { command ->
            webViewInstance?.post {
                webViewInstance?.evaluateJavascript(command, null)
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
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.textZoom = 100
                
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        injectEditorBridge(view)
                    }
                }

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

                webViewInstance = this
                loadDataWithBaseURL("file:///android_asset/templates/", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webViewInstance = it }
    )
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
