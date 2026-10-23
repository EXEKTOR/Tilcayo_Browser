package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ChatMessage
import com.example.model.TilcayoModel
import com.example.ui.theme.TilcayoAiBlue
import com.example.ui.theme.TilcayoGold
import kotlinx.coroutines.delay

@Composable
fun SmartTilcayoDialog(
    visible: Boolean,
    messages: List<ChatMessage>,
    currentModel: TilcayoModel,
    speakingMessageId: String? = null,
    onModelSelect: (TilcayoModel) -> Unit,
    onSendMessage: (String, Bitmap?, Boolean) -> Unit,
    onToggleSpeech: (String, String) -> Unit = { _, _ -> },
    onClearChat: () -> Unit,
    onDismiss: () -> Unit,
    initialPrompt: String = ""
) {
    if (visible) {
        BackHandler { onDismiss() }
    }

    var inputText by remember { mutableStateOf(initialPrompt) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var modelMenuExpanded by remember { mutableStateOf(false) }
    var useGoogleSearch by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                selectedBitmap = bitmap
            } catch (_: Exception) { }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickSuggestions = listOf(
        "Расскажи про породу кошек тилкайо 🐾",
        "Перескажи суть открытого сайта 📖",
        "Придумай крутой JS-плагин для браузера 💡",
        "Посоветуй интересные сайты для чтения 🌐"
    )

    // FLUID ANIMATED OVERLAY WITH BLUR & SPRING TRANSITIONS
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(280)),
        exit = fadeOut(animationSpec = tween(220))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC0B0D12)) // Frosted scrim overlay
                .testTag("smart_tilcayo_dialog")
        ) {
            // Main Glass Sheet with spring slide-in and scale
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + scaleIn(
                    initialScale = 0.93f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(240)
                ) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(200)
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding(),
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Header Bar
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Mascot
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .shadow(6.dp, CircleShape, spotColor = TilcayoGold)
                                        .clip(CircleShape)
                                        .border(2.dp, TilcayoGold, CircleShape)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.tilcayo_smart),
                                        contentDescription = "Маскот Умный Тилкайо",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Умный Тилкайо",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = TilcayoGold,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    // Model Picker Chip
                                    Box {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { modelMenuExpanded = true }
                                                .padding(vertical = 2.dp)
                                                .testTag("ai_model_picker")
                                        ) {
                                            Text(
                                                text = currentModel.displayName,
                                                fontSize = 12.sp,
                                                color = TilcayoAiBlue,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Выбрать модель",
                                                tint = TilcayoAiBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = modelMenuExpanded,
                                            onDismissRequest = { modelMenuExpanded = false }
                                        ) {
                                            TilcayoModel.entries.forEach { model ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(
                                                                text = model.displayName,
                                                                fontWeight = if (model == currentModel) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (model == currentModel) TilcayoGold else MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                text = model.description,
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        onModelSelect(model)
                                                        modelMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Reset / Clear chat
                                IconButton(onClick = onClearChat) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Очистить чат",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Close Dialog
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Закрыть",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Chat Messages Stream
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (messages.isEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 28.dp, bottom = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(90.dp)
                                                .shadow(12.dp, CircleShape, spotColor = Color(0x66D9822B))
                                                .clip(CircleShape)
                                                .border(2.dp, TilcayoGold, CircleShape)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.tilcayo_smart),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.size(90.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "Мяу! Я Умный Тилкайо",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Твой пухлый эксперт и браузерный ассистент.\nЗадавай любой вопрос, присылай картинки, включай онлайн поиск или проси озвучить ответы голосом!",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                    }
                                }
                            }

                            items(messages, key = { it.id }) { msg ->
                                ChatBubble(
                                    message = msg,
                                    isSpeaking = speakingMessageId == msg.id,
                                    onToggleSpeech = { onToggleSpeech(msg.id, msg.text) },
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(msg.text))
                                    }
                                )
                            }
                        }

                        // Quick suggestion chips
                        if (messages.size <= 2) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(quickSuggestions) { suggestion ->
                                    Surface(
                                        onClick = {
                                            onSendMessage(suggestion, null, useGoogleSearch)
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, TilcayoGold.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = suggestion,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Attached image preview
                        AnimatedVisibility(visible = selectedBitmap != null) {
                            selectedBitmap?.let { bitmap ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, TilcayoGold, RoundedCornerShape(8.dp))
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Прикрепленное фото",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Фото готово к анализу Тилкайо",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = { selectedBitmap = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Удалить фото", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Web Search Toggle Bar above input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                onClick = { useGoogleSearch = !useGoogleSearch },
                                shape = RoundedCornerShape(20.dp),
                                color = if (useGoogleSearch) TilcayoGold.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (useGoogleSearch) TilcayoGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.testTag("ai_web_search_toggle")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (useGoogleSearch) Icons.Default.TravelExplore else Icons.Default.Public,
                                        contentDescription = null,
                                        tint = if (useGoogleSearch) TilcayoGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (useGoogleSearch) "Поиск в Google: ВКЛ" else "Поиск в Google: ВЫКЛ (база)",
                                        fontSize = 12.sp,
                                        fontWeight = if (useGoogleSearch) FontWeight.Bold else FontWeight.Normal,
                                        color = if (useGoogleSearch) TilcayoGold else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = if (useGoogleSearch) "ИИ ищет свежие данные в сети" else "Ответ из встроенных знаний",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }

                        // Input Bar in Liquid Glass
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Attach photo button
                                IconButton(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("attach_photo_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Прикрепить фото для анализа",
                                        tint = TilcayoGold
                                    )
                                }

                                // Text Field
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = { Text("Спросить Умного Тилкайо...", fontSize = 14.sp) },
                                    maxLines = 4,
                                    shape = RoundedCornerShape(22.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TilcayoGold,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp)
                                        .testTag("ai_chat_input")
                                )

                                // Send Button
                                val canSend = inputText.isNotBlank() || selectedBitmap != null
                                Surface(
                                    onClick = {
                                        if (canSend) {
                                            val text = inputText
                                            val bitmap = selectedBitmap
                                            val searchEnabled = useGoogleSearch
                                            inputText = ""
                                            selectedBitmap = null
                                            onSendMessage(text, bitmap, searchEnabled)
                                        }
                                    },
                                    shape = CircleShape,
                                    color = if (canSend) TilcayoGold else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .testTag("send_ai_message_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Отправить",
                                            tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    onToggleSpeech: () -> Unit,
    onCopy: () -> Unit
) {
    var copiedRecently by remember { mutableStateOf(false) }

    LaunchedEffect(copiedRecently) {
        if (copiedRecently) {
            delay(1800)
            copiedRecently = false
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(1.dp, TilcayoGold, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tilcayo_smart),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(if (message.isUser) 0.82f else 0.90f),
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            if (!message.isUser) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Тилкайо · ${message.modelName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TilcayoGold
                    )
                    if (message.isStreaming) {
                        Spacer(modifier = Modifier.width(6.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(10.dp),
                            strokeWidth = 1.5.dp,
                            color = TilcayoGold
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // TTS Voice Narration Button
                    IconButton(
                        onClick = onToggleSpeech,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("tts_btn_${message.id}")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeaking) "Остановить озвучку" else "Озвучить ответ через Google TTS",
                            tint = if (isSpeaking) TilcayoGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Copy Message Button
                    IconButton(
                        onClick = {
                            onCopy()
                            copiedRecently = true
                        },
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("copy_msg_${message.id}")
                    ) {
                        Icon(
                            imageVector = if (copiedRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Скопировать ответ",
                            tint = if (copiedRecently) TilcayoGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                color = if (message.isUser) TilcayoGold else MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (message.isUser) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    message.imageBitmap?.let { bitmap ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(bottom = 8.dp)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Загруженное изображение",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    SelectionContainer {
                        if (message.isUser) {
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                color = Color.White,
                                lineHeight = 20.sp
                            )
                        } else {
                            if (message.text.isBlank() && message.isStreaming) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = TilcayoGold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Тилкайо размышляет над ответом...",
                                        fontSize = 13.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                RichMarkdownContent(
                                    markdown = message.text,
                                    textColor = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// RICH MARKDOWN COMPOSABLE SYSTEM
// ==========================================

sealed class MarkdownBlock {
    data class Code(val language: String, val code: String) : MarkdownBlock()
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Blockquote(val text: String) : MarkdownBlock()
    data class Bullet(val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = text.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        // Code block starts with ```
        if (trimmed.startsWith("```")) {
            val language = trimmed.removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            if (i < lines.size) i++ // consume closing ```
            blocks.add(
                MarkdownBlock.Code(
                    language = language.ifBlank { "code" },
                    code = codeLines.joinToString("\n")
                )
            )
            continue
        }

        // Heading #, ##, ###
        if (trimmed.startsWith("#")) {
            val hashes = trimmed.takeWhile { it == '#' }
            if (hashes.length in 1..6 && trimmed.length > hashes.length && trimmed[hashes.length] == ' ') {
                val headingText = trimmed.substring(hashes.length).trim()
                blocks.add(MarkdownBlock.Heading(level = hashes.length, text = headingText))
                i++
                continue
            }
        }

        // Blockquote >
        if (trimmed.startsWith(">")) {
            val quoteLines = mutableListOf(trimmed.removePrefix(">").trim())
            i++
            while (i < lines.size && lines[i].trim().startsWith(">")) {
                quoteLines.add(lines[i].trim().removePrefix(">").trim())
                i++
            }
            blocks.add(MarkdownBlock.Blockquote(text = quoteLines.joinToString("\n")))
            continue
        }

        // Bullet list *, -, +
        if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("+ ")) {
            val bulletText = trimmed.substring(2).trim()
            blocks.add(MarkdownBlock.Bullet(text = bulletText))
            i++
            continue
        }

        // Regular paragraph (skip purely blank lines between blocks)
        if (trimmed.isNotEmpty()) {
            blocks.add(MarkdownBlock.Paragraph(text = line))
        }
        i++
    }

    return blocks
}

@Composable
fun RichMarkdownContent(
    markdown: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val fontSize = when (block.level) {
                        1 -> 17.sp
                        2 -> 16.sp
                        else -> 14.5.sp
                    }
                    Text(
                        text = parseInlineMarkdown(block.text, textColor),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = TilcayoGold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                is MarkdownBlock.Code -> {
                    CodeBlockCard(language = block.language, code = block.code)
                }

                is MarkdownBlock.Blockquote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(TilcayoGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(block.text, textColor.copy(alpha = 0.85f)),
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 18.sp
                        )
                    }
                }

                is MarkdownBlock.Bullet -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontSize = 15.sp,
                            color = TilcayoGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp, top = 1.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, textColor),
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text, textColor),
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlockCard(language: String, code: String) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1800)
            copied = false
        }
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF14171E),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282E3E)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1C212B))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = language.uppercase(),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TilcayoGold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(code))
                            copied = true
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Копировать код",
                        tint = if (copied) TilcayoGold else Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (copied) "Скопировано!" else "Копировать",
                        fontSize = 11.sp,
                        color = if (copied) TilcayoGold else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Code Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(10.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFFE2E8F0)
                )
            }
        }
    }
}

fun parseInlineMarkdown(text: String, baseTextColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            // Bold **text**
            if (text.startsWith("**", i)) {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseTextColor)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            }
            // Inline code `code`
            if (text.startsWith("`", i)) {
                val end = text.indexOf("`", i + 1)
                if (end != -1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            background = Color(0x33D9822B),
                            color = TilcayoGold
                        )
                    ) {
                        append(" ${text.substring(i + 1, end)} ")
                    }
                    i = end + 1
                    continue
                }
            }
            // Italic *text*
            if (text.startsWith("*", i) && !text.startsWith("**", i)) {
                val end = text.indexOf("*", i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseTextColor)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            }
            // Normal character
            append(text[i])
            i++
        }
    }
}
