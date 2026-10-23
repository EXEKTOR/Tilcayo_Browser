package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SearchEngine
import com.example.ui.theme.TilcayoGold
import com.example.ui.theme.TilcayoGoldLight

@Composable
fun SearchPillBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    currentEngine: SearchEngine,
    onEngineChange: (SearchEngine) -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    recentQueries: List<String> = emptyList(),
    onDeleteRecentQuery: (String) -> Unit = {},
    onClearAllHistory: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var isInputFocused by remember { mutableStateOf(false) }
    var engineMenuExpanded by remember { mutableStateOf(false) }

    val capsuleBg = if (isDarkTheme) Color(0xE61E212B) else Color(0xF7FFFFFF)
    val capsuleBorderBrush = if (isDarkTheme) {
        Brush.verticalGradient(listOf(Color(0x40FFFFFF), Color(0x18FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0x66CBD5E1)))
    }
    val textColor = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val hintColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val iconTint = if (isDarkTheme) Color(0xFFA0AAB8) else Color(0xFF64748B)
    val dropdownBg = if (isDarkTheme) Color(0xFF1E212B) else Color(0xFFFFFFFF)
    val dropdownBorder = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x24000000)
    val historyBg = if (isDarkTheme) Color(0xE61E212B) else Color(0xF7FFFFFF)

    Column(modifier = modifier.fillMaxWidth()) {
        // Floating Search Capsule with clean layout and full visibility
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(
                    elevation = if (isDarkTheme) 12.dp else 8.dp,
                    shape = RoundedCornerShape(29.dp),
                    spotColor = if (isDarkTheme) Color(0x66000000) else Color(0x26000000),
                    ambientColor = Color(0x14000000)
                )
                .border(
                    width = 1.2.dp,
                    brush = capsuleBorderBrush,
                    shape = RoundedCornerShape(29.dp)
                ),
            shape = RoundedCornerShape(29.dp),
            color = capsuleBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Engine Switcher Icon (clean circular badge ensuring logo visibility)
                Box {
                    Surface(
                        onClick = { engineMenuExpanded = true },
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1F000000)),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("search_engine_toggle_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = currentEngine.iconRes),
                                contentDescription = currentEngine.displayName,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    // Mini Plate for changing search engine (Google / Yandex) + Theme toggle
                    DropdownMenu(
                        expanded = engineMenuExpanded,
                        onDismissRequest = { engineMenuExpanded = false },
                        modifier = Modifier
                            .background(dropdownBg)
                            .border(1.dp, dropdownBorder, RoundedCornerShape(14.dp))
                    ) {
                        Text(
                            text = "Поисковая система",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color(0xFF9AA0A6) else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )

                        SearchEngine.entries.forEach { engine ->
                            val isSelected = engine == currentEngine
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White,
                                            shadowElevation = 1.dp,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = engine.iconRes),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .padding(2.dp)
                                                    .fillMaxSize()
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = engine.displayName,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) TilcayoGold else if (isDarkTheme) Color.White else Color(0xFF0F172A)
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = TilcayoGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onEngineChange(engine)
                                    engineMenuExpanded = false
                                }
                            )
                        }

                        HorizontalDivider(
                            color = if (isDarkTheme) Color(0x22FFFFFF) else Color(0x14000000),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Quick Theme Switcher item in dropdown
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = TilcayoGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isDarkTheme) "Светлая тема" else "Тёмная тема",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF0F172A)
                                    )
                                }
                            },
                            onClick = {
                                onToggleTheme()
                                engineMenuExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Spacious Text Input Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = currentEngine.hintText,
                            fontSize = 14.sp,
                            color = hintColor,
                            maxLines = 1
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = textColor,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(TilcayoGold),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                isInputFocused = false
                                focusManager.clearFocus()
                                onSearch(query)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isInputFocused = it.isFocused }
                            .testTag("search_input_field")
                    )
                }

                // Clear button (X) when typing
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Очистить",
                            tint = iconTint,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Voice search microphone
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("voice_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Голосовой поиск",
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Primary "Найти" Button
                Surface(
                    onClick = {
                        isInputFocused = false
                        focusManager.clearFocus()
                        onSearch(query)
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = TilcayoGold,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("search_submit_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Найти",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Найти",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Search History Dropdown
        AnimatedVisibility(
            visible = isInputFocused && recentQueries.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0x33000000))
                    .border(
                        width = 1.dp,
                        brush = capsuleBorderBrush,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = historyBg
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = TilcayoGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "История поиска",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme) Color(0xFFA0AAB8) else Color(0xFF64748B)
                            )
                        }

                        Text(
                            text = "Очистить всё",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TilcayoGold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onClearAllHistory() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    HorizontalDivider(
                        color = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x14000000),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    recentQueries.take(6).forEach { historyQuery ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isInputFocused = false
                                    focusManager.clearFocus()
                                    onSearch(historyQuery)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = historyQuery,
                                fontSize = 14.sp,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )

                            // Paste query into search field
                            IconButton(
                                onClick = { onQueryChange(historyQuery) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NorthWest,
                                    contentDescription = "Вставить в строку",
                                    tint = iconTint,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Delete this item
                            IconButton(
                                onClick = { onDeleteRecentQuery(historyQuery) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Удалить",
                                    tint = iconTint,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
