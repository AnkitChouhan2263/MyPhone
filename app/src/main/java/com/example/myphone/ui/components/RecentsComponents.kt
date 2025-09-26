package com.example.myphone.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myphone.features.recents.data.CallLogEntry
import com.example.myphone.features.recents.data.CallType
import com.example.myphone.navigation.Screen
import java.util.Calendar
import java.util.Locale

/**
 * A reusable, self-contained composable that displays a list of call log entries
 * with fully animated items and sticky headers.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallLogList(
    navController: NavController,
    callLog: List<CallLogEntry>,
    onCall: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedItemId by remember { mutableStateOf<String?>(null) }
    val groupedByDate = callLog.groupBy { getDateSection(it.dateMillis) }

    LazyColumn(modifier = modifier) {
        groupedByDate.forEach { (section, entries) ->
            // THE FIX: The stickyHeader itself is now an animated item.
            stickyHeader {
                Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null, placementSpec = tween(300))) {
                    SectionHeader(title = section.title)
                }
            }
            items(entries, key = { it.id }) { entry ->
                // The animation is applied to the self-contained CallLogItem.
                CallLogItem(
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null, placementSpec = tween(300)),
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

@Composable
private fun SectionHeader(title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
    ) {
        Text(
            text = title.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CallLogItem(
    modifier: Modifier = Modifier,
    entry: CallLogEntry,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCall: (String, Boolean) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToHistory: (String) -> Unit
) {
    // THE FIX: The item is now a self-contained unit, including its own divider.
    Column(
        modifier = modifier
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
                    CallTypeIcon(type = entry.type, modifier = Modifier.size(14.dp))
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
                    Icon(Icons.Default.History, contentDescription = "Call History")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("History")
                }
                Button(onClick = { onCall(entry.number, true) }) {
                    Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Video Call")
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
fun CallTypeIcon(type: CallType, modifier: Modifier = Modifier) {
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
    Icon(imageVector = icon, contentDescription = type.name, tint = color, modifier = modifier)
}

sealed class DateSection(val title: String) {
    object Today : DateSection("Today")
    object Yesterday : DateSection("Yesterday")
    object Older : DateSection("Older")
}

fun getDateSection(timestamp: Long): DateSection {
    val now = Calendar.getInstance()
    val callDate = Calendar.getInstance().apply { timeInMillis = timestamp }

    if (now.get(Calendar.YEAR) == callDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == callDate.get(Calendar.DAY_OF_YEAR)
    ) {
        return DateSection.Today
    }

    now.add(Calendar.DAY_OF_YEAR, -1)
    if (now.get(Calendar.YEAR) == callDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == callDate.get(Calendar.DAY_OF_YEAR)
    ) {
        return DateSection.Yesterday
    }

    return DateSection.Older
}

