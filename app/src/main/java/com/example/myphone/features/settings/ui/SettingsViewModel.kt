package com.example.myphone.features.settings.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myphone.features.settings.data.AvatarStyle
import com.example.myphone.features.settings.data.DialerLayout
import com.example.myphone.features.settings.data.SettingsRepository
import com.example.myphone.features.settings.data.ThemeSetting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    // Expose each setting as a StateFlow for the UI to observe
    val themeSetting: StateFlow<ThemeSetting> = repository.themeSetting
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeSetting.SYSTEM)

    val avatarStyle: StateFlow<AvatarStyle> = repository.avatarStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AvatarStyle.INITIALS)

    val dialerLayout: StateFlow<DialerLayout> = repository.dialerLayout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DialerLayout.STANDARD)

    // Functions to be called by the UI to update the settings
    fun updateTheme(newTheme: ThemeSetting) {
        viewModelScope.launch { repository.setThemeSetting(newTheme) }
    }

    fun updateAvatarStyle(newStyle: AvatarStyle) {
        viewModelScope.launch { repository.setAvatarStyle(newStyle) }
    }

    fun updateDialerLayout(newLayout: DialerLayout) {
        viewModelScope.launch { repository.setDialerLayout(newLayout) }
    }
}

