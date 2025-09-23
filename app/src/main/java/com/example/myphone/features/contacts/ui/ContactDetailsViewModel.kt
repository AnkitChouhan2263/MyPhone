package com.example.myphone.features.contacts.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myphone.features.contacts.data.ContactDetails
import com.example.myphone.features.contacts.data.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactDetailsViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    sealed interface ContactDetailsUiState {
        object Loading : ContactDetailsUiState
        data class Success(val contactDetails: ContactDetails) : ContactDetailsUiState
        object Error : ContactDetailsUiState
    }

    private val _uiState = MutableStateFlow<ContactDetailsUiState>(ContactDetailsUiState.Loading)
    val uiState: StateFlow<ContactDetailsUiState> = _uiState

    private val repository = ContactsRepository(application.contentResolver)

    // The init block is now empty. The UI layer is now in control of when to load data.
    init {}

    /**
     * Public function that can be called by the UI to load or reload the contact's details.
     * This is the key to ensuring the data is always fresh.
     */
    fun loadContactDetails() {
        val contactId: String? = savedStateHandle.get("contactId")
        if (contactId == null) {
            _uiState.value = ContactDetailsUiState.Error
            return
        }

        viewModelScope.launch {
            _uiState.value = ContactDetailsUiState.Loading
            try {
                val details = repository.getContactDetails(contactId)
                _uiState.value = ContactDetailsUiState.Success(details)
            } catch (_: Exception) {
                _uiState.value = ContactDetailsUiState.Error
            }
        }
    }
}

