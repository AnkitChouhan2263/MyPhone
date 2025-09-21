package com.example.myphone.features.contacts.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myphone.features.contacts.data.ContactDetails
import com.example.myphone.features.contacts.data.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactDetailsViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // UI state definition for the details screen
    sealed interface ContactDetailsUiState {
        object Loading : ContactDetailsUiState
        data class Success(val contactDetails: ContactDetails) : ContactDetailsUiState
        object Error : ContactDetailsUiState
    }

    private val _uiState = MutableStateFlow<ContactDetailsUiState>(ContactDetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val repository = ContactsRepository(application.contentResolver)

    // The init block is called when the ViewModel is first created.
    init {
        // Safely get the contactId from the navigation arguments.
        val contactId: String? = savedStateHandle.get<String>("contactId")
        // Only fetch details if the ID is not null or blank.
        if (!contactId.isNullOrBlank()) {
            fetchContactDetails(contactId)
        } else {
            // If the ID is missing, go directly to the error state.
            _uiState.value = ContactDetailsUiState.Error
        }
    }

    private fun fetchContactDetails(contactId: String) {
        _uiState.value = ContactDetailsUiState.Loading
        viewModelScope.launch {
            try {
                // Fetch details for the specific contact ID
                val details = repository.getContactDetails(contactId)
                _uiState.value = ContactDetailsUiState.Success(details)
            } catch (_: Exception) {
                _uiState.value = ContactDetailsUiState.Error
            }
        }
    }
}
