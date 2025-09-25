package com.example.myphone.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.myphone.features.contacts.data.ContactInfo
import com.example.myphone.features.recents.ui.CallHistoryViewModel
import com.example.myphone.ui.components.CallLogList
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

    Scaffold(
        topBar = {
            CallHistoryTopBar(
                contactInfo = uiState.contactInfo,
                phoneNumber = uiState.phoneNumber,
                onNavigateBack = { navController.navigateUp() }
            )
        },
        bottomBar = {
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
                    CallLogList(
                        navController = navController,
                        callLog = uiState.callHistory,
                        onCall = { number, isVideo -> placeCall(isVideo) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallHistoryTopBar(
    contactInfo: ContactInfo?,
    phoneNumber: String,
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ContactAvatar(
                    name = contactInfo?.name ?: phoneNumber,
                    photoUri = contactInfo?.photoUri,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
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
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Implement more options */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
        }
    )
}

