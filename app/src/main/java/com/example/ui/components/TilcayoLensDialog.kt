package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SearchEngine
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurfaceCard
import com.example.ui.theme.TilcayoAiBlue
import com.example.ui.theme.TilcayoGold
import com.example.ui.theme.TilcayoGoldLight

@Composable
fun TilcayoLensDialog(
    visible: Boolean,
    currentEngine: SearchEngine,
    onEngineChange: (SearchEngine) -> Unit,
    onSearchVisual: (SearchEngine, Bitmap?) -> Unit,
    onAskAiWithImage: (Bitmap) -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        BackHandler { onDismiss() }
    }

    val context = LocalContext.current
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            previewBitmap = bitmap
            Toast.makeText(
                context,
                "Снимок сделан! Поиск в ${currentEngine.displayName}...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Gallery photo picker launcher (zero-permission Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
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
                previewBitmap = bitmap
                Toast.makeText(
                    context,
                    "Фото выбрано! Поиск в ${currentEngine.displayName}...",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Не удалось загрузить фото", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Scanning animation for viewfinder
    val infiniteTransition = rememberInfiniteTransition(label = "lens_scan")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_offset"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.94f),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.96f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE608090D))
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("tilcayo_lens_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TilcayoGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TilcayoGoldLight),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CenterFocusStrong,
                                    contentDescription = null,
                                    tint = TilcayoGoldLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tilcayo объектив",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Умный визуальный поиск",
                                fontSize = 12.sp,
                                color = Color(0xFFBAC2DE)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Engine Switcher Pill (Google vs Yandex)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0x33282C37),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SearchEngine.entries.forEach { engine ->
                            val isSelected = engine == currentEngine
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) TilcayoGold else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onEngineChange(engine) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = engine.iconRes),
                                        contentDescription = engine.displayName,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = engine.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFFCAD0E0)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Main Lens Viewfinder / Camera Screen HUD
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF13151D))
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(TilcayoGold, Color(0x33FFFFFF), TilcayoAiBlue)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "Снимок объектива",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Animated Scanning Line over the image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .align(Alignment.TopCenter)
                                .padding(top = (scanOffset * 320).dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            TilcayoGold,
                                            Color.White,
                                            TilcayoGold,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    } else {
                        // Viewfinder Reticle Guide
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0x26FFFFFF),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = TilcayoGoldLight,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Наведите камеру или выберите фото",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Tilcayo объектив мгновенно найдёт совпадения в ${currentEngine.displayName}",
                                fontSize = 13.sp,
                                color = Color(0xFFA0A8C0),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Scanner Corner Accents (HUD style)
                    Text(
                        text = "TILCAYO LENS HUD // ACTIVE",
                        fontSize = 10.sp,
                        color = Color(0x66FFFFFF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action Triggers
                if (previewBitmap != null) {
                    // Actions for photo taken
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                onSearchVisual(currentEngine, previewBitmap)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TilcayoGold),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("lens_submit_search_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = currentEngine.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Искать в ${currentEngine.displayName}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    previewBitmap?.let { onAskAiWithImage(it) }
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TilcayoAiBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Спросить ИИ", color = Color.White, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { previewBitmap = null },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Text("Переснять", color = Color(0xFFBAC2DE), fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    // Primary capture buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = TilcayoGold),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = TilcayoGold)
                                .testTag("lens_take_photo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Снять фото",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33282C37)),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                                .testTag("lens_pick_gallery_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = TilcayoGoldLight,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Галерея",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
