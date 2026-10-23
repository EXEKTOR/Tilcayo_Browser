package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.Plugin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AnimatedWallpaperView(
    presetId: String?,
    plugin: Plugin?,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (plugin != null && plugin.isWallpaperPlugin() && plugin.htmlCode.isNotBlank()) {
            // Render Custom HTML5 Canvas Wallpaper Plugin
            PluginWallpaperRunner(
                htmlCode = plugin.htmlCode,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Render Procedural Compose Canvas Wallpaper
            when (presetId) {
                "anim_stardust" -> StardustProceduralWallpaper(isDarkTheme)
                "anim_matrix" -> MatrixProceduralWallpaper()
                "anim_liquid_gold" -> LiquidGoldProceduralWallpaper()
                else -> AuroraProceduralWallpaper(isDarkTheme) // default animated
            }
        }
    }
}

/**
 * 60 FPS Procedural Aurora Waves
 */
@Composable
fun AuroraProceduralWallpaper(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_transition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val baseGradient = if (isDarkTheme) {
        listOf(Color(0xFF091219), Color(0xFF0A1826), Color(0xFF140F2D))
    } else {
        listOf(Color(0xFFF0FDF4), Color(0xFFE0F2FE), Color(0xFFF5F3FF))
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(baseGradient))
    ) {
        val w = size.width
        val h = size.height

        // Layer 1: Emerald Aurora Wave
        val path1 = Path().apply {
            moveTo(0f, h * 0.45f)
            for (x in 0..w.toInt() step 12) {
                val xf = x.toFloat()
                val y = h * 0.42f +
                        sin(xf * 0.004f + phase) * 65f +
                        cos(xf * 0.007f - phase * 0.8f) * 40f
                lineTo(xf, y)
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = path1,
            brush = Brush.verticalGradient(
                colors = listOf(
                    if (isDarkTheme) Color(0x6600F5D4) else Color(0x442DD4BF),
                    if (isDarkTheme) Color(0x3300B4D8) else Color(0x2238BDF8),
                    Color.Transparent
                ),
                startY = h * 0.35f,
                endY = h * 0.85f
            ),
            style = Fill
        )

        // Layer 2: Violet / Azure Ribbon Wave
        val path2 = Path().apply {
            moveTo(0f, h * 0.58f)
            for (x in 0..w.toInt() step 12) {
                val xf = x.toFloat()
                val y = h * 0.55f +
                        cos(xf * 0.005f + phase * 1.2f) * 75f +
                        sin(xf * 0.008f + phase) * 35f
                lineTo(xf, y)
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = path2,
            brush = Brush.verticalGradient(
                colors = listOf(
                    if (isDarkTheme) Color(0x557209B7) else Color(0x33A855F7),
                    if (isDarkTheme) Color(0x443A0CA3) else Color(0x22818CF8),
                    Color.Transparent
                ),
                startY = h * 0.45f,
                endY = h * 0.95f
            ),
            style = Fill
        )

        // Layer 3: Golden Glimmer Crest
        val path3 = Path().apply {
            moveTo(0f, h * 0.7f)
            for (x in 0..w.toInt() step 14) {
                val xf = x.toFloat()
                val y = h * 0.68f +
                        sin(xf * 0.006f - phase * 0.9f) * 50f +
                        cos(xf * 0.003f + phase) * 30f
                lineTo(xf, y)
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = path3,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x44E58A2B),
                    Color(0x11FFD166),
                    Color.Transparent
                ),
                startY = h * 0.6f,
                endY = h
            ),
            style = Fill
        )
    }
}

/**
 * Procedural Cosmic Stardust Particles with constellation connections
 */
private data class StarParticle(
    val xNorm: Float,
    val yNorm: Float,
    val radius: Float,
    val speed: Float,
    val phaseOffset: Float,
    val isGold: Boolean
)

@Composable
fun StardustProceduralWallpaper(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "stardust_transition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val particles = remember {
        val rand = Random(42)
        List(55) {
            StarParticle(
                xNorm = rand.nextFloat(),
                yNorm = rand.nextFloat(),
                radius = rand.nextFloat() * 2.8f + 1.2f,
                speed = rand.nextFloat() * 0.04f + 0.015f,
                phaseOffset = rand.nextFloat() * 6.28f,
                isGold = rand.nextBoolean()
            )
        }
    }

    val bgGradient = if (isDarkTheme) {
        listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0A0F1D))
    } else {
        listOf(Color(0xFFF8FAFC), Color(0xFFEEF2FF), Color(0xFFFEF3C7))
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgGradient))
    ) {
        val w = size.width
        val h = size.height

        val currentPoints = particles.map { p ->
            val curY = ((p.yNorm + time * p.speed) % 1.0f) * h
            val curX = (p.xNorm * w) + sin(time * 0.15f + p.phaseOffset) * 20f
            val pulse = (sin(time * 0.8f + p.phaseOffset) + 1f) / 2f
            val alpha = pulse * 0.6f + 0.35f
            val particleColor = if (p.isGold) {
                Color(0xFFF59E0B).copy(alpha = alpha)
            } else {
                Color(0xFF38BDF8).copy(alpha = alpha)
            }

            // Draw glowing halo
            drawCircle(
                color = particleColor.copy(alpha = alpha * 0.35f),
                radius = p.radius * 3.5f,
                center = Offset(curX, curY)
            )
            // Core
            drawCircle(
                color = if (isDarkTheme) Color.White.copy(alpha = alpha) else particleColor,
                radius = p.radius,
                center = Offset(curX, curY)
            )
            Offset(curX, curY)
        }

        // Connect nearby stars with faint glowing constellation lines
        for (i in 0 until currentPoints.size step 2) {
            val p1 = currentPoints[i]
            for (j in (i + 1) until currentPoints.size step 4) {
                val p2 = currentPoints[j]
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val distSq = dx * dx + dy * dy
                if (distSq < 140f * 140f) {
                    val lineAlpha = (1f - (distSq / (140f * 140f))) * 0.18f
                    drawLine(
                        color = (if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFFD97706)).copy(alpha = lineAlpha),
                        start = p1,
                        end = p2,
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}

/**
 * Cyber Matrix Rain Stream
 */
@Composable
fun MatrixProceduralWallpaper() {
    val infiniteTransition = rememberInfiniteTransition(label = "matrix_transition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "matrix_progress"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040A07))
    ) {
        val w = size.width
        val h = size.height
        val colCount = 22
        val colWidth = w / colCount

        for (i in 0 until colCount) {
            val offsetSpeed = ((i * 13) % 7 + 3) * 0.12f
            val headY = ((progress * offsetSpeed + (i * 0.17f)) % 1.0f) * (h + 300f) - 150f
            val x = i * colWidth + colWidth / 2f

            // Draw stream gradient line
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF00FF66).copy(alpha = 0.15f),
                        Color(0xFF00FF88).copy(alpha = 0.65f),
                        Color(0xFFFFFFFF)
                    ),
                    startY = headY - 260f,
                    endY = headY
                ),
                start = Offset(x, headY - 260f),
                end = Offset(x, headY),
                strokeWidth = 3f
            )

            // Draw bright glowing head orb
            drawCircle(
                color = Color(0xFF00FF88).copy(alpha = 0.5f),
                radius = 7f,
                center = Offset(x, headY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = Offset(x, headY)
            )
        }
    }
}

/**
 * Liquid Gold & Pearl Caustic Waves
 */
@Composable
fun LiquidGoldProceduralWallpaper() {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_gold")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xFFFEF9C3), Color(0xFFFDE68A))
                )
            )
    ) {
        val w = size.width
        val h = size.height

        for (i in 0..3) {
            val path = Path().apply {
                val startY = h * (0.35f + i * 0.15f)
                moveTo(0f, startY)
                for (x in 0..w.toInt() step 15) {
                    val xf = x.toFloat()
                    val y = startY +
                            sin(xf * 0.005f + phase + i * 1.3f) * 45f +
                            cos(xf * 0.008f - phase) * 25f
                    lineTo(xf, y)
                }
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x33D97706),
                        Color(0x1ADB8A00),
                        Color.Transparent
                    ),
                    startY = h * (0.3f + i * 0.12f),
                    endY = h
                ),
                style = Fill
            )
        }
    }
}

/**
 * Embedded Hardware-Accelerated Sandbox for User HTML5/Canvas Wallpaper Plugins
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PluginWallpaperRunner(
    htmlCode: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(AndroidColor.TRANSPARENT)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            isClickable = false
            isFocusable = false
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.destroy()
        }
    }

    AndroidView(
        factory = {
            webView.apply {
                loadDataWithBaseURL(
                    "https://tilcayo.local/wallpaper/",
                    htmlCode,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { wv ->
            wv.loadDataWithBaseURL(
                "https://tilcayo.local/wallpaper/",
                htmlCode,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier
    )
}
