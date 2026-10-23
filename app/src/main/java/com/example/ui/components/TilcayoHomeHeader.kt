package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Bookmark
import com.example.model.SearchEngine
import com.example.ui.theme.TilcayoAiBlue
import com.example.ui.theme.TilcayoGold
import com.example.ui.theme.TilcayoGoldLight

data class QuickDialItem(
    val title: String,
    val url: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun TilcayoHomeHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    currentEngine: SearchEngine,
    onEngineChange: (SearchEngine) -> Unit,
    onOpenAiMode: () -> Unit,
    onOpenLens: () -> Unit,
    onOpenPluginStore: () -> Unit,
    onOpenThemeCustomizer: () -> Unit,
    onOpenLicense: () -> Unit = {},
    onVoiceClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    bookmarks: List<Bookmark>,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    recentQueries: List<String> = emptyList(),
    onDeleteRecentQuery: (String) -> Unit = {},
    onClearAllHistory: () -> Unit = {}
) {
    val quickDials = listOf(
        QuickDialItem("Google", "https://www.google.com", Icons.Default.Language, Color(0xFF4285F4)),
        QuickDialItem("Яндекс", "https://ya.ru", Icons.Default.Language, Color(0xFFFF3333)),
        QuickDialItem("YouTube", "https://www.youtube.com", Icons.Default.PlayArrow, Color(0xFFFF0000)),
        QuickDialItem("Википедия", "https://ru.wikipedia.org", Icons.Default.Public, if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF475569)),
        QuickDialItem("GitHub", "https://github.com", Icons.Default.Code, if (isDarkTheme) Color.White else Color(0xFF24292E)),
        QuickDialItem("Хабр", "https://habr.com", Icons.Default.Language, Color(0xFF65A3BE)),
        QuickDialItem("Telegram", "https://web.telegram.org", Icons.Default.Send, Color(0xFF29B6F6)),
        QuickDialItem("Плагины", "tilcayo://plugins", Icons.Default.Extension, TilcayoGold)
    )

    val textColor = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textMuted = if (isDarkTheme) Color(0xFFA0AAB8) else Color(0xFF64748B)
    val topBtnBg = if (isDarkTheme) Color(0x33282C37) else Color(0xD9FFFFFF)
    val topBtnBorder = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x33CBD5E1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top action bar with License, Quick Theme Switcher and Theme Customizer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // License & About Button
            Surface(
                onClick = onOpenLicense,
                shape = CircleShape,
                color = topBtnBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, topBtnBorder),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("home_license_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Лицензия и о программе",
                        tint = TilcayoGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Quick Theme Switcher (Sun/Moon button)
            Surface(
                onClick = onToggleTheme,
                shape = CircleShape,
                color = topBtnBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, topBtnBorder),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("home_theme_toggle_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Переключить тему (Светлая/Тёмная)",
                        tint = TilcayoGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Theme & Wallpaper Customizer Button
            Surface(
                onClick = onOpenThemeCustomizer,
                shape = CircleShape,
                color = topBtnBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, topBtnBorder),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("theme_customizer_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Кастомизация обоев и темы",
                        tint = TilcayoGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // LIQUID GLASS MASCOT AVATAR
        Box(
            modifier = Modifier
                .size(132.dp)
                .shadow(
                    elevation = if (isDarkTheme) 20.dp else 10.dp,
                    shape = CircleShape,
                    spotColor = Color(0x73D9822B),
                    ambientColor = Color(0x33000000)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isDarkTheme) {
                            listOf(Color(0xFF38312B), Color(0xFF1E1A17))
                        } else {
                            listOf(Color(0xFFFFF7ED), Color(0xFFFED7AA))
                        }
                    )
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            TilcayoGoldLight,
                            Color(0x80FFFFFF),
                            TilcayoGold,
                            Color(0x33D9822B)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.tilcayo_logo),
                contentDescription = "Логотип Тилкайо Браузер",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(126.dp)
                    .clip(CircleShape)
                    .testTag("tilcayo_home_logo")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Browser Title in Material 3
        Text(
            text = "Тилкайо браузер",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = (-0.5).sp
        )

        // Subtle Liquid Glass Mascot Badge
        Surface(
            color = if (isDarkTheme) Color(0x26E58A2B) else Color(0x26D97706),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isDarkTheme) Color(0x40E58A2B) else Color(0x40D97706)
            ),
            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
        ) {
            Text(
                text = "🐾 Пухлый кот с плагинами",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) TilcayoGoldLight else Color(0xFFB45309),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // UNCLUTTERED LIQUID GLASS SEARCH PILL BAR
        SearchPillBar(
            query = searchQuery,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            currentEngine = currentEngine,
            onEngineChange = onEngineChange,
            onVoiceClick = onVoiceClick,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            recentQueries = recentQueries,
            onDeleteRecentQuery = onDeleteRecentQuery,
            onClearAllHistory = onClearAllHistory,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // THE 2 SEPARATE PLATES BELOW SEARCH: "✨ Умный тилкайо" & "📷 Tilcayo объектив"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Plate 1: "✨ Умный тилкайо"
            val aiPlateBg = if (isDarkTheme) Color(0x3328324E) else Color(0xF5FFFFFF)
            val aiPlateBorder = if (isDarkTheme) {
                Brush.horizontalGradient(listOf(Color(0x666C8CFF), Color(0x22FFFFFF)))
            } else {
                Brush.horizontalGradient(listOf(Color(0x4D4C6EF5), Color(0x22CBD5E1)))
            }

            Surface(
                onClick = onOpenAiMode,
                shape = RoundedCornerShape(20.dp),
                color = aiPlateBg,
                border = androidx.compose.foundation.BorderStroke(width = 1.dp, brush = aiPlateBorder),
                shadowElevation = if (isDarkTheme) 4.dp else 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("home_smart_tilcayo_plate")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = TilcayoAiBlue.copy(alpha = 0.2f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isDarkTheme) Color(0xFF8AB4F8) else TilcayoAiBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "Умный тилкайо",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "ИИ режим",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) Color(0xFFA0B0D0) else Color(0xFF4C6EF5)
                        )
                    }
                }
            }

            // Plate 2: "📷 Tilcayo объектив"
            val lensPlateBg = if (isDarkTheme) Color(0x33382C22) else Color(0xF5FFFFFF)
            val lensPlateBorder = if (isDarkTheme) {
                Brush.horizontalGradient(listOf(Color(0x66E58A2B), Color(0x22FFFFFF)))
            } else {
                Brush.horizontalGradient(listOf(Color(0x4DE58A2B), Color(0x22CBD5E1)))
            }

            Surface(
                onClick = onOpenLens,
                shape = RoundedCornerShape(20.dp),
                color = lensPlateBg,
                border = androidx.compose.foundation.BorderStroke(width = 1.dp, brush = lensPlateBorder),
                shadowElevation = if (isDarkTheme) 4.dp else 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("home_tilcayo_lens_plate")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = TilcayoGold.copy(alpha = 0.2f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (isDarkTheme) TilcayoGoldLight else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "Tilcayo объектив",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Поиск по фото",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) TilcayoGoldLight else Color(0xFFB45309)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // QUICK DIALS SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Быстрый доступ",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )

            Surface(
                onClick = onOpenPluginStore,
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkTheme) Color(0x26E58A2B) else Color(0x20E58A2B),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDarkTheme) Color(0x33E58A2B) else Color(0x40E58A2B)
                )
            ) {
                Text(
                    text = "Магазин плагинов ›",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) TilcayoGoldLight else Color(0xFFB45309),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4x2 Grid of Liquid Glass Quick Dial Tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            quickDials.take(4).forEach { item ->
                LiquidGlassDial(
                    item = item,
                    isDarkTheme = isDarkTheme,
                    onClick = {
                        if (item.url == "tilcayo://plugins") {
                            onOpenPluginStore()
                        } else {
                            onOpenUrl(item.url)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            quickDials.drop(4).take(4).forEach { item ->
                LiquidGlassDial(
                    item = item,
                    isDarkTheme = isDarkTheme,
                    onClick = {
                        if (item.url == "tilcayo://plugins") {
                            onOpenPluginStore()
                        } else {
                            onOpenUrl(item.url)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun LiquidGlassDial(
    item: QuickDialItem,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val dialBg = if (isDarkTheme) Color(0x99242934) else Color(0xF5FFFFFF)
    val dialBorder = if (isDarkTheme) {
        Brush.verticalGradient(listOf(Color(0x40FFFFFF), Color(0x10FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0x66CBD5E1)))
    }
    val dialTextColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF0F172A)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
            .width(68.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = if (isDarkTheme) 6.dp else 3.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = if (isDarkTheme) Color(0x40000000) else Color(0x1A000000)
                )
                .border(
                    width = 1.dp,
                    brush = dialBorder,
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            color = dialBg
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = dialTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
