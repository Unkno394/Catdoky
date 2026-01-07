package com.example.sudoky.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemePreferences(private val context: Context) {

    private val darkThemeKey = booleanPreferencesKey("dark_theme")

    val darkThemeFlow: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[darkThemeKey]
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[darkThemeKey] = enabled
        }
    }
}
