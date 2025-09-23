package com.example.myphone.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.myphone.features.contacts.ui.ContactDetailsViewModel
import com.example.myphone.navigation.Screen
import com.example.myphone.ui.components.ContactAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailsScreen(
    navController: NavController,
    contactDetailsViewModel: ContactDetailsViewModel = viewModel(),
) {
    val uiState by contactDetailsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // This is the definitive fix. This LaunchedEffect is keyed to the unique
    // navigation instance. It is GUARANTEED to run every time you navigate
    // to this screen, telling the ViewModel to fetch fresh data.
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(key1 = currentBackStackEntry) {
        contactDetailsViewModel.loadContactDetails()
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
                    // UPDATED: Check if the UI state is Success to safely access the contactId.
                    if (uiState is ContactDetailsViewModel.ContactDetailsUiState.Success) {
                        // Extract the contactId from the success state.
                        val contactId = (uiState as ContactDetailsViewModel.ContactDetailsUiState.Success).contactDetails.id
                        IconButton(onClick = {
                            navController.navigate(Screen.EditContact.createRoute(contactId))
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Contact")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is ContactDetailsViewModel.ContactDetailsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ContactDetailsViewModel.ContactDetailsUiState.Success -> {
                    val details = state.contactDetails
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        // Contact Photo and Name
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            // UPDATED: Replaced AsyncImage with our new smart ContactAvatar component.
                            ContactAvatar(
                                name = details.name,
                                photoUri = details.photoUri,
                                modifier = Modifier.size(120.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = details.name, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(32.dp))

                        }

                        // Phone Numbers
                        items(details.phoneNumbers) { number ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = number,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        if (hasCallPermission) {
                                            val intent =
                                                Intent(Intent.ACTION_CALL, "tel:$number".toUri())
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
                }
                is ContactDetailsViewModel.ContactDetailsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Failed to load contact details.")
                    }
                }
            }
        }
    }
}

