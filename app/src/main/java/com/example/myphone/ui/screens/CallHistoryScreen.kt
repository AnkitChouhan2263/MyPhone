package com.example.myphone.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myphone.features.recents.data.CallLogEntry
import com.example.myphone.features.recents.ui.CallHistoryViewModel
import com.example.myphone.ui.components.ContactAvatar
import com.example.myphone.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    navController: NavController,
    viewModel: CallHistoryViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasCallPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCallPermission = isGranted }
    )

    fun placeCall(isVideo: Boolean) {
        if (uiState.numberToCall.isBlank()) return

        if (hasCallPermission) {
            // THE FIX: Use ACTION_CALL for both, but add an extra for video.
            val intent = Intent(Intent.ACTION_CALL, "tel:${uiState.numberToCall}".toUri())
            if (isVideo) {
                // This is the official, more direct way to request a video call.
                intent.putExtra(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, true)
            }
            context.startActivity(intent)
        } else {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "History for ${uiState.contactIdentifier}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // New bottom bar for call actions.
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { placeCall(false) }, enabled = !uiState.isLoading) {
                        Icon(Icons.Default.Call, contentDescription = "Call")
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Call")
                    }
                    Button(onClick = { placeCall(true) }, enabled = !uiState.isLoading) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Video Call")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                EmptyState(
                    title = "Error",
                    message = "Could not load call history.",
                    icon = Icons.Default.Warning
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(uiState.callHistory, key = { it.id }) { entry ->
                        CallHistoryItem(entry = entry)
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                    }
                }
            }
        }
    }
}

/**
 * A new, dedicated composable for a single item in the call history list.
 */
@Composable
private fun CallHistoryItem(entry: CallLogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            name = if (entry.name != "Unknown") entry.name else entry.number,
            photoUri = entry.photoUri,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            val primaryText = when {
                entry.name != "Unknown" -> entry.name
                entry.number.isNotBlank() -> entry.number
                else -> "Unknown Number"
            }
            Text(
                text = primaryText,
                style = MaterialTheme.typography.bodyLarge
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                CallTypeIcon(type = entry.type)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = entry.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // Display the call duration on the right.
        Text(
            text = formatDuration(entry.duration),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A helper function to format call duration from seconds to a readable m:ss format.
 */
private fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return ""
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}

