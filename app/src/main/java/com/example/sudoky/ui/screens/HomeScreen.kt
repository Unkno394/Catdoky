package com.example.sudoky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.sudoky.R
import com.example.sudoky.data.ProgressPreferences
import com.example.sudoky.data.levelProgress
import com.example.sudoky.data.requiredLevelForDifficulty
import com.example.sudoky.sudoku.Difficulty
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onNewGame: (Difficulty) -> Unit
) {
    var showLevels by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val bg = if (isDarkTheme) Color(0xFF1A1E24) else Color(0xFFF7F8FF) // тёмно-серый вместо почти черного
    val sheetBg = if (isDarkTheme) Color(0xFF15181E) else bg
    val accent = if (isDarkTheme) Color(0xFF949CCD) else Color(0xFFADB8F5)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF0B0F14)
    val context = LocalContext.current
    val progressPrefs = remember { ProgressPreferences(context) }
    val totalXp by progressPrefs.totalXpFlow.collectAsState(initial = 0)
    val lp = levelProgress(totalXp)
    val level = lp.level
    val progress = lp.progress

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.mainone)
    )

    // Котик на главном можно крутить в луп (выглядит “живым”)
    val animProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 12.dp)
        ) {
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    strokeWidth = 5.dp,
                    color = accent,
                    trackColor = accent.copy(alpha = 0.25f),
                    modifier = Modifier
                        .matchParentSize()
                        .padding(6.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.progress),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize()
                )
                Text(
                    text = level.toString(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-30).dp)
        ) {
            // Кот в центре
            LottieAnimation(
                composition = composition,
                progress = { animProgress },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(260.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Catoky",
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }

        IconButton(
            onClick = { onToggleTheme(!isDarkTheme) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp)
                .size(44.dp)
                .zIndex(1f)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = "Toggle theme",
                tint = accent
            )
        }

        Button(
            onClick = { showLevels = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Новая игра",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        if (showLevels) {
            ModalBottomSheet(
                onDismissRequest = { showLevels = false },
                sheetState = sheetState,
                containerColor = sheetBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Выберите уровень",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )

                    val levels = listOf(
                        "Новичок" to Difficulty.NOVICE_9,
                        "Стандарт" to Difficulty.STANDARD_9,
                        "Тяжелый" to Difficulty.HARD_9,
                        "Судоку 16x16" to Difficulty.STANDARD_16
                    )

                    levels.forEach { (title, diff) ->
                        val requiredLevel = requiredLevelForDifficulty(diff)
                        val isUnlocked = level >= requiredLevel
                        Button(
                            onClick = {
                                if (isUnlocked) {
                                    scope.launch {
                                        sheetState.hide()
                                        showLevels = false
                                        onNewGame(diff)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isUnlocked) {
                                    accent
                                } else {
                                    accent.copy(alpha = 0.4f)
                                },
                                contentColor = if (isUnlocked) {
                                    Color.White
                                } else {
                                    Color.White.copy(alpha = 0.7f)
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            enabled = isUnlocked
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Filled.Lock,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .size(18.dp)
                                                .padding(end = 6.dp)
                                        )
                                    }
                                    Text(
                                        text = title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isUnlocked) {
                                            Color.White
                                        } else {
                                            Color.White.copy(alpha = 0.7f)
                                        }
                                    )
                                }
                                if (!isUnlocked) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Доступно с уровня $requiredLevel",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
