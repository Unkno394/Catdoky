package com.example.sudoky.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProgressPreferences(private val context: Context) {

    private val xpKey = intPreferencesKey("xp_total")
    private val tutorialShownKey = booleanPreferencesKey("tutorial_shown")

    val totalXpFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[xpKey] ?: 0
    }

    val tutorialShownFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[tutorialShownKey] ?: false
    }

    suspend fun addXp(amount: Int) {
        if (amount <= 0) return
        context.dataStore.edit { prefs ->
            val current = prefs[xpKey] ?: 0
            prefs[xpKey] = current + amount
        }
    }

    suspend fun setTutorialShown(shown: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[tutorialShownKey] = shown
        }
    }
}
