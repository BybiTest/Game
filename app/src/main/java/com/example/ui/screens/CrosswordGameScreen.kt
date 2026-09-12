package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CrosswordLevel
import com.example.data.GameLevelsData
import com.example.data.UserEntity
import com.example.ui.CrosswordGameState
import com.example.ui.components.TapsellStandardBanner
import com.example.ui.theme.GameTheme

@Composable
fun CrosswordGameScreen(
    state: CrosswordGameState,
    user: UserEntity?,
    theme: GameTheme,
    onSelectCell: (Int, Int) -> Unit,
    onInputChar: (Char) -> Unit,
    onClearCell: () -> Unit,
    onNextLevel: () -> Unit,
    onDismissWinDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level: CrosswordLevel = GameLevelsData.crosswordLevels.getOrElse(state.currentLevelIndex) {
        GameLevelsData.crosswordLevels.first()
    }

    val alphabet = listOf('م', 'ا', 'ه', 'و', 'ر', 'ت', 'س', 'ب')

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tapsell Banner at top
        TapsellStandardBanner(modifier = Modifier.padding(bottom = 12.dp))

        // Level Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = level.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Text(
                    text = "جایزه: ${level.coinReward} سکه",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Clues
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                level.clues.forEach { clue ->
                    Text(
                        text = "${if (clue.isAcross) "افقی" else "عمودی"} ${clue.number}: ${clue.clueText} (${clue.answer.length} حرف)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (r in 0 until level.rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (c in 0 until level.cols) {
                            val activeCell = level.cells.find { it.row == r && it.col == c }
                            val isSelected = state.selectedCell?.let { it.first == r && it.second == c } ?: false
                            val enteredChar = state.enteredGrid[r to c]

                            if (activeCell != null) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) Color(0xFFFFE082)
                                            else if (enteredChar == activeCell.correctChar) Color(0xFFC8E6C9)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFFFF8F00) else Color.LightGray,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { onSelectCell(r, c) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = enteredChar?.toString() ?: "",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color(0xFFE2E8F0))
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Keyboard Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            alphabet.forEach { char ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0F2FE))
                        .clickable { onInputChar(char) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = char.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onClearCell,
            enabled = state.selectedCell != null
        ) {
            Icon(Icons.Default.Backspace, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("پاک کردن خانه")
        }

        if (state.showWinDialog) {
            AlertDialog(
                onDismissRequest = onDismissWinDialog,
                title = { Text("آفرین! جدول حل شد") },
                text = { Text("+${level.coinReward} سکه دریافت کردید!") },
                confirmButton = {
                    Button(onClick = {
                        onDismissWinDialog()
                        onNextLevel()
                    }) {
                        Text("مرحله بعد")
                    }
                }
            )
        }
    }
}
