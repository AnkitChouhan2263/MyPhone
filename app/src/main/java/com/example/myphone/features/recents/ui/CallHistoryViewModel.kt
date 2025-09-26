package com.example.myphone.features.recents.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myphone.features.contacts.data.ContactInfo // Correctly import the new data class
import com.example.myphone.features.contacts.data.ContactsRepository
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

    data class CallHistoryUiState(
        val isLoading: Boolean = true,
        val callHistory: List<CallLogEntry> = emptyList(),
        val contactInfo: ContactInfo? = null,
        val phoneNumber: String = "",
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(CallHistoryUiState())
    val uiState: StateFlow<CallHistoryUiState> = _uiState.asStateFlow()

    private val recentsRepository = RecentsRepository(application.contentResolver)
    private val contactsRepository = ContactsRepository(application.contentResolver)
    private val phoneNumber: String? = savedStateHandle["phoneNumber"]

    init {
        fetchHistory()
    }

    private fun fetchHistory() {
        if (phoneNumber.isNullOrBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Phone number not found.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, phoneNumber = phoneNumber) }
            try {
                val history = recentsRepository.getCallHistoryForNumber(phoneNumber)
                // This call will now resolve correctly.
                val contactInfo = contactsRepository.getContactInfoForNumber(phoneNumber)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        callHistory = history,
                        contactInfo = contactInfo
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load history.") }
            }
        }
    }
}

