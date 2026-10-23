package com.example

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.RecognizerIntent
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.util.TilcayoTtsManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.TilcayoAiManager
import com.example.data.BrowserRepository
import com.example.data.PluginRepository
import com.example.data.SearchHistoryRepository
import com.example.data.ThemeRepository
import com.example.model.ChatMessage
import com.example.model.SearchEngine
import com.example.model.ThemeMode
import com.example.model.TilcayoModel
import com.example.model.UserWallpaperState
import com.example.model.WallpaperPresets
import com.example.ui.components.AboutLicenseSheet
import com.example.ui.components.AnimatedWallpaperView
import com.example.ui.components.BrowserWebView
import com.example.ui.components.PluginStoreSheet
import com.example.ui.components.SmartTilcayoDialog
import com.example.ui.components.TabsSheet
import com.example.ui.components.ThemeCustomizerSheet
import com.example.ui.components.TilcayoHomeHeader
import com.example.ui.components.TilcayoLensDialog
import com.example.ui.components.WebsiteTopBar
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TilcayoGold
import com.example.util.configureAppCheck
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: App Check contract with Intent
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val themeRepo = remember { ThemeRepository(context) }
            val wallpaperState by themeRepo.wallpaperState.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (wallpaperState.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemDark
            }

            MyApplicationTheme(darkTheme = isDark) {
                TilcayoBrowserApp(
                    themeRepo = themeRepo,
                    wallpaperState = wallpaperState,
                    isDarkTheme = isDark
                )
            }
        }
    }
}

@Composable
fun TilcayoBrowserApp(
    themeRepo: ThemeRepository,
    wallpaperState: UserWallpaperState,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val browserRepo = remember { BrowserRepository(context) }
    val pluginRepo = remember { PluginRepository(context) }
    val aiManager = remember { TilcayoAiManager() }
    val searchHistoryRepo = remember { SearchHistoryRepository(context) }
    val ttsManager = remember { TilcayoTtsManager(context) }
    val speakingMessageId by ttsManager.speakingMessageId.collectAsState()

    DisposableEffect(ttsManager) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    val tabs by browserRepo.tabs.collectAsState()
    val activeTabId by browserRepo.activeTabId.collectAsState()
    val bookmarks by browserRepo.bookmarks.collectAsState()
    val installedPlugins by pluginRepo.installedPlugins.collectAsState()
    val storeCatalog by pluginRepo.storePlugins.collectAsState()
    val currentEngine by browserRepo.searchEngine.collectAsState()
    val recentQueries by searchHistoryRepo.recentQueries.collectAsState()

    val activeTab = remember(tabs, activeTabId) {
        tabs.find { it.id == activeTabId } ?: tabs.firstOrNull() ?: browserRepo.getActiveTab()
    }

    var searchQuery by remember { mutableStateOf("") }
    var activeWebView by remember { mutableStateOf<WebView?>(null) }

    // Dialog sheets
    var showTabsSheet by remember { mutableStateOf(false) }
    var showPluginStore by remember { mutableStateOf(false) }
    var showSmartTilcayo by remember { mutableStateOf(false) }
    var showTilcayoLens by remember { mutableStateOf(false) }
    var showThemeCustomizer by remember { mutableStateOf(false) }
    var showAboutLicense by remember { mutableStateOf(false) }
    var initialAiPrompt by remember { mutableStateOf("") }

    // Smooth Material 3 blur animation for transitions
    val contentBlur by animateDpAsState(
        targetValue = if (showSmartTilcayo || showTilcayoLens) 16.dp else 0.dp,
        animationSpec = tween(durationMillis = 350),
        label = "content_blur"
    )

    // AI Chat state
    var selectedModel by remember { mutableStateOf(TilcayoModel.DEFAULT) }
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var activeAiJob by remember { mutableStateOf<Job?>(null) }

    // Voice recognition launcher
    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                searchQuery = spokenText
                searchHistoryRepo.addQuery(spokenText)
                val targetUrl = browserRepo.formatSearchOrUrl(spokenText, currentEngine)
                browserRepo.updateActiveTab { it.copy(url = targetUrl, title = spokenText) }
            }
        }
    }

    fun startVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите поисковый запрос для Тилкайо...")
        }
        try {
            voiceSearchLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Голосовой ввод недоступен на этом устройстве", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendAiMessage(promptText: String, imageBitmap: Bitmap?, useGoogleSearch: Boolean = false) {
        val userMsgId = UUID.randomUUID().toString()
        val assistantMsgId = UUID.randomUUID().toString()
        val currentModelName = selectedModel.displayName

        val userMessage = ChatMessage(
            id = userMsgId,
            text = promptText,
            isUser = true,
            imageBitmap = imageBitmap
        )
        val assistantPlaceholder = ChatMessage(
            id = assistantMsgId,
            text = "",
            isUser = false,
            isStreaming = true,
            modelName = currentModelName
        )

        chatMessages = chatMessages + userMessage + assistantPlaceholder

        activeAiJob?.cancel()
        activeAiJob = scope.launch {
            try {
                aiManager.streamReply(
                    prompt = promptText,
                    history = chatMessages,
                    modelType = selectedModel,
                    imageBitmap = imageBitmap,
                    useGoogleSearch = useGoogleSearch
                ).collect { chunk ->
                    chatMessages = chatMessages.map { msg ->
                        if (msg.id == assistantMsgId) {
                            msg.copy(
                                text = msg.text + chunk,
                                isStreaming = true,
                                modelName = currentModelName
                            )
                        } else msg
                    }
                }
                chatMessages = chatMessages.map { msg ->
                    if (msg.id == assistantMsgId) msg.copy(isStreaming = false, modelName = currentModelName) else msg
                }
            } catch (e: Exception) {
                chatMessages = chatMessages.map { msg ->
                    if (msg.id == assistantMsgId) {
                        msg.copy(
                            text = if (msg.text.isBlank()) "🐾 Мяу, возникла ошибка: ${e.localizedMessage ?: "Не удалось получить ответ"}" else msg.text,
                            isStreaming = false,
                            modelName = currentModelName
                        )
                    } else msg
                }
            }
        }
    }

    // Back button handling
    BackHandler(enabled = true) {
        if (showSmartTilcayo) {
            showSmartTilcayo = false
        } else if (showTilcayoLens) {
            showTilcayoLens = false
        } else if (showThemeCustomizer) {
            showThemeCustomizer = false
        } else if (showPluginStore) {
            showPluginStore = false
        } else if (showTabsSheet) {
            showTabsSheet = false
        } else if (activeWebView?.canGoBack() == true) {
            activeWebView?.goBack()
        } else if (activeTab.url != "tilcayo://home") {
            browserRepo.updateActiveTab { it.copy(url = "tilcayo://home", title = "Тилкайо Домой") }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .blur(contentBlur)
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            BrowserBottomBar(
                canGoBack = activeTab.canGoBack,
                canGoForward = activeTab.canGoForward,
                tabCount = tabs.size,
                pluginCount = installedPlugins.count { it.isEnabled },
                onBack = { activeWebView?.goBack() },
                onForward = { activeWebView?.goForward() },
                onHome = {
                    searchQuery = ""
                    browserRepo.updateActiveTab { it.copy(url = "tilcayo://home", title = "Тилкайо Домой") }
                },
                onOpenTabs = { showTabsSheet = true },
                onOpenPlugins = { showPluginStore = true }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permanent Top Address Bar on all screens with progress indicator & stop action
            WebsiteTopBar(
                url = activeTab.url,
                title = activeTab.title,
                isLoading = activeTab.isLoading,
                progress = activeTab.progress,
                isBookmarked = browserRepo.isBookmarked(activeTab.url),
                isDesktopMode = activeTab.isDesktopMode,
                searchEngine = currentEngine,
                onToggleBookmark = { browserRepo.toggleBookmark(activeTab) },
                onToggleDesktop = {
                    browserRepo.updateActiveTab { it.copy(isDesktopMode = !it.isDesktopMode) }
                },
                onReload = { activeWebView?.reload() },
                onStopLoading = {
                    activeWebView?.stopLoading()
                    browserRepo.updateActiveTab { it.copy(isLoading = false) }
                },
                onHome = {
                    browserRepo.updateActiveTab { it.copy(url = "tilcayo://home", title = "Тилкайо Домой", isLoading = false) }
                },
                onNavigate = { targetUrl ->
                    val formatted = browserRepo.formatSearchOrUrl(targetUrl, currentEngine)
                    browserRepo.updateActiveTab { it.copy(url = formatted, title = targetUrl, isLoading = true, progress = 15) }
                }
            )

            // Main Web View or Home Screen with customizable wallpaper
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (activeTab.url == "tilcayo://home") {
                    // Customizable Wallpaper / Gradient / Live Animated Background
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (wallpaperState.isAnimated) {
                            val activeWpPlugin = installedPlugins.find { it.id == wallpaperState.activeWallpaperPluginId }
                            AnimatedWallpaperView(
                                presetId = wallpaperState.animatedPresetId,
                                plugin = activeWpPlugin,
                                isDarkTheme = isDarkTheme,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Subtle acrylic contrast overlay to ensure search and text are always crystal clear
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (isDarkTheme) Color(0x660E1118) else Color(0x33FFFFFF))
                            )
                        } else if (wallpaperState.customImageUri != null) {
                            AsyncImage(
                                model = wallpaperState.customImageUri,
                                contentDescription = "Пользовательские обои",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Adaptive acrylic overlay to preserve contrast
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (isDarkTheme) Color(0xD90E1118) else Color(0xB3FFFFFF))
                            )
                        } else {
                            val activePreset = WallpaperPresets.getPreset(wallpaperState.presetId)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(activePreset.colors)
                                    )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            TilcayoHomeHeader(
                                searchQuery = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearch = { query ->
                                    searchHistoryRepo.addQuery(query)
                                    val targetUrl = browserRepo.formatSearchOrUrl(query, currentEngine)
                                    browserRepo.updateActiveTab { it.copy(url = targetUrl, title = query, isLoading = true, progress = 15) }
                                },
                                currentEngine = currentEngine,
                                onEngineChange = { engine ->
                                    browserRepo.setSearchEngine(engine)
                                },
                                onOpenAiMode = {
                                    initialAiPrompt = searchQuery
                                    showSmartTilcayo = true
                                },
                                onOpenLens = {
                                    showTilcayoLens = true
                                },
                                onOpenPluginStore = { showPluginStore = true },
                                onOpenThemeCustomizer = { showThemeCustomizer = true },
                                onOpenLicense = { showAboutLicense = true },
                                onVoiceClick = { startVoiceSearch() },
                                onOpenUrl = { url ->
                                    browserRepo.updateActiveTab { it.copy(url = url, title = url, isLoading = true, progress = 15) }
                                },
                                bookmarks = bookmarks,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { themeRepo.toggleThemeMode() },
                                recentQueries = recentQueries,
                                onDeleteRecentQuery = { searchHistoryRepo.removeQuery(it) },
                                onClearAllHistory = { searchHistoryRepo.clearAll() }
                            )
                        }
                    }
                } else {
                    // Embedded WebView with custom ChromeClient and DownloadListener
                    BrowserWebView(
                        tab = activeTab,
                        plugins = installedPlugins.filter { it.isEnabled },
                        onUrlChanged = { newUrl ->
                            browserRepo.updateActiveTab { it.copy(url = newUrl) }
                        },
                        onTitleChanged = { newTitle ->
                            browserRepo.updateActiveTab { it.copy(title = newTitle) }
                        },
                        onProgressChanged = { progress ->
                            browserRepo.updateActiveTab { it.copy(progress = progress) }
                        },
                        onLoadingChanged = { loading ->
                            browserRepo.updateActiveTab { it.copy(isLoading = loading) }
                        },
                        onCanGoBackForwardChanged = { canBack, canFwd ->
                            browserRepo.updateActiveTab { it.copy(canGoBack = canBack, canGoForward = canFwd) }
                        },
                        onPageSummaryRequested = { pageText ->
                            initialAiPrompt = "Сделай краткое содержание страницы:\n$pageText"
                            showSmartTilcayo = true
                        },
                        webViewHolder = { webView ->
                            activeWebView = webView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // TABS MANAGEMENT MODAL SHEET
    if (showTabsSheet) {
        TabsSheet(
            tabs = tabs,
            activeTabId = activeTabId,
            onSelectTab = { tabId ->
                browserRepo.selectTab(tabId)
                showTabsSheet = false
            },
            onCloseTab = { tabId ->
                browserRepo.closeTab(tabId)
                if (browserRepo.tabs.value.isEmpty()) {
                    showTabsSheet = false
                }
            },
            onNewTab = { isIncognito ->
                browserRepo.openNewTab("tilcayo://home", isIncognito)
                searchQuery = ""
                showTabsSheet = false
            },
            onDismiss = { showTabsSheet = false }
        )
    }

    // EXTENSION & PLUGIN STORE MODAL SHEET
    if (showPluginStore) {
        PluginStoreSheet(
            installedPlugins = installedPlugins,
            storeCatalog = storeCatalog,
            onTogglePlugin = { pluginId, isEnabled ->
                pluginRepo.togglePlugin(pluginId, isEnabled)
            },
            onInstallPlugin = { plugin ->
                pluginRepo.installPlugin(plugin)
                Toast.makeText(context, "Плагин «${plugin.name}» установлен!", Toast.LENGTH_SHORT).show()
            },
            onUninstallPlugin = { pluginId ->
                pluginRepo.uninstallPlugin(pluginId)
            },
            onSaveCustomPlugin = { plugin ->
                pluginRepo.saveOrUpdateCustomPlugin(plugin)
            },
            onPublishPlugin = { plugin ->
                pluginRepo.publishPluginToStore(plugin)
            },
            onDismiss = { showPluginStore = false }
        )
    }

    // TILCAYO LENS VISUAL SEARCH MODAL
    TilcayoLensDialog(
        visible = showTilcayoLens,
        currentEngine = currentEngine,
        onEngineChange = { browserRepo.setSearchEngine(it) },
        onSearchVisual = { engine, _ ->
            showTilcayoLens = false
            browserRepo.updateActiveTab { it.copy(url = engine.visualSearchUrl, title = "Объектив: ${engine.displayName}") }
        },
        onAskAiWithImage = { bitmap ->
            showTilcayoLens = false
            sendAiMessage("Что изображено на этом фото?", bitmap)
            showSmartTilcayo = true
        },
        onDismiss = { showTilcayoLens = false }
    )

    // THEME & WALLPAPER CUSTOMIZER MODAL SHEET
    if (showThemeCustomizer) {
        ThemeCustomizerSheet(
            wallpaperState = wallpaperState,
            wallpaperPlugins = installedPlugins.filter { it.isWallpaperPlugin() },
            onSelectPreset = { themeRepo.selectPreset(it) },
            onSelectAnimatedPreset = { themeRepo.selectAnimatedPreset(it) },
            onSelectWallpaperPlugin = { themeRepo.setWallpaperPlugin(it) },
            onSetCustomWallpaper = { themeRepo.setCustomWallpaper(it) },
            onSetThemeMode = { themeRepo.setThemeMode(it) },
            onOpenPluginCreator = {
                showThemeCustomizer = false
                showPluginStore = true
            },
            onResetDefault = { themeRepo.resetToDefault() },
            onDismiss = { showThemeCustomizer = false }
        )
    }

    // ABOUT & OPEN SOURCE LICENSE SHEET
    if (showAboutLicense) {
        AboutLicenseSheet(onDismiss = { showAboutLicense = false })
    }

    // SMART TILCAYO FULL-SCREEN AI DIALOG (Smooth Material 3 animated transition with blur)
    SmartTilcayoDialog(
        visible = showSmartTilcayo,
        messages = chatMessages,
        currentModel = selectedModel,
        speakingMessageId = speakingMessageId,
        onModelSelect = { selectedModel = it },
        onSendMessage = { text, bitmap, search -> sendAiMessage(text, bitmap, search) },
        onToggleSpeech = { messageId, rawText -> ttsManager.speak(messageId, rawText) },
        onClearChat = {
            ttsManager.stop()
            chatMessages = emptyList()
        },
        onDismiss = {
            ttsManager.stop()
            showSmartTilcayo = false
        },
        initialPrompt = initialAiPrompt
    )
}


@Composable
fun BrowserBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    pluginCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenPlugins: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back
            IconButton(
                onClick = onBack,
                enabled = canGoBack,
                modifier = Modifier.testTag("nav_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = if (canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Forward
            IconButton(
                onClick = onForward,
                enabled = canGoForward,
                modifier = Modifier.testTag("nav_forward_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Вперед",
                    tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            // Home
            IconButton(
                onClick = onHome,
                modifier = Modifier.testTag("nav_home_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Домой",
                    tint = TilcayoGold
                )
            }

            // Plugins with active badge
            IconButton(
                onClick = onOpenPlugins,
                modifier = Modifier.testTag("nav_plugins_btn")
            ) {
                BadgedBox(
                    badge = {
                        if (pluginCount > 0) {
                            Badge(
                                containerColor = TilcayoGold,
                                contentColor = Color.White
                            ) {
                                Text("$pluginCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = "Плагины",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Tabs with badge
            IconButton(
                onClick = onOpenTabs,
                modifier = Modifier.testTag("nav_tabs_btn")
            ) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text("$tabCount")
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Tab,
                        contentDescription = "Вкладки",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
