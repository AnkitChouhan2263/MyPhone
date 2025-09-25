package com.example.myphone.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.navigation.NavController
import com.example.myphone.features.recents.data.CallLogEntry
import com.example.myphone.features.recents.data.CallType
import com.example.myphone.features.recents.ui.RecentsViewModel
import com.example.myphone.navigation.Screen
import com.example.myphone.ui.components.ContactAvatar
import com.example.myphone.ui.components.EmptyState
import java.util.Calendar

@Composable
fun RecentsScreen(
    navController: NavController,
    recentsViewModel: RecentsViewModel = viewModel(),
) {
    val context = LocalContext.current

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
                        navController = navController,
                        callLog = state.callLog,
                        onCall = { number, isVideo ->
                            if (hasCallPermission) {
                                val intentAction = if (isVideo) Intent.ACTION_VIEW else Intent.ACTION_CALL
                                val intent = Intent(intentAction, "tel:$number".toUri())
                                if (isVideo) {
                                    intent.putExtra("videocall", true)
                                }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallLogList(
    navController: NavController,
    callLog: List<CallLogEntry>,
    onCall: (String, Boolean) -> Unit
) {
    var expandedItemId by remember { mutableStateOf<String?>(null) }
    val groupedByDate = callLog.groupBy { getDateSection(it.dateMillis) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        groupedByDate.forEach { (section, entries) ->
            stickyHeader {
                SectionHeader(title = section.title)
            }
            // THE FIX: Iterate over the correct 'entries' for this section, not the whole 'callLog'.
            items(entries, key = { it.id }) { entry ->
                // The animation is now on a simple container Box, which is more stable.
                Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null, placementSpec = tween(300))) {
                    CallLogItem(
                        entry = entry,
                        isExpanded = expandedItemId == entry.id,
                        onToggleExpand = {
                            expandedItemId = if (expandedItemId == entry.id) null else entry.id
                        },
                        onCall = onCall,
                        onNavigateToDetails = { contactId ->
                            navController.navigate(Screen.ContactDetails.createRoute(contactId))
                        },
                        onNavigateToHistory = { number ->
                            navController.navigate(Screen.CallHistory.createRoute(number))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CallLogItem(
    entry: CallLogEntry,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCall: (String, Boolean) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToHistory: (String) -> Unit
) {
    // The entire item, including the divider, is now one self-contained unit.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.clickable(enabled = entry.contactId != null) {
                entry.contactId?.let { onNavigateToDetails(it) }
            }) {
                val avatarName = if (entry.name != "Unknown") entry.name else ""
                ContactAvatar(
                    name = avatarName,
                    photoUri = entry.photoUri,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val primaryText = when {
                    entry.name != "Unknown" -> entry.name
                    entry.number.isNotBlank() -> entry.number
                    else -> "Unknown Number"
                }
                Text(
                    text = primaryText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (entry.type == CallType.MISSED || entry.type == CallType.REJECTED) FontWeight.Bold else FontWeight.Normal,
                    color = if (entry.type == CallType.MISSED || entry.type == CallType.REJECTED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
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
            IconButton(onClick = { onCall(entry.number, false) }) {
                Icon(Icons.Default.Call, contentDescription = "Call back")
            }
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, start = 64.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = { onNavigateToHistory(entry.number) }) {
                    Icon(
                        Icons.Default.History, modifier = Modifier.size(ButtonDefaults.IconSize),
                        contentDescription = "Call History"
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("History")
                }
                Button(onClick = { onCall(entry.number, true) }) {
                    Icon(
                        Icons.Default.Videocam, modifier = Modifier.size(ButtonDefaults.IconSize),
                        contentDescription = "Video Call"
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Video Call")
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
fun CallTypeIcon(type: CallType) {
    val icon: ImageVector
    val color: Color
    when (type) {
        CallType.INCOMING -> {
            icon = Icons.AutoMirrored.Filled.CallReceived
            color = Color(0xFF388E3C)
        }
        CallType.OUTGOING -> {
            icon = Icons.AutoMirrored.Filled.CallMade
            color = Color(0xFF1976D2)
        }
        CallType.MISSED -> {
            icon = Icons.AutoMirrored.Filled.CallMissed
            color = MaterialTheme.colorScheme.error
        }
        CallType.REJECTED -> {
            icon = Icons.Default.CallEnd
            color = MaterialTheme.colorScheme.error
        }
        CallType.BLOCKED -> {
            icon = Icons.Default.Block
            color = MaterialTheme.colorScheme.onSurfaceVariant
        }
        CallType.VOICEMAIL -> {
            icon = Icons.Default.Voicemail
            color = Color(0xFF8E44AD)
        }
        CallType.ANSWERED_EXTERNALLY -> {
            icon = Icons.Default.PhoneInTalk
            color = Color(0xFF00838F)
        }
        CallType.UNKNOWN -> {
            icon = Icons.AutoMirrored.Filled.HelpOutline
            color = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
    Icon(imageVector = icon, contentDescription = type.name, tint = color, modifier = Modifier.size(14.dp))
}

/**
 * A sealed class to represent the date-based sections for the call log.
 */
private sealed class DateSection(val title: String) {
    object Today : DateSection("Today")
    object Yesterday : DateSection("Yesterday")
    object Older : DateSection("Older")
}

/**
 * A helper function to determine which date section a call log entry belongs to.
 */
private fun getDateSection(timestamp: Long): DateSection {
    val now = Calendar.getInstance()
    val callDate = Calendar.getInstance().apply { timeInMillis = timestamp }

    if (now.get(Calendar.YEAR) == callDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == callDate.get(Calendar.DAY_OF_YEAR)) {
        return DateSection.Today
    }

    now.add(Calendar.DAY_OF_YEAR, -1) // Set calendar to yesterday
    if (now.get(Calendar.YEAR) == callDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == callDate.get(Calendar.DAY_OF_YEAR)) {
        return DateSection.Yesterday
    }

    return DateSection.Older
}

