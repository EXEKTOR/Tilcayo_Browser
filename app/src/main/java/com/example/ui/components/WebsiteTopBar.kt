package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SearchEngine
import com.example.ui.theme.TilcayoGold

@Composable
fun WebsiteTopBar(
    url: String,
    title: String,
    isLoading: Boolean,
    progress: Int,
    isBookmarked: Boolean,
    isDesktopMode: Boolean,
    searchEngine: SearchEngine,
    onToggleBookmark: () -> Unit,
    onToggleDesktop: () -> Unit,
    onReload: () -> Unit,
    onStopLoading: () -> Unit,
    onHome: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val isHome = url == "tilcayo://home" || url.isBlank()
    var isEditing by remember { mutableStateOf(false) }
    var editTextFieldValue by remember {
        mutableStateOf(TextFieldValue(if (isHome) "" else url, TextRange(if (isHome) 0 else url.length)))
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(url) {
        if (!isEditing) {
            editTextFieldValue = TextFieldValue(if (isHome) "" else url, TextRange(if (isHome) 0 else url.length))
        }
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            editTextFieldValue = TextFieldValue(if (isHome) "" else url, TextRange(0, if (isHome) 0 else url.length))
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    // Pulsing animation for "Загрузка..." badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_loading")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            .testTag("website_top_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditing) {
                    // In-Place URL Editing Mode
                    IconButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Отмена",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, TilcayoGold),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp)
                        ) {
                            BasicTextField(
                                value = editTextFieldValue,
                                onValueChange = { editTextFieldValue = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(TilcayoGold),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction = ImeAction.Go
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        val target = editTextFieldValue.text.trim()
                                        if (target.isNotBlank()) {
                                            isEditing = false
                                            onNavigate(target)
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester)
                                    .testTag("url_inline_input")
                            )

                            if (editTextFieldValue.text.isNotEmpty()) {
                                IconButton(
                                    onClick = { editTextFieldValue = TextFieldValue("") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Очистить",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            val target = editTextFieldValue.text.trim()
                            if (target.isNotBlank()) {
                                isEditing = false
                                onNavigate(target)
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TilcayoGold)
                            .testTag("url_go_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Перейти",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    // Standard Mode: Universal Top Bar
                    IconButton(
                        onClick = onHome,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("topbar_home_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Домой",
                            tint = if (isHome) TilcayoGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Tappable Liquid Glass URL / Search Pill
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { isEditing = true }
                            .testTag("topbar_url_pill"),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isHome) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = searchEngine.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Поиск или веб-адрес...",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Безопасное соединение",
                                    tint = if (url.startsWith("https://")) Color(0xFF10B981) else Color(0xFF9E9E9E),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title.ifBlank {
                                        url.removePrefix("https://").removePrefix("http://")
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Loading state indicator in top bar: Status + percentage badge
                            if (isLoading) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = TilcayoGold.copy(alpha = 0.18f * pulseAlpha + 0.1f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TilcayoGold.copy(alpha = 0.4f)),
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Загрузка",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TilcayoGold
                                        )
                                        if (progress in 1..99) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "$progress%",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TilcayoGold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    if (!isHome) {
                        // Desktop mode toggle
                        IconButton(
                            onClick = onToggleDesktop,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isDesktopMode) Icons.Default.DesktopWindows else Icons.Default.PhoneAndroid,
                                contentDescription = "Режим ПК",
                                tint = if (isDesktopMode) TilcayoGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Bookmark toggle
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "В закладки",
                                tint = if (isBookmarked) TilcayoGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Reload or Stop button based on isLoading
                    IconButton(
                        onClick = {
                            if (isLoading) onStopLoading() else onReload()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("topbar_reload_stop_btn")
                    ) {
                        Icon(
                            imageVector = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                            contentDescription = if (isLoading) "Остановить загрузку" else "Обновить страницу",
                            tint = if (isLoading) TilcayoGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Visible animated liquid gradient progress bar
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val effectiveProgress = if (progress <= 0) 0.15f else (progress / 100f).coerceIn(0.05f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = effectiveProgress,
                    animationSpec = tween(durationMillis = 200),
                    label = "page_load_progress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFE58A2B),
                                        Color(0xFFFBBF24),
                                        Color(0xFF00F5D4)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}
