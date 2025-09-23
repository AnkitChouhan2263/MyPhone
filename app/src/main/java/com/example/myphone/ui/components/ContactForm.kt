package com.example.myphone.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myphone.features.contacts.ui.AddEditContactAction
import com.example.myphone.features.contacts.ui.AddEditContactUiState

@Composable
fun ContactForm(
    uiState: AddEditContactUiState,
    onAction: (AddEditContactAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Contact Photo",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = uiState.firstName,
            onValueChange = { onAction(AddEditContactAction.UpdateFirstName(it)) },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.lastName,
            onValueChange = { onAction(AddEditContactAction.UpdateLastName(it)) },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = uiState.phone,
            onValueChange = { newValue ->
                // Filter the input to allow only digits and a '+' at the start.
                val filtered = newValue.filterIndexed { index, char ->
                    char.isDigit() || (index == 0 && char == '+')
                }
                onAction(AddEditContactAction.UpdatePhone(filtered))
            },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth(),
            // Set the keyboard to a phone-specific layout.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onAction(AddEditContactAction.UpdateEmail(it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
