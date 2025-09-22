package com.example.myphone.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myphone.R
import com.example.myphone.features.contacts.data.ContactsViewModel
import com.example.myphone.navigation.Screen

@Composable
fun ContactsScreen(
    navController: NavController,
    contactsViewModel: ContactsViewModel = viewModel()
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
        }
    )

    LaunchedEffect(key1 = hasPermission) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            contactsViewModel.fetchContacts()
        }
    }

    val uiState by contactsViewModel.uiState.collectAsState()
    val searchQuery by contactsViewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { contactsViewModel.onSearchQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search contacts & numbers") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { contactsViewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Search")
                    }
                }
            },
            singleLine = true
        )

        if (hasPermission) {
            when (val state = uiState) {
                is ContactsViewModel.ContactsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ContactsViewModel.ContactsUiState.Success -> {
                    ContactsList(
                        results = state.results,
                        onContactClick = { contactId ->
                            navController.navigate(Screen.ContactDetails.createRoute(contactId))
                        }
                    )
                }
                is ContactsViewModel.ContactsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Failed to load contacts.")
                    }
                }
            }
        } else {
            PermissionDeniedContent {
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }
}

@Composable
fun ContactsList(
    results: List<ContactsViewModel.ContactSearchResult>,
    onContactClick: (String) -> Unit
) {
    LazyColumn {
        items(results, key = { it.contact.id }) { result ->
            ContactListItem(
                result = result,
                onClick = { onContactClick(result.contact.id) }
            )
        }
    }
}

@Composable
fun ContactListItem(
    result: ContactsViewModel.ContactSearchResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(result.contact.photoUri)
                .error(R.drawable.ic_launcher_foreground)
                .crossfade(true)
                .build(),
            contentDescription = "${result.contact.name}'s photo",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(text = buildHighlightedText(result.contact.name, result.matchedQuery))
            // UPDATED: Display the first number from the list for a clean UI.
            result.contact.numbers.firstOrNull()?.let { firstNumber ->
                Text(
                    text = buildHighlightedText(firstNumber, result.matchedQuery),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun buildHighlightedText(fullText: String, query: String): AnnotatedString {
    return buildAnnotatedString {
        append(fullText)
        if (query.isNotBlank()) {
            val startIndex = fullText.indexOf(query, ignoreCase = true)
            if (startIndex >= 0) {
                val endIndex = startIndex + query.length
                addStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    start = startIndex,
                    end = endIndex
                )
            }
        }
    }
}

@Composable
fun PermissionDeniedContent(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Text("Permission needed to show contacts.", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }
    }
}

