package com.example.myphone.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myphone.features.settings.data.AvatarStyle
import com.example.myphone.features.settings.data.DialerLayout
import com.example.myphone.features.settings.data.ThemeSetting
import com.example.myphone.features.settings.ui.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val currentTheme by viewModel.themeSetting.collectAsState()
    val currentAvatarStyle by viewModel.avatarStyle.collectAsState()
    val currentDialerLayout by viewModel.dialerLayout.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { SectionTitle("Appearance") }
            item {
                ThemeSelector(
                    currentTheme = currentTheme,
                    onThemeSelected = { viewModel.updateTheme(it) }
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
            item { SectionTitle("Contacts") }
            item {
                AvatarStyleSelector(
                    currentStyle = currentAvatarStyle,
                    onStyleSelected = { viewModel.updateAvatarStyle(it) }
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
            item { SectionTitle("Dialer") }
            item {
                DialerLayoutSelector(
                    currentLayout = currentDialerLayout,
                    onLayoutSelected = { viewModel.updateDialerLayout(it) }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ThemeSelector(
    currentTheme: ThemeSetting,
    onThemeSelected: (ThemeSetting) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SettingsOption(
            title = "Light",
            isSelected = currentTheme == ThemeSetting.LIGHT,
            onClick = { onThemeSelected(ThemeSetting.LIGHT) }
        )
        SettingsOption(
            title = "Dark",
            isSelected = currentTheme == ThemeSetting.DARK,
            onClick = { onThemeSelected(ThemeSetting.DARK) }
        )
        SettingsOption(
            title = "System Default",
            isSelected = currentTheme == ThemeSetting.SYSTEM,
            onClick = { onThemeSelected(ThemeSetting.SYSTEM) }
        )
    }
}

@Composable
private fun AvatarStyleSelector(
    currentStyle: AvatarStyle,
    onStyleSelected: (AvatarStyle) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SettingsOption(
            title = "Show Initials",
            isSelected = currentStyle == AvatarStyle.INITIALS,
            onClick = { onStyleSelected(AvatarStyle.INITIALS) }
        )
        SettingsOption(
            title = "Show Generic Icon",
            isSelected = currentStyle == AvatarStyle.ICON,
            onClick = { onStyleSelected(AvatarStyle.ICON) }
        )
    }
}

@Composable
private fun DialerLayoutSelector(
    currentLayout: DialerLayout,
    onLayoutSelected: (DialerLayout) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SettingsOption(
            title = "Standard",
            isSelected = currentLayout == DialerLayout.STANDARD,
            onClick = { onLayoutSelected(DialerLayout.STANDARD) }
        )
        SettingsOption(
            title = "Compact",
            isSelected = currentLayout == DialerLayout.COMPACT,
            onClick = { onLayoutSelected(DialerLayout.COMPACT) }
        )
    }
}

@Composable
private fun SettingsOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}

