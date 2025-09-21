package com.example.myphone.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myphone.features.dialer.ui.DialerAction
import com.example.myphone.features.dialer.ui.DialerViewModel

// A map to associate each digit with its corresponding letters.
private val keypadLetters = mapOf(
    '1' to " ", '2' to "ABC", '3' to "DEF",
    '4' to "GHI", '5' to "JKL", '6' to "MNO",
    '7' to "PQRS", '8' to "TUV", '9' to "WXYZ",
    '*' to "", '0' to "+", '#' to ""
)

@Preview
@Composable
fun DialerScreen(dialerViewModel: DialerViewModel = viewModel()) {
    val uiState by dialerViewModel.uiState.collectAsState()
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
        onResult = { isGranted ->
            hasCallPermission = isGranted
        }
    )

    val keypadButtons = listOf(
        '1', '2', '3',
        '4', '5', '6',
        '7', '8', '9',
        '*', '0', '#'
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // This spacer pushes the dialer content towards the bottom,
        // making the layout adaptive to different screen heights.
        Spacer(modifier = Modifier.weight(1f))

        // Display for the entered number
        Text(
            text = uiState.enteredNumber.ifEmpty { "Enter number" },
            style = MaterialTheme.typography.displayMedium,
            color = if (uiState.enteredNumber.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            maxLines = 1,
            // Simple auto-sizing logic
            fontSize = if (uiState.enteredNumber.length > 10) 36.sp else 40.sp
        )

        // Keypad Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(horizontal = 24.dp),
            userScrollEnabled = false
        ) {
            items(keypadButtons) { button ->
                KeypadButton(
                    char = button,
                    letters = keypadLetters[button] ?: "",
                    onClick = { dialerViewModel.onAction(DialerAction.NumberPressed(button)) }
                )
            }
        }

        // Action buttons row (Call, Delete)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer for layout balance, making it a "ghost" button
            Spacer(modifier = Modifier.size(72.dp))

            // Call Button
            FloatingActionButton(
                onClick = {
                    if (uiState.enteredNumber.isNotEmpty()) {
                        if (hasCallPermission) {
                            val intent = Intent(
                                Intent.ACTION_CALL,
                                "tel:${uiState.enteredNumber}".toUri()
                            )
                            context.startActivity(intent)
                        } else {
                            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                        }
                    }
                },
                shape = CircleShape,
                modifier = Modifier.size(72.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onPrimary)
            }

            // Delete Button
            IconButton(
                onClick = { dialerViewModel.onAction(DialerAction.Delete) },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun KeypadButton(
    char: Char,
    letters: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = char.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Only show letters if they exist for that key
            if (letters.isNotBlank()) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

