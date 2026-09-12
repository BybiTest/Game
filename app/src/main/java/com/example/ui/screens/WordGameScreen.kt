package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameLevelsData
import com.example.data.UserEntity
import com.example.ui.WordGameState
import com.example.ui.components.TapsellStandardBanner
import com.example.ui.theme.GameTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun WordGameScreen(
    state: WordGameState,
    user: UserEntity?,
    theme: GameTheme,
    onSelectLetter: (Char) -> Unit,
    onRemoveLastLetter: () -> Unit,
    onClearLetters: () -> Unit,
    onSubmitWord: () -> Unit,
    onShuffleLetters: () -> Unit,
    onNextLevel: () -> Unit,
    onDismissWinDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level = GameLevelsData.wordLevels.getOrElse(state.currentLevelIndex) {
        GameLevelsData.wordLevels.first()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.backgroundGradient)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // REAL TAPSELL STANDARD BANNER AT TOP
            TapsellStandardBanner(modifier = Modifier.padding(bottom = 12.dp))

            // Level Title & Persian Theme Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.primaryVariant.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "${level.title} - ${level.theme}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.accent.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.accent.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "پیدا شده: ${state.foundWords.size} از ${level.targetWords.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Target Words Display Slots
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardBackground.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    level.targetWords.forEach { targetWord ->
                        val isFound = state.foundWords.contains(targetWord)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            targetWord.forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .shadow(if (isFound) 3.dp else 1.dp, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isFound) Color(0xFF2E7D32) else theme.letterWheelCenter
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isFound) Color(0xFF81C784) else theme.cardBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isFound) char.toString() else "",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFound) Color.White else theme.textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Feedback Message
            AnimatedVisibility(visible = state.feedbackMessage != null) {
                state.feedbackMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (msg.contains("آفرین")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (msg.contains("آفرین")) Color(0xFF81C784) else Color(0xFFE57373)
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = msg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (msg.contains("آفرین")) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Word Construction Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.primaryVariant.copy(alpha = 0.6f)),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onRemoveLastLetter,
                        enabled = state.selectedLetters.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "حذف حرف",
                            tint = if (state.selectedLetters.isNotEmpty()) theme.primary else Color.Gray
                        )
                    }

                    Text(
                        text = if (state.selectedLetters.isNotEmpty()) state.selectedLetters.joinToString("") else "حروف را وصل کنید",
                        fontSize = if (state.selectedLetters.isNotEmpty()) 22.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.selectedLetters.isNotEmpty()) theme.accent else theme.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = onSubmitWord,
                        enabled = state.selectedLetters.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "ثبت کلمه",
                            tint = if (state.selectedLetters.isNotEmpty()) Color(0xFF2E7D32) else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CIRCULAR LETTER WHEEL
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // Center Shuffle Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(theme.letterWheelCenter)
                        .border(2.dp, theme.primaryVariant, CircleShape)
                        .clickable { onShuffleLetters() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "بر زدن",
                        tint = theme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                val letterCount = state.shuffledLetters.size
                val wheelRadiusDp = 86.dp
                state.shuffledLetters.forEachIndexed { index, char ->
                    val angleRad = (2 * PI * index / letterCount) - (PI / 2)
                    val offsetX = (wheelRadiusDp.value * cos(angleRad)).roundToInt().dp
                    val offsetY = (wheelRadiusDp.value * sin(angleRad)).roundToInt().dp
                    Box(
                        modifier = Modifier
                            .offset(offsetX, offsetY)
                            .size(54.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(theme.letterButtonGradient))
                            .border(2.dp, theme.accent, CircleShape)
                            .clickable { onSelectLetter(char) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clear Button
            OutlinedButton(
                onClick = onClearLetters,
                enabled = state.selectedLetters.isNotEmpty(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("پاک کردن", fontSize = 12.sp)
            }
        }

        // Win Dialog
        if (state.showWinDialog) {
            AlertDialog(
                onDismissRequest = onDismissWinDialog,
                title = {
                    Text(
                        text = "تبریک! مرحله تکمیل شد",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                },
                text = {
                    Text(
                        text = "آفرین! تمام کلمات مرحله را با موفقیت پیدا کردید.\n+${level.coinReward} سکه دریافت کردید!",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDismissWinDialog()
                            onNextLevel()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("مرحله بعدی", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
