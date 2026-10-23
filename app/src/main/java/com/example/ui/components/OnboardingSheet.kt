package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.TilcayoAiBlue
import com.example.ui.theme.TilcayoGold

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val imageRes: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingSheet(
    onComplete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentStep by remember { mutableIntStateOf(0) }

    val steps = listOf(
        OnboardingStep(
            title = "Добро пожаловать в Тилкайо!",
            subtitle = "Версия 0.1B Beta",
            description = "Современный быстрый Android-браузер с очаровательным маскотом, живыми обоями и поддержкой плагинов.",
            icon = Icons.Default.Public,
            accentColor = TilcayoGold,
            imageRes = R.drawable.tilcayo_logo
        ),
        OnboardingStep(
            title = "Живые и авторские обои",
            subtitle = "Зацикленная анимация для светлой и темной тем",
            description = "Наслаждайтесь парящими частицами пыли, утренней росой или создавайте свои живые обои через открытую систему плагинов.",
            icon = Icons.Default.Palette,
            accentColor = Color(0xFF60A5FA)
        ),
        OnboardingStep(
            title = "Умный Тилкайо & Поиск в сети",
            subtitle = "ИИ-ассистент с поиском в Google и голосовой озвучкой",
            description = "Задавайте вопросы, анализируйте изображения, переключайте поиск в сети и слушайте озвучку ответов через синтез речи.",
            icon = Icons.Default.AutoAwesome,
            accentColor = TilcayoGold,
            imageRes = R.drawable.tilcayo_smart
        ),
        OnboardingStep(
            title = "Магазин плагинов и Объектив",
            subtitle = "Бесконечные возможности персонализации",
            description = "Устанавливайте расширения из магазина, создавайте свои скрипты и выполняйте поиск по картинкам через Объектив.",
            icon = Icons.Default.Extension,
            accentColor = TilcayoAiBlue
        )
    )

    ModalBottomSheet(
        onDismissRequest = onComplete,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("onboarding_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Step Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (index == currentStep) 24.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentStep) TilcayoGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step Content Animated
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_content"
            ) { stepIdx ->
                val step = steps[stepIdx]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Visual Avatar / Icon Box
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(step.accentColor.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )
                            .border(2.dp, step.accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (step.imageRes != null) {
                            Image(
                                painter = painterResource(id = step.imageRes),
                                contentDescription = null,
                                modifier = Modifier.size(76.dp)
                            )
                        } else {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = step.accentColor,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = step.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = step.subtitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = step.accentColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = step.description,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Button
                OutlinedButton(
                    onClick = onComplete,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("skip_onboarding_btn")
                ) {
                    Text("Пропустить")
                }

                // Next or Finish Button
                Button(
                    onClick = {
                        if (currentStep < steps.size - 1) {
                            currentStep++
                        } else {
                            onComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TilcayoGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("next_onboarding_btn")
                ) {
                    if (currentStep < steps.size - 1) {
                        Text("Далее")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Начать серфинг!")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
