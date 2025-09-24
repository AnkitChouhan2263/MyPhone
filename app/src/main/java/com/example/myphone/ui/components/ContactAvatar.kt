package com.example.myphone.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * A smart avatar composable that displays the contact's photo if available,
 * otherwise it shows their initials on a colored background.
 */
@Composable
fun ContactAvatar(
    name: String,
    photoUri: String?,
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
        InitialsAvatar(name = name, modifier = modifier)
    }
}

/**
 * A composable that displays a contact's initials on a deterministically generated colored background.
 * If the name is blank, it shows a generic person icon instead.
 */
@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    val colorPair = remember(name) { generateAvatarColors(name) }
    val initials = remember(name) { getInitials(name) }

    Surface(
        color = colorPair.background,
        modifier = modifier.clip(CircleShape)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (initials.isNotBlank()) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorPair.foreground,
                    fontWeight = FontWeight.Bold
                )
            } else {
                // If there are no initials, show a generic person icon.
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Unknown Contact",
                    tint = colorPair.foreground,
                    modifier = Modifier.fillMaxSize(0.6f) // Make icon slightly smaller than circle
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
 * Generates a stable, unique pair of background and foreground colors based on the contact's name.
 * The same name will always produce the same color pair.
 */
private fun generateAvatarColors(name: String): AvatarColor {
    val hash = name.hashCode()
    // Use HSL (Hue, Saturation, Lightness) color space for more pleasing, vibrant colors.
    val hue = (hash and 0xFFFFFF) % 360f
    // Generate a darker color for the background
    val background = Color.hsl(hue, 0.5f, 0.15f) // Lowered lightness for an even darker background
    // Generate a lighter color for the foreground text
    val foreground = Color.hsl(hue, 0.45f, 0.75f) // Higher lightness for a lighter text

    return AvatarColor(background, foreground)
}
