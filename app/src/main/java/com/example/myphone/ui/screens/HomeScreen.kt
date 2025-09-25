package com.example.myphone.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myphone.features.recents.data.CallType
import com.example.myphone.features.recents.ui.RecentsViewModel
import com.example.myphone.ui.components.CallLogList

enum class HomeFilter(val label: String) {
    ALL("All"),
    MISSED("Missed")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    recentsViewModel: RecentsViewModel = viewModel(),
) {
    val recentsState by recentsViewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(HomeFilter.ALL) }
    val context = LocalContext.current

    var hasReadPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val readPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasReadPermission = isGranted }
    )
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

    LaunchedEffect(Unit) {
        if (hasReadPermission) {
            recentsViewModel.fetchRecents()
        } else {
            readPermissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
//                .padding(padding)
        ) {
//            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(
                modifier = Modifier.padding(horizontal = 16.dp)
            )
//            Spacer(modifier = Modifier.height(16.dp))
            Filter(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                selected = selectedFilter,
                onItemClick = { selectedFilter = it },
                options = HomeFilter.entries
            )
//            Spacer(modifier = Modifier.height(8.dp))

            if (hasReadPermission) {
                when (val state = recentsState) {
                    is RecentsViewModel.RecentsUiState.Success -> {
                        val filteredList = when (selectedFilter) {
                            HomeFilter.ALL -> state.callLog
                            HomeFilter.MISSED -> state.callLog.filter {
                                it.type == CallType.MISSED || it.type == CallType.REJECTED
                            }
                        }

                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 100.dp)
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No calls found for this filter.")
                            }
                        } else {
                            CallLogList(
                                navController = navController,
                                callLog = filteredList,
                                onCall = { number, isVideo ->
                                    if (hasCallPermission) {
                                        val intentAction = if (isVideo) Intent.ACTION_VIEW else Intent.ACTION_CALL
                                        val intent = Intent(intentAction, "tel:$number".toUri())
                                        if (isVideo) {
                                            intent.putExtra("videocall", true)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                                    }
                                }
                            )
                        }
                    }
                    is RecentsViewModel.RecentsUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is RecentsViewModel.RecentsUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .padding(top = 100.dp)
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Failed to load recent calls.")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(16.dp).weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Permission is required to see recent calls.")
                    Button(onClick = { readPermissionLauncher.launch(Manifest.permission.READ_CALL_LOG) }) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}

// These composables are specific to the HomeScreen's UI, so they remain here.
/**
 * THE FIX: This is the missing SearchBar composable.
 * It is now correctly declared within the HomeScreen.kt file.
 */
@Composable
private fun SearchBar(modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = modifier.fillMaxWidth(),
        readOnly = true,
        enabled = false, // Display only, not functional yet
        placeholder = { Text("Search contacts & places") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        shape = RoundedCornerShape(50),
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is PressInteraction.Release) {
                            // TODO: Navigate to a dedicated search screen in a future step
                        }
                    }
                }
            }
    )
}

@Composable
private fun Filter(
    modifier: Modifier = Modifier,
    selected: HomeFilter,
    options: List<HomeFilter>,
    onItemClick: (HomeFilter) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(state = rememberScrollState())
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onItemClick(option) },
                label = { Text(option.label) }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}


//@Composable
//fun FavouriteContacts(
//    isFavouriteExpanded: Boolean,
//    onDropDown: () -> Unit,
//) {
//
//    Row(
//        modifier = Modifier.fillMaxWidth(.8f),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ){
//        Row (
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Start,
//            modifier = Modifier.clickable(
//                onClick = onDropDown
//            )
//        ){
//            Text(
//                text = "Favourites",
//                color = Color.LightGray
//            )
//            Icon(
//                imageVector = if (isFavouriteExpanded){Icons.Default.KeyboardArrowUp}else Icons.Default.KeyboardArrowDown,
//                contentDescription = "DropDownIcon",
//                tint = Color.LightGray,
//            )
//        }
//
//        Surface(
//            onClick = {},
//            shape = RoundedCornerShape(24.dp),
//            color = DarkSecondaryGray,
//        ) {
//            Text(
//                text = "View contacts",
//                color = Color.LightGray,
//                modifier = Modifier.padding(8.dp).padding(horizontal = 8.dp)
//
//            )
//        }
//    }
//
//}