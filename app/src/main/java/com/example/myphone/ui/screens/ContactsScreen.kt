package com.example.myphone.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.myphone.features.contacts.data.ContactsViewModel
import com.example.myphone.navigation.Screen
import com.example.myphone.ui.components.ContactAvatar
import com.example.myphone.ui.components.EmptyState

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
                    if (state.results.isNotEmpty()) {
                        ContactsList(
                            results = state.results,
                            onContactClick = { contactId ->
                                navController.navigate(Screen.ContactDetails.createRoute(contactId))
                            }
                        )
                    } else {
                        // Handle both empty list and no search results scenarios
                        if (searchQuery.isNotBlank()) {
                            EmptyState(
                                title = "No results found",
                                message = "Try a different name or number.",
                                icon = Icons.Default.SearchOff
                            )
                        } else {
                            EmptyState(
                                title = "No contacts",
                                message = "Your contact list is empty.",
                                icon = Icons.Default.People
                            )
                        }
                    }
                }
                is ContactsViewModel.ContactsUiState.Error -> {
                    EmptyState(
                        title = "Error",
                        message = "Failed to load your contacts. Please try again later.",
                        icon = Icons.Default.Warning
                    )
                }
            }
        } else {
            // UPDATED: Replaced PermissionDeniedContent with the reusable EmptyState
            EmptyState(
                title = "Permission needed",
                message = "This app needs to read your contacts to display them.",
                icon = Icons.Default.People,
                actionText = "Grant Permission",
                onAction = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }
            )
        }
    }
}

@Composable
fun ContactsList(
    results: List<ContactsViewModel.ContactSearchResult>,
    onContactClick: (String) -> Unit
) {
    LazyColumn {
        // UPDATED: The key is now a combination of contact ID and the matched number,
        // ensuring it's unique even when the same contact appears multiple times.
        items(results, key = { "${it.contact.id}-${it.matchedNumber}" }) { result ->
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
        // UPDATED: Replaced AsyncImage with our new smart ContactAvatar component.
        ContactAvatar(
            name = result.contact.name,
            photoUri = result.contact.photoUri,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))

        Column {
            val nameHighlightQuery = if (result.matchedNumber == null) result.matchedQuery else ""
            Text(text = buildHighlightedText(result.contact.name, nameHighlightQuery))
            val numberToShow = result.matchedNumber ?: result.contact.numbers.firstOrNull()

            numberToShow?.let { number ->
                val numberHighlightQuery = if (result.matchedNumber != null) result.matchedQuery else ""
                Text(
                    text = buildHighlightedText(number, numberHighlightQuery),
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

