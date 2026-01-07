package com.example.sudoky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.sudoky.R
import com.example.sudoky.sudoku.Difficulty
import com.example.sudoky.data.pawsForTimeWinOnly

@Composable
fun WinScreen(
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    elapsedSeconds: Int,
    onMenu: () -> Unit,
    onNewGame: (Difficulty) -> Unit
) {
    ResultScreen(
        isDarkTheme = isDarkTheme,
        title = "Победа!",
        buttonText = "Новая игра",
        isWin = true,
        difficulty = difficulty,
        elapsedSeconds = elapsedSeconds,
        onMenu = onMenu,
        onNewGame = { onNewGame(difficulty) },
        animation = {
            WinAnimation(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(0.98f)
                    .height(440.dp)
            )
        }
    )
}

@Composable
fun LoseScreen(
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    elapsedSeconds: Int,
    onMenu: () -> Unit,
    onNewGame: (Difficulty) -> Unit
) {
    ResultScreen(
        isDarkTheme = isDarkTheme,
        title = "Поражение",
        buttonText = "Новая игра",
        isWin = false,
        difficulty = difficulty,
        elapsedSeconds = elapsedSeconds,
        onMenu = onMenu,
        onNewGame = { onNewGame(difficulty) },
        animation = {
            LoseAnimation(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(0.98f)
                    .height(440.dp)
            )
        }
    )
}

@Composable
private fun ResultScreen(
    isDarkTheme: Boolean,
    title: String,
    buttonText: String,
    isWin: Boolean,
    difficulty: Difficulty,
    elapsedSeconds: Int,
    onMenu: () -> Unit,
    onNewGame: () -> Unit,
    animation: @Composable () -> Unit
) {
    val background = if (isDarkTheme) Color(0xFF1A1E24) else Color(0xFFF7F8FF)
    val accent = if (isDarkTheme) Color(0xFF949CCD) else Color(0xFFADB8F5)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF0B0F14)
    val reactionText = if (isWin) "Кот доволен." else "Кот расстроен..."
    val paws = pawsForTimeWinOnly(difficulty, elapsedSeconds, isWin)
    val pawColor = Color(0xFFFFD54F)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = onMenu,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Text(
                text = "В меню",
                color = accent,
                fontSize = 16.sp
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            animation()
            if (paws > 0) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(paws) {
                        Icon(
                            imageVector = Icons.Filled.Pets,
                            contentDescription = null,
                            tint = pawColor,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }
                }
            }
            Text(
                text = title,
                color = textColor,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Время: ${formatElapsedTime(elapsedSeconds)}",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = reactionText,
                color = textColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun LoseAnimation(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loos)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1f
    )
    val frozenProgress = if (progress >= 0.75f) 0.75f else progress
    LottieAnimation(
        composition = composition,
        progress = { frozenProgress },
        modifier = modifier
    )
}

@Composable
fun WinAnimation(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.win)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1f
    )
    val frozenProgress = if (progress >= 0.75f) 0.75f else progress
    LottieAnimation(
        composition = composition,
        progress = { frozenProgress },
        modifier = modifier
    )
}

private fun formatElapsedTime(seconds: Int): String {
    val hours = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return "%02d:%02d:%02d".format(hours, mins, secs)
}
