package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.util.Base64
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.Plugin
import com.example.model.TabItem
import com.example.ui.theme.TilcayoGold

class TilcayoBridge(
    private val onPageSummaryRequested: (String) -> Unit
) {
    @JavascriptInterface
    fun requestPageSummary(pageText: String) {
        onPageSummaryRequested(pageText)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: TabItem,
    plugins: List<Plugin>,
    onUrlChanged: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    onCanGoBackForwardChanged: (Boolean, Boolean) -> Unit,
    onPageSummaryRequested: (String) -> Unit,
    webViewHolder: (WebView?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        fileUploadCallback?.onReceiveValue(uris)
        fileUploadCallback = null
    }

    val webView = remember(tab.id) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Setup cookies
            try {
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)
            } catch (_: Exception) {}

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                allowFileAccess = false
                allowContentAccess = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = if (tab.isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT

                if (tab.isDesktopMode) {
                    userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                }
            }

            addJavascriptInterface(
                TilcayoBridge(onPageSummaryRequested),
                "TilcayoBridge"
            )

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    onProgressChanged(newProgress)
                    if (newProgress >= 100) {
                        onLoadingChanged(false)
                    } else {
                        onLoadingChanged(true)
                    }
                    onCanGoBackForwardChanged(view?.canGoBack() == true, view?.canGoForward() == true)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    if (!title.isNullOrBlank()) {
                        onTitleChanged(title)
                    }
                }

                override fun onShowFileChooser(
                    wv: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    fileUploadCallback?.onReceiveValue(null)
                    fileUploadCallback = filePathCallback
                    return try {
                        val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "*/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                        filePickerLauncher.launch(intent)
                        true
                    } catch (_: Exception) {
                        fileUploadCallback?.onReceiveValue(null)
                        fileUploadCallback = null
                        false
                    }
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val targetUrl = request?.url?.toString() ?: return false
                    if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) {
                        onUrlChanged(targetUrl)
                        return false // Let WebView handle web pages
                    }
                    return try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                        context.startActivity(intent)
                        true
                    } catch (_: Exception) {
                        true
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    hasError = false
                    url?.let { onUrlChanged(it) }
                    onLoadingChanged(true)
                    onCanGoBackForwardChanged(view?.canGoBack() == true, view?.canGoForward() == true)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    onLoadingChanged(false)
                    url?.let { currentUrl ->
                        onUrlChanged(currentUrl)
                        onCanGoBackForwardChanged(view?.canGoBack() == true, view?.canGoForward() == true)

                        // Inject active plugins for this URL
                        injectPlugins(view, currentUrl, plugins)
                    }
                }

                @SuppressLint("WebViewClientOnReceivedSslError")
                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    handler?.proceed()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        onLoadingChanged(false)
                        hasError = true
                        errorMessage = error?.description?.toString() ?: "Не удалось загрузить страницу"
                    }
                }

                override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                    onLoadingChanged(false)
                    hasError = true
                    val didCrash = detail?.didCrash() == true
                    errorMessage = if (didCrash) {
                        "Процесс вкладки аварийно завершился. Нажмите «Повторить» для перезагрузки."
                    } else {
                        "Процесс вкладки был выгружен системой. Нажмите «Повторить» для перезагрузки."
                    }
                    // Return true so Android knows the app handled the crash and will not terminate the process
                    return true
                }
            }

            // Load initial target URL if present
            if (tab.url.isNotBlank() && tab.url != "tilcayo://home" && tab.url != "about:blank") {
                loadUrl(tab.url)
            }
        }
    }

    DisposableEffect(tab.id) {
        webViewHolder(webView)
        onDispose {
            webViewHolder(null)
            try {
                webView.stopLoading()
                webView.destroy()
            } catch (_: Exception) {}
        }
    }

    // React to URL changes dynamically
    LaunchedEffect(tab.url) {
        if (tab.url.isNotBlank() && tab.url != "tilcayo://home" && tab.url != "about:blank") {
            if (tab.url != webView.url) {
                hasError = false
                onLoadingChanged(true)
                webView.loadUrl(tab.url)
            }
        }
    }

    // React to Desktop Mode toggle without broken initial reload
    var isFirstDesktopComposition by remember { mutableStateOf(true) }
    LaunchedEffect(tab.isDesktopMode) {
        if (isFirstDesktopComposition) {
            isFirstDesktopComposition = false
            return@LaunchedEffect
        }
        webView.settings.userAgentString = if (tab.isDesktopMode) {
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        } else {
            null
        }
        if (webView.url != null) {
            webView.reload()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { webView },
            update = { wv ->
                if (tab.url.isNotBlank() && tab.url != "tilcayo://home" && tab.url != "about:blank") {
                    if (tab.url != wv.url) {
                        wv.loadUrl(tab.url)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Error overlay with retry button
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = TilcayoGold,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Страница недоступна",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage.ifBlank { "Проверьте подключение к интернету или правильность адреса" },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            hasError = false
                            onLoadingChanged(true)
                            webView.reload()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TilcayoGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Повторить попытку")
                    }
                }
            }
        }

        // Web Loading indicator
        if (tab.isLoading && tab.progress in 1..99) {
            LinearProgressIndicator(
                progress = { tab.progress / 100f },
                color = TilcayoGold,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            )
        }
    }
}

private fun injectPlugins(webView: WebView?, url: String, plugins: List<Plugin>) {
    if (webView == null || url.isBlank() || url == "about:blank") return

    plugins.filter { it.appliesTo(url) }.forEach { plugin ->
        // Inject CSS if present
        if (plugin.cssCode.isNotBlank()) {
            val encodedCss = Base64.encodeToString(plugin.cssCode.toByteArray(), Base64.NO_WRAP)
            val cssScript = """
                (function() {
                    try {
                        const styleId = 'tilcayo-plugin-css-${plugin.id}';
                        let style = document.getElementById(styleId);
                        if (!style) {
                            style = document.createElement('style');
                            style.id = styleId;
                            style.type = 'text/css';
                            style.appendChild(document.createTextNode(atob('$encodedCss')));
                            document.head.appendChild(style);
                        }
                    } catch(e) { console.error('[Tilcayo Plugin CSS Error]', e); }
                })();
            """.trimIndent()
            webView.evaluateJavascript(cssScript, null)
        }

        // Inject JS if present
        if (plugin.jsCode.isNotBlank()) {
            val jsScript = """
                (function() {
                    try {
                        ${plugin.jsCode}
                    } catch(e) { console.error('[Tilcayo Plugin JS Error]', e); }
                })();
            """.trimIndent()
            webView.evaluateJavascript(jsScript, null)
        }
    }
}
