package com.example.myphone.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.min
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myphone.features.settings.data.AvatarStyle

/**
 * A smart avatar composable that displays the contact's photo if available,
 * otherwise it shows their initials on a colored background.
 */
@Composable
fun ContactAvatar(
    name: String,
    photoUri: String?,
    avatarStyle: AvatarStyle,
    modifier: Modifier = Modifier
) {
    if (!photoUri.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUri)
                .crossfade(true)
                .build(),
            contentDescription = "$name's photo",
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        InitialsAvatar(name = name, avatarStyle = avatarStyle, modifier = modifier)
    }
}

/**
 * A composable that displays a contact's initials on a deterministically generated colored background.
 * If the name is blank, it shows a generic person icon instead.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun InitialsAvatar(
    name: String,
    avatarStyle: AvatarStyle,
    modifier: Modifier = Modifier
) {
    // Check if the system is currently in dark theme.
    val isDarkTheme = isSystemInDarkTheme()
    // The color pair is now remembered based on both the name and the current theme.
    val colorPair = remember(name, isDarkTheme) { generateAvatarColors(name, isDarkTheme) }
    val initials = remember(name) { getInitials(name) }

    Surface(
        color = colorPair.background,
        modifier = modifier.clip(CircleShape)
    ) {
        // Use BoxWithConstraints to measure the available space for the avatar.
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val dynamicFontSize = LocalDensity.current.run { (min(maxWidth, maxHeight).toPx() / 2.5f).toSp() }

            if (avatarStyle == AvatarStyle.INITIALS && initials.isNotBlank()) {
                Text(
                    text = initials,
                    // THE FIX: Use the dynamically calculated font size.
                    fontSize = dynamicFontSize,
                    color = colorPair.foreground,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Contact",
                    tint = colorPair.foreground,
                    modifier = Modifier.fillMaxSize(0.6f)
                )
            }
        }
    }
}

/**
 * A data class to hold the generated color pair.
 */
data class AvatarColor(val background: Color, val foreground: Color)


/**
 * Generates the initials from a full name.
 * e.g., "John Doe" -> "JD", "SingleName" -> "S"
 */
private fun getInitials(name: String): String {
    return name.split(' ')
        .filter { it.isNotBlank() }
        .take(2).joinToString("") { it.first().uppercase() }
}

/**
 * Generates a stable, unique pair of background and foreground colors based on the contact's name
 * and the current system theme (light/dark).
 */
private fun generateAvatarColors(name: String, isDarkTheme: Boolean): AvatarColor {
    val hash = name.hashCode()
    // Use HSL (Hue, Saturation, Lightness) color space for more pleasing, vibrant colors.
    val hue = (hash and 0xFFFFFF) % 360f

    val background: Color
    val foreground: Color

    if (isDarkTheme) {
        // Dark theme: Dark, muted background with a light, vibrant foreground.
        background = Color.hsl(hue, 0.5f, 0.12f)
        foreground = Color.hsl(hue, 0.45f, 0.85f)
    } else {
        // Light theme: Light, vibrant background with a dark, muted foreground.
        background = Color.hsl(hue, 0.5f, 0.90f)
        foreground = Color.hsl(hue, 0.55f, 0.30f)
    }

    return AvatarColor(background, foreground)
}