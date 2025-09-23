package com.example.myphone.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myphone.features.recents.data.CallLogEntry
import com.example.myphone.features.recents.data.CallType
import com.example.myphone.features.recents.ui.RecentsViewModel
import com.example.myphone.ui.components.EmptyState

@Composable
fun RecentsScreen(recentsViewModel: RecentsViewModel = viewModel()) {
    val context = LocalContext.current
    // --- Permission for READING the call log ---
    var hasReadPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val readPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasReadPermission = isGranted
        }
    )

    // --- Permission for MAKING calls (THE FIX) ---
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
        onResult = { isGranted ->
            hasCallPermission = isGranted
        }
    )

    // Request permission when the screen is first composed
    LaunchedEffect(key1 = Unit) {
        if (!hasReadPermission) {
            readPermissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
        }
    }


    if (hasReadPermission) {
        // Fetch recents if permission is granted
        LaunchedEffect(key1 = Unit) {
            recentsViewModel.fetchRecents()
        }
        val uiState by recentsViewModel.uiState.collectAsState()
        when (val state = uiState) {
            is RecentsViewModel.RecentsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RecentsViewModel.RecentsUiState.Success -> {
                if (state.callLog.isNotEmpty()) {
                    CallLogList(
                        callLog = state.callLog,
                        onCall = { number ->
                            // THE FIX: Check for call permission before making the call.
                            if (hasCallPermission) {
                                val intent = Intent(Intent.ACTION_CALL, "tel:$number".toUri())
                                context.startActivity(intent)
                            } else {
                                callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                            }
                        }
                    )
                } else {
                    EmptyState(
                        title = "No recent calls",
                        message = "Your call history will appear here.",
                        icon = Icons.Default.History
                    )
                }
            }
            is RecentsViewModel.RecentsUiState.Error -> {
                EmptyState(
                    title = "Error",
                    message = "Failed to load your call log. Please try again later.",
                    icon = Icons.Default.Warning
                )
            }
        }
    } else {
        EmptyState(
            title = "Permission needed",
            message = "This app needs to read your call log to display your history.",
            icon = Icons.Default.History,
            actionText = "Grant Permission",
            onAction = { readPermissionLauncher.launch(Manifest.permission.READ_CALL_LOG) }
        )
    }
}

@Composable
fun CallLogList(callLog: List<CallLogEntry>, onCall: (String) -> Unit) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(callLog) { entry ->
            // Pass the onCall lambda down to the item.
            CallLogItem(entry = entry, onCall = onCall)
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
    }
}

@Composable
fun CallLogItem(entry: CallLogEntry, onCall: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CallTypeIcon(type = entry.type)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (entry.type == CallType.MISSED) FontWeight.Bold else FontWeight.Normal,
                color = if (entry.type == CallType.MISSED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = entry.number,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = entry.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // The onCall lambda now contains the safe, permission-checking logic.
        IconButton(onClick = { onCall(entry.number) }) {
            Icon(Icons.Default.Call, contentDescription = "Call back")
        }
    }
}

@Composable
fun CallTypeIcon(type: CallType) {
    val icon: ImageVector
    val color: Color
    when (type) {
        CallType.INCOMING -> {
            icon = Icons.AutoMirrored.Filled.CallReceived
            color = Color(0xFF388E3C) // Green
        }

        CallType.OUTGOING -> {
            icon = Icons.AutoMirrored.Filled.CallMade
            color = Color(0xFF1976D2) // Blue
        }

        CallType.MISSED -> {
            icon = Icons.AutoMirrored.Filled.CallMissed
            color = MaterialTheme.colorScheme.error
        }

        CallType.UNKNOWN -> {
            icon = Icons.AutoMirrored.Filled.HelpOutline
            color = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
    Icon(imageVector = icon, contentDescription = type.name, tint = color)
}
