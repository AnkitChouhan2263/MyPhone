package com.example.myphone.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myphone.features.contacts.data.ContactDetails
import com.example.myphone.features.contacts.ui.ContactDetailsViewModel
import com.example.myphone.features.contacts.ui.DetailsAction
import com.example.myphone.features.settings.ui.SettingsViewModel
import com.example.myphone.navigation.Screen
import com.example.myphone.ui.components.ContactAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailsScreen(
    navController: NavController,
    contactDetailsViewModel: ContactDetailsViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel() // Get settings
) {
    val uiState by contactDetailsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val avatarStyle by settingsViewModel.avatarStyle.collectAsState() // Observe style

    // This is the definitive fix. This LaunchedEffect is keyed to the unique
    // navigation instance. It is GUARANTEED to run every time you navigate
    // to this screen, telling the ViewModel to fetch fresh data.
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(key1 = currentBackStackEntry) {
        contactDetailsViewModel.loadContactDetails()
    }

    // UPDATED: This effect now also listens for the needsRefresh signal.
    LaunchedEffect(key1 = uiState.contactDeleted, key2 = uiState.needsRefresh) {
        if (uiState.contactDeleted || uiState.needsRefresh) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("should_refresh_contacts", true)
            if (uiState.contactDeleted) {
                navController.navigateUp()
            }
        }
    }

    // This state now holds the last successful data to prevent blinking.
    var lastSuccessDetails by remember { mutableStateOf<ContactDetails?>(null) }
    if (uiState.contactDetails != null) {
        lastSuccessDetails = uiState.contactDetails
    }

    // --- Start of Call Permission Logic ---
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
    // --- End of Call Permission Logic ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // UPDATED: Added an Edit button to the actions.
                actions = {
                    if (uiState.contactDetails != null) {
                        // Star (Favorite) Button
                        IconButton(onClick = { contactDetailsViewModel.onAction(DetailsAction.ToggleFavorite) }) {
                            Icon(
                                imageVector = if (uiState.contactDetails!!.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Toggle Favorite",
                                tint = if (uiState.contactDetails!!.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Edit button
                        IconButton(onClick = {
                            navController.navigate(Screen.EditContact.createRoute(uiState.contactDetails!!.id))
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Contact")
                        }
                        // Dropdown menu for Delete
                        TopBarDropdownMenu(onDelete = {
                            contactDetailsViewModel.onAction(DetailsAction.ShowDeleteDialog)
                        })
                    }
                }
            )
        }
    ) { padding ->

        // Show confirmation dialog if needed
        if (uiState.showDeleteConfirmDialog) {
            DeleteConfirmationDialog(
                onConfirm = { contactDetailsViewModel.onAction(DetailsAction.ConfirmDelete) },
                onDismiss = { contactDetailsViewModel.onAction(DetailsAction.HideDeleteDialog) }
            )
        }

        // Use the cached data if available, otherwise check the current state.
        val detailsToShow = lastSuccessDetails

        Box(modifier = Modifier.padding(padding)) {



            if (detailsToShow != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        ContactAvatar(
                            name = detailsToShow.name,
                            photoUri = detailsToShow.photoUri,
                            avatarStyle = avatarStyle, // Pass to avatar
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = detailsToShow.name, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    items(detailsToShow.phoneNumbers) { number ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(number, style = MaterialTheme.typography.bodyLarge)
                                IconButton(onClick = {
                                    if (hasCallPermission) {
                                        val intent = Intent(Intent.ACTION_CALL,
                                            "tel:$number".toUri())
                                        context.startActivity(intent)
                                    } else {
                                        callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                                    }
                                }) {
                                    Icon(Icons.Default.Call, contentDescription = "Call")
                                }
                            }
                        }
                    }
                }
            } else if (uiState.isLoading) {
                // Only show full-screen spinner on initial load.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error!!)
                }
            }
        }
    }
}

@Composable
fun TopBarDropdownMenu(onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    expanded = false
                    onDelete()
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete") }
            )
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Contact") },
        text = { Text("Are you sure you want to permanently delete this contact?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



