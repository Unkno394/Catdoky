package com.example.sudoky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.sudoky.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isDarkTheme: Boolean,
    onDone: () -> Unit
) {

    // 0.0 .. 1.0
    var loadProgress by remember { mutableFloatStateOf(0f) }

    // Эмуляция загрузки (потом можно заменить на реальную)
    LaunchedEffect(Unit) {
        while (loadProgress < 1f) {
            delay(30) // скорость заполнения (меньше = быстрее)
            loadProgress = (loadProgress + 0.01f).coerceAtMost(1f)
        }
        onDone()
    }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.cat)
    )

    // Кот играет по кругу, пока идёт загрузка
    val animProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = loadProgress < 1f
    )

    val background = if (isDarkTheme) Color(0xFF1A1E24) else Color(0xFFF7F8FF)
    val accent = if (isDarkTheme) Color(0xFF949CCD) else Color(0xFFADB8F5)
    val track = if (isDarkTheme) Color(0xFF2A2F38) else Color(0xFFCFD3E7)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            LottieAnimation(
                composition = composition,
                progress = { animProgress },
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(Modifier.height(18.dp))

            LinearProgressIndicator(
                progress = { loadProgress },
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(6.dp),
                color = accent,
                trackColor = track
            )
        }
    }
}
