package com.example.myphone.features.recents.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myphone.features.recents.data.CallLogEntry
import com.example.myphone.features.recents.data.RecentsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallHistoryViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // The Success state now includes the identifier for the title and the number to call.
    data class CallHistoryUiState(
        val isLoading: Boolean = true,
        val callHistory: List<CallLogEntry> = emptyList(),
        val contactIdentifier: String = "",
        val numberToCall: String = "",
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(CallHistoryUiState())
    val uiState: StateFlow<CallHistoryUiState> = _uiState.asStateFlow()

    private val repository = RecentsRepository(application.contentResolver)
    private val phoneNumber: String? = savedStateHandle["phoneNumber"]

    init {
        fetchHistory()
    }

    private fun fetchHistory() {
        if (phoneNumber == null) {
            _uiState.update { it.copy(isLoading = false, error = "Phone number not found.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val history = repository.getCallHistoryForNumber(phoneNumber)
                val firstEntry = history.firstOrNull()
                // Determine the best identifier for the screen title.
                val identifier = when {
                    firstEntry != null && firstEntry.name != "Unknown" -> firstEntry.name
                    phoneNumber.isNotBlank() -> phoneNumber
                    else -> "Unknown"
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        callHistory = history,
                        contactIdentifier = identifier,
                        numberToCall = phoneNumber
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load history.") }
            }
        }
    }
}

