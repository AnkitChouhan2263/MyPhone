package com.example.myphone.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
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
 */
@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    // `remember` ensures the color and initials are calculated only once for each contact
    // and preserved across recompositions.
    val backgroundColor = remember(name) { generateAvatarColor(name) }
    val initials = remember(name) { getInitials(name) }

    Surface(
        color = backgroundColor,
        modifier = modifier.clip(CircleShape)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

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
 * Generates a stable, unique background color based on the contact's name.
 * The same name will always produce the same color.
 */
private fun generateAvatarColor(name: String): Color {
    val hash = name.hashCode()
    // Use HSL (Hue, Saturation, Lightness) color space for more pleasing, vibrant colors.
    val hue = (hash and 0xFFFFFF) % 360f
    return Color.hsl(hue, 0.5f, 0.4f)
}
