package com.example.sudoky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.sudoky.navigation.AppNav
import com.example.sudoky.data.ThemePreferences
import com.example.sudoky.ui.theme.SudokyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val themePreferences by lazy { ThemePreferences(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()
            val savedDarkTheme by themePreferences.darkThemeFlow.collectAsState(initial = null)
            val isDarkTheme = savedDarkTheme ?: systemDark
            val scope = rememberCoroutineScope()

            SudokyTheme(darkTheme = isDarkTheme) {
                AppNav(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { useDark ->
                        scope.launch { themePreferences.setDarkTheme(useDark) }
                    }
                )
            }
        }
    }
}
