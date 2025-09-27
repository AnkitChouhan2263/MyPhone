package com.example.myphone.features.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define the DataStore instance at the top level for the whole app to use
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Define enums for type-safe setting options
enum class ThemeSetting { SYSTEM, LIGHT, DARK }
enum class AvatarStyle { INITIALS, ICON }
enum class DialerLayout { STANDARD, COMPACT }

class SettingsRepository(private val context: Context) {

    // Define keys for each preference we want to store
    private val themePreferenceKey = stringPreferencesKey("theme_preference")
    private val avatarStylePreferenceKey = stringPreferencesKey("avatar_style_preference")
    private val dialerLayoutPreferenceKey = stringPreferencesKey("dialer_layout_preference")

    // A Flow that emits the current theme setting whenever it changes
    val themeSetting: Flow<ThemeSetting> = context.dataStore.data.map { preferences ->
        ThemeSetting.valueOf(preferences[themePreferenceKey] ?: ThemeSetting.SYSTEM.name)
    }

    // A Flow that emits the current avatar style setting whenever it changes
    val avatarStyle: Flow<AvatarStyle> = context.dataStore.data.map { preferences ->
        AvatarStyle.valueOf(preferences[avatarStylePreferenceKey] ?: AvatarStyle.INITIALS.name)
    }

    // A Flow that emits the current dialer layout setting whenever it changes
    val dialerLayout: Flow<DialerLayout> = context.dataStore.data.map { preferences ->
        DialerLayout.valueOf(preferences[dialerLayoutPreferenceKey] ?: DialerLayout.STANDARD.name)
    }

    // Suspend functions to update and save the new preferences
    suspend fun setThemeSetting(themeSetting: ThemeSetting) {
        context.dataStore.edit { settings ->
            settings[themePreferenceKey] = themeSetting.name
        }
    }

    suspend fun setAvatarStyle(avatarStyle: AvatarStyle) {
        context.dataStore.edit { settings ->
            settings[avatarStylePreferenceKey] = avatarStyle.name
        }
    }

    suspend fun setDialerLayout(dialerLayout: DialerLayout) {
        context.dataStore.edit { settings ->
            settings[dialerLayoutPreferenceKey] = dialerLayout.name
        }
    }
}

