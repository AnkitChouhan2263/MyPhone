package com.example.myphone.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myphone.R
import com.example.myphone.features.contacts.ui.ContactDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailsScreen(
    navController: NavController,
    contactDetailsViewModel: ContactDetailsViewModel = viewModel(),
) {
    val uiState by contactDetailsViewModel.uiState.collectAsState()
    val context = LocalContext.current

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
                            // If the photoUri is not null, display the image from the URI
                            if (details.photoUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(details.photoUri)
                                        // Use a safer vector drawable resource
                                        .error(R.drawable.ic_launcher_foreground)
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "${details.name}'s photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                // If the photoUri is null, display a generic person icon
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "${details.name}'s photo",
                                        modifier = Modifier.size(72.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = details.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(24.dp))
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

