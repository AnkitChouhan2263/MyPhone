package com.example.myphone.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.myphone.features.contacts.data.ContactInfo
import com.example.myphone.features.recents.data.CallLogEntry
import com.example.myphone.features.recents.ui.CallHistoryViewModel
import com.example.myphone.features.settings.data.AvatarStyle
import com.example.myphone.features.settings.ui.SettingsViewModel
import com.example.myphone.ui.components.CallTypeIcon
import com.example.myphone.ui.components.ContactAvatar
import com.example.myphone.ui.components.EmptyState
import com.example.myphone.ui.components.getDateSection
import java.util.Locale

@Composable
fun CallHistoryScreen(
    navController: NavController,
    viewModel: CallHistoryViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel() // Get settings
) {
    val uiState by viewModel.uiState.collectAsState()
    val avatarStyle by settingsViewModel.avatarStyle.collectAsState() // Observe style
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
        if (uiState.phoneNumber.isBlank()) return

        if (hasCallPermission) {
            val intentAction = if (isVideo) Intent.ACTION_VIEW else Intent.ACTION_CALL
            val intent = Intent(intentAction, "tel:${uiState.phoneNumber}".toUri())
            if (isVideo) intent.putExtra("videocall", true)
            context.startActivity(intent)
        } else {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                EmptyState(
                    title = "Error",
                    message = "Could not load call history.",
                    icon = Icons.Default.Warning
                )
            }
            else -> {
                // Your custom Top Bar UI
                CallHistoryTopBar(
                    contactInfo = uiState.contactInfo,
                    phoneNumber = uiState.phoneNumber,
                    avatarStyle = avatarStyle, // Pass style down
                    onNavigateBack = { navController.navigateUp() }
                )

                // The redesigned CallLogList now takes up the remaining space.
                CallHistoryList(
                    entries = uiState.callHistory,
                    modifier = Modifier.weight(1f)
                )

                // Your custom Bottom Bar UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { placeCall(false) }) {
                        Icon(Icons.Default.Call, contentDescription = "Call")
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Call")
                    }
                    Button(onClick = { placeCall(true) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Video Call")
                    }
                }
            }
        }
    }
}

@Composable
private fun CallHistoryTopBar(
    contactInfo: ContactInfo?,
    phoneNumber: String,
    avatarStyle: AvatarStyle,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        ContactAvatar(
            name = contactInfo?.name ?: phoneNumber,
            photoUri = contactInfo?.photoUri,
            avatarStyle = avatarStyle, // Pass to avatar
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contactInfo?.name ?: phoneNumber,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (contactInfo != null) {
                Text(
                    text = phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = { /* TODO: Implement more options */ }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CallHistoryList(entries: List<CallLogEntry>, modifier: Modifier = Modifier) {
    val groupedByDate = entries.groupBy { getDateSection(it.dateMillis) }

    LazyColumn(modifier = modifier) {
        groupedByDate.forEach { (section, sectionEntries) ->

            stickyHeader {
                SectionHeader(title = section.title)
            }
            items(sectionEntries, key = { it.id }) { entry ->
                CallHistoryListItem(entry = entry)
//                HorizontalDivider(
//                    modifier = Modifier.padding(horizontal = 16.dp),
//                    thickness = DividerDefaults.Thickness,
//                    color = DividerDefaults.color
//                )
            }
        }
    }
}

@Composable
private fun CallHistoryListItem(entry: CallLogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CallTypeIcon(type = entry.type, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.type.name.lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = entry.formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatDuration(entry.duration),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return ""
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}

