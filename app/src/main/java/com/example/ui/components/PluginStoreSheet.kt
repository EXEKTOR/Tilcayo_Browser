package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Plugin
import com.example.model.PluginType
import com.example.ui.theme.TilcayoAiBlue
import com.example.ui.theme.TilcayoGold
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginStoreSheet(
    installedPlugins: List<Plugin>,
    storeCatalog: List<Plugin>,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (Plugin) -> Unit,
    onUninstallPlugin: (String) -> Unit,
    onSaveCustomPlugin: (Plugin) -> Unit,
    onPublishPlugin: suspend (Plugin) -> Result<Unit>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Магазин", "Мои (${installedPlugins.size})", "Создать", "Документация")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeTemplate by remember { mutableStateOf<Plugin?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("plugin_store_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = TilcayoGold.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = TilcayoGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Система плагинов Тилкайо",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Скрипты для сайтов, живые обои и документация API",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TilcayoGold
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> StoreCatalogView(
                        storeCatalog = storeCatalog,
                        installedPlugins = installedPlugins,
                        onInstall = onInstallPlugin
                    )
                    1 -> InstalledPluginsView(
                        installedPlugins = installedPlugins,
                        onToggle = onTogglePlugin,
                        onUninstall = onUninstallPlugin
                    )
                    2 -> CreatePluginView(
                        templatePlugin = activeTemplate,
                        onSaveLocal = { plugin ->
                            onSaveCustomPlugin(plugin)
                            Toast.makeText(context, "Плагин «${plugin.name}» сохранен!", Toast.LENGTH_SHORT).show()
                            selectedTab = 1
                        },
                        onPublishCloud = { plugin ->
                            scope.launch {
                                val result = onPublishPlugin(plugin)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Плагин «${plugin.name}» выгружен в Магазин!", Toast.LENGTH_LONG).show()
                                    selectedTab = 0
                                } else {
                                    Toast.makeText(context, "Ошибка: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                    3 -> PluginDocsView(
                        onLoadTemplate = { template ->
                            activeTemplate = template
                            selectedTab = 2
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreCatalogView(
    storeCatalog: List<Plugin>,
    installedPlugins: List<Plugin>,
    onInstall: (Plugin) -> Unit
) {
    if (storeCatalog.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Каталог плагинов загружается...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(storeCatalog, key = { it.id }) { plugin ->
                val isInstalled = installedPlugins.any { it.id == plugin.id }
                StorePluginCard(
                    plugin = plugin,
                    isInstalled = isInstalled,
                    onInstall = { onInstall(plugin) }
                )
            }
        }
    }
}

@Composable
private fun StorePluginCard(
    plugin: Plugin,
    isInstalled: Boolean,
    onInstall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = getCategoryColor(plugin.category).copy(alpha = 0.15f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getPluginIcon(plugin.iconName),
                        contentDescription = null,
                        tint = getCategoryColor(plugin.category),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plugin.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = getCategoryColor(plugin.category).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (plugin.isWallpaperPlugin()) "Обои" else plugin.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = getCategoryColor(plugin.category),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = plugin.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Автор: ${plugin.author} · v${plugin.version}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isInstalled) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TilcayoGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Установлен",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TilcayoGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else {
                Button(
                    onClick = onInstall,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TilcayoGold),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Установить", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun InstalledPluginsView(
    installedPlugins: List<Plugin>,
    onToggle: (String, Boolean) -> Unit,
    onUninstall: (String) -> Unit
) {
    if (installedPlugins.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "У вас пока нет установленных плагинов",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(installedPlugins, key = { it.id }) { plugin ->
                InstalledPluginCard(
                    plugin = plugin,
                    onToggle = { isEnabled -> onToggle(plugin.id, isEnabled) },
                    onUninstall = { onUninstall(plugin.id) }
                )
            }
        }
    }
}

@Composable
private fun InstalledPluginCard(
    plugin: Plugin,
    onToggle: (Boolean) -> Unit,
    onUninstall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = getCategoryColor(plugin.category).copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getPluginIcon(plugin.iconName),
                            contentDescription = null,
                            tint = getCategoryColor(plugin.category),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plugin.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (plugin.isWallpaperPlugin()) "Живые обои" else "URL: ${plugin.matchUrlRegex}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = plugin.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TilcayoGold
                    )
                )

                if (!plugin.isPreinstalled) {
                    IconButton(onClick = onUninstall) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить плагин",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = plugin.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CreatePluginView(
    templatePlugin: Plugin?,
    onSaveLocal: (Plugin) -> Unit,
    onPublishCloud: (Plugin) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf(PluginType.SCRIPT) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Утилиты") }
    var matchUrlRegex by remember { mutableStateOf(".*") }
    var jsCode by remember {
        mutableStateOf(
            """
                // Пример пользовательского скрипта:
                console.log("[Тилкайо Плагин] Страница загружена!");
            """.trimIndent()
        )
    }
    var cssCode by remember {
        mutableStateOf(
            """
                /* Кастомные CSS стили для сайтов */
                a { text-decoration: underline !important; }
            """.trimIndent()
        )
    }
    var htmlCode by remember {
        mutableStateOf(
            """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<style>
  html, body { margin:0; padding:0; width:100%; height:100%; overflow:hidden; background:transparent; }
  canvas { display:block; width:100%; height:100%; }
</style>
</head>
<body>
<canvas id="c"></canvas>
<script>
  const canvas = document.getElementById('c');
  const ctx = canvas.getContext('2d');
  let w, h, t = 0;
  function resize() { w = canvas.width = window.innerWidth; h = canvas.height = window.innerHeight; }
  window.addEventListener('resize', resize);
  resize();
  function loop() {
    ctx.clearRect(0, 0, w, h);
    t += 0.02;
    ctx.fillStyle = 'rgba(229,138,43,0.7)';
    ctx.beginPath();
    ctx.arc(w/2 + Math.sin(t)*80, h/2 + Math.cos(t)*80, 25, 0, Math.PI*2);
    ctx.fill();
    requestAnimationFrame(loop);
  }
  loop();
</script>
</body>
</html>
            """.trimIndent()
        )
    }

    var showLivePreview by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    // Update if template provided
    LaunchedEffect(templatePlugin) {
        templatePlugin?.let {
            name = it.name
            description = it.description
            category = it.category
            matchUrlRegex = it.matchUrlRegex
            jsCode = it.jsCode
            cssCode = it.cssCode
            htmlCode = it.htmlCode
            selectedType = when (it.pluginType) {
                PluginType.WALLPAPER.name -> PluginType.WALLPAPER
                PluginType.STYLESHEET.name -> PluginType.STYLESHEET
                else -> PluginType.SCRIPT
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Конструктор плагинов",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Создавайте скрипты для сайтов или живые обои HTML5 Canvas",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = { showImportDialog = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Импорт JSON", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Plugin Type Selector
        Text(
            text = "Тип плагина:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PluginType.entries.forEach { type ->
                val isSelected = selectedType == type
                Surface(
                    onClick = {
                        selectedType = type
                        if (type == PluginType.WALLPAPER) {
                            category = "Живые обои"
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) TilcayoGold else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = type.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Название плагина") },
            placeholder = { Text("Например: Неоновые искры или Умный шрифт") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Краткое описание") },
            placeholder = { Text("Что делает этот плагин...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedType == PluginType.WALLPAPER) {
            // Live Wallpaper HTML5 Canvas Code Editor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HTML5 Canvas / WebGL код живых обоев:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { showLivePreview = !showLivePreview }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TilcayoGold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showLivePreview) "Скрыть тест" else "Тест обоев", fontSize = 12.sp, color = TilcayoGold)
                }
            }

            if (showLivePreview) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, TilcayoGold, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    PluginWallpaperRunner(
                        htmlCode = htmlCode,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = htmlCode,
                onValueChange = { htmlCode = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )
        } else {
            // Web Script / CSS Editor
            OutlinedTextField(
                value = matchUrlRegex,
                onValueChange = { matchUrlRegex = it },
                label = { Text("URL Regex фильтр сайтов") },
                placeholder = { Text(".* для всех сайтов или ^https?://.*wikipedia.*") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CSS стили (инжектируются в head сайта):",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = cssCode,
                onValueChange = { cssCode = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "JavaScript код страницы:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = jsCode,
                onValueChange = { jsCode = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Введите название плагина", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val plugin = Plugin(
                        id = "user_plugin_" + UUID.randomUUID().toString().take(8),
                        name = name,
                        description = description.ifBlank { "Пользовательский плагин" },
                        author = "Вы",
                        version = "1.0.0",
                        iconName = if (selectedType == PluginType.WALLPAPER) "auto_awesome" else "code",
                        category = if (selectedType == PluginType.WALLPAPER) "Живые обои" else category,
                        pluginType = selectedType.name,
                        jsCode = jsCode,
                        cssCode = cssCode,
                        htmlCode = htmlCode,
                        matchUrlRegex = matchUrlRegex,
                        isEnabled = true,
                        isPreinstalled = false
                    )
                    onSaveLocal(plugin)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TilcayoGold),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Сохранить локально")
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Введите название плагина", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val plugin = Plugin(
                        id = "store_plugin_" + UUID.randomUUID().toString().take(8),
                        name = name,
                        description = description.ifBlank { "Пользовательский плагин" },
                        author = "Пользователь",
                        version = "1.0.0",
                        iconName = if (selectedType == PluginType.WALLPAPER) "auto_awesome" else "code",
                        category = if (selectedType == PluginType.WALLPAPER) "Живые обои" else category,
                        pluginType = selectedType.name,
                        jsCode = jsCode,
                        cssCode = cssCode,
                        htmlCode = htmlCode,
                        matchUrlRegex = matchUrlRegex,
                        isEnabled = true,
                        isPreinstalled = false,
                        downloads = 1L
                    )
                    onPublishCloud(plugin)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TilcayoAiBlue),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("В Магазин")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Импорт манифеста плагина") },
            text = {
                Column {
                    Text("Вставьте JSON манифест плагина Тилкайо:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        placeholder = { Text("{\"id\": \"...\", \"name\": \"...\"}") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = Plugin.fromJsonString(importJsonText)
                        if (parsed != null) {
                            name = parsed.name
                            description = parsed.description
                            category = parsed.category
                            matchUrlRegex = parsed.matchUrlRegex
                            jsCode = parsed.jsCode
                            cssCode = parsed.cssCode
                            htmlCode = parsed.htmlCode
                            selectedType = when (parsed.pluginType) {
                                PluginType.WALLPAPER.name -> PluginType.WALLPAPER
                                PluginType.STYLESHEET.name -> PluginType.STYLESHEET
                                else -> PluginType.SCRIPT
                            }
                            showImportDialog = false
                            Toast.makeText(context, "Манифест успешно загружен!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Некорректный JSON манифест", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TilcayoGold)
                ) {
                    Text("Загрузить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

/**
 * In-App Developer Documentation for the Tilcayo Plugin Engine
 */
@Composable
private fun PluginDocsView(
    onLoadTemplate: (Plugin) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Section 1: Overview & Architecture
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = TilcayoGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Архитектура и идентификация плагинов",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Тилкайо определяет каждый плагин через уникальный идентификатор и манифест спецификации v2. " +
                            "Каждый плагин декларирует категорию, тип назначения (SCRIPT, STYLESHEET или WALLPAPER) и правила URL-фильтрации.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Plugin Types
        Text(
            text = "Поддерживаемые типы плагинов:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        DocFeatureCard(
            badge = "SCRIPT",
            badgeColor = TilcayoAiBlue,
            title = "Веб-скрипты JavaScript",
            description = "Выполняются в контексте открытых страниц после события DOMContentLoaded. Имеют полный доступ к DOM дереву, fetch API и мосту window.TilcayoBridge."
        )

        Spacer(modifier = Modifier.height(6.dp))

        DocFeatureCard(
            badge = "STYLESHEET",
            badgeColor = Color(0xFFAB47BC),
            title = "CSS Стили",
            description = "Инжектируются в head страницы под изолированным идентификатором #tilcayo-plugin-<id>. Позволяют менять темную тему, шрифты и скрывать элементы."
        )

        Spacer(modifier = Modifier.height(6.dp))

        DocFeatureCard(
            badge = "WALLPAPER",
            badgeColor = TilcayoGold,
            title = "Живые обои HTML5 Canvas",
            description = "Выполняются как интерактивный фон домашнего экрана Тилкайо на 60 FPS через аппаратное ускорение WebView Sandbox с прозрачной подложкой."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Section 3: Injected APIs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "JavaScript API моста (window.TilcayoBridge)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "На каждой веб-странице доступен системный интерфейс:\n\n" +
                            "• window.TilcayoBridge.requestPageSummary(text) — открывает ИИ диалог Тилкайо и передает текст страницы для мгновенного анализа.\n" +
                            "• document.getElementById(...) — стандартный доступ к DOM.\n" +
                            "• console.log('[Тилкайо]', ...) — логгирование в Logcat браузера.",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 4: Manifest JSON Specification
        val sampleManifest = """
{
  "id": "my_fireflies_wallpaper",
  "name": "Ночные светлячки",
  "version": "1.0.0",
  "author": "Разработчик",
  "pluginType": "WALLPAPER",
  "category": "Живые обои",
  "matchUrlRegex": ".*",
  "htmlCode": "<!DOCTYPE html><html>...<canvas id='c'></canvas>...</html>",
  "manifestVersion": 2
}
        """.trimIndent()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Спецификация Манифеста JSON",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(sampleManifest))
                            Toast.makeText(context, "Спецификация скопирована в буфер!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Копировать", modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = sampleManifest,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TilcayoGold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 5: Interactive Templates
        Text(
            text = "Готовые шаблоны (нажмите для загрузки в редактор):",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Template 1: Fireflies Wallpaper
        TemplateButton(
            title = "✨ Шаблон: Живые обои «Светлячки на Canvas»",
            subtitle = "Интерактивный холст с пульсирующими огоньками",
            onClick = {
                onLoadTemplate(
                    Plugin(
                        id = "user_wp_fireflies",
                        name = "Светлячки (Мои обои)",
                        description = "Интерактивный холст с пульсирующими светлячками HTML5 Canvas",
                        category = "Живые обои",
                        pluginType = PluginType.WALLPAPER.name,
                        htmlCode = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<style>
  html, body { margin:0; padding:0; width:100%; height:100%; overflow:hidden; background:transparent; }
  canvas { display:block; width:100%; height:100%; }
</style>
</head>
<body>
<canvas id="c"></canvas>
<script>
  const canvas = document.getElementById('c');
  const ctx = canvas.getContext('2d');
  let w, h;
  function resize() { w = canvas.width = window.innerWidth; h = canvas.height = window.innerHeight; }
  window.addEventListener('resize', resize);
  resize();
  const bugs = [];
  for(let i=0; i<40; i++) {
    bugs.push({x:Math.random()*w, y:Math.random()*h, vx:(Math.random()-0.5)*0.8, vy:(Math.random()-0.5)*0.8, r:Math.random()*3+2, p:Math.random()*6});
  }
  function loop() {
    ctx.clearRect(0,0,w,h);
    bugs.forEach(b => {
      b.x+=b.vx; b.y+=b.vy; b.p+=0.04;
      if(b.x<0) b.x=w; if(b.x>w) b.x=0; if(b.y<0) b.y=h; if(b.y>h) b.y=0;
      const a = (Math.sin(b.p)+1)/2 * 0.7 + 0.3;
      const g = ctx.createRadialGradient(b.x, b.y, 0, b.x, b.y, b.r*3);
      g.addColorStop(0, 'rgba(229,138,43,' + a + ')');
      g.addColorStop(1, 'rgba(229,138,43,0)');
      ctx.fillStyle = g;
      ctx.beginPath(); ctx.arc(b.x, b.y, b.r*3, 0, Math.PI*2); ctx.fill();
    });
    requestAnimationFrame(loop);
  }
  loop();
</script>
</body>
</html>
                        """.trimIndent()
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Template 2: Custom Reading Mode
        TemplateButton(
            title = "📖 Шаблон: Скрипт «Инспектор чтения»",
            subtitle = "Добавляет бейдж со статистикой слов на страницах",
            onClick = {
                onLoadTemplate(
                    Plugin(
                        id = "user_script_reader",
                        name = "Мой инспектор чтения",
                        description = "Показывает время чтения вверху любой статьи",
                        category = "Утилиты",
                        pluginType = PluginType.SCRIPT.name,
                        matchUrlRegex = "^https?://.*",
                        jsCode = """
(function() {
  const words = (document.body.innerText || '').trim().split(/\\s+/).length;
  const time = Math.max(1, Math.round(words / 190));
  const el = document.createElement('div');
  el.style = 'position:fixed;top:12px;left:12px;background:#1E2026;color:#E58A2B;padding:6px 12px;border-radius:20px;font-size:12px;z-index:999999;box-shadow:0 4px 10px rgba(0,0,0,0.4);';
  el.innerText = '⏱️ Время чтения: ~' + time + ' мин';
  document.body.appendChild(el);
})();
                        """.trimIndent()
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DocFeatureCard(
    badge: String,
    badgeColor: Color,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TemplateButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, TilcayoGold.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = TilcayoGold,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun getPluginIcon(iconName: String): ImageVector {
    return when (iconName) {
        "dark_mode" -> Icons.Default.Palette
        "security" -> Icons.Default.Security
        "auto_awesome" -> Icons.Default.AutoAwesome
        "timer" -> Icons.Default.Timer
        "code" -> Icons.Default.Code
        else -> Icons.Default.Extension
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category) {
        "Внешний вид" -> Color(0xFFAB47BC)
        "Безопасность" -> Color(0xFF2E7D32)
        "ИИ" -> TilcayoAiBlue
        "Живые обои" -> Color(0xFF00B4D8)
        "Утилиты" -> TilcayoGold
        else -> TilcayoGold
    }
}
