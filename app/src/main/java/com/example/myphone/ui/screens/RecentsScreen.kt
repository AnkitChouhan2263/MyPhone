package com.example.myphone.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myphone.features.recents.data.CallLogEntry
import com.example.myphone.features.recents.data.CallType
import com.example.myphone.features.recents.ui.RecentsViewModel
import androidx.core.net.toUri

@Composable
fun RecentsScreen(recentsViewModel: RecentsViewModel = viewModel()) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
        }
    )

    // Request permission when the screen is first composed
    LaunchedEffect(key1 = Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
        }
    }

    if (hasPermission) {
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
                CallLogList(callLog = state.callLog)
            }
            is RecentsViewModel.RecentsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Failed to load call log.")
                }
            }
        }
    } else {
        // Show a UI to explain why the permission is needed
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Permission needed to show call log.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_CALL_LOG) }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun CallLogList(callLog: List<CallLogEntry>) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(callLog) { entry ->
            CallLogItem(entry = entry, onCall = { number ->
                // No need to request permission here as we can't get to this screen without it.
                val intent = Intent(Intent.ACTION_CALL, "tel:$number".toUri())
                context.startActivity(intent)
            })
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
