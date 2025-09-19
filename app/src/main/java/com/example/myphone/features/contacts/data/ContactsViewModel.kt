package com.example.myphone.features.contacts.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myphone.features.contacts.data.Contact
import com.example.myphone.features.contacts.data.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Our ViewModel needs the application context to get the ContentResolver,
// so we inherit from AndroidViewModel.
class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContactsRepository(application.contentResolver)

    // This sealed interface represents the different states our UI can be in.
    sealed interface ContactsUiState {
        object Loading : ContactsUiState
        data class Success(val contacts: List<Contact>) : ContactsUiState
        object Error : ContactsUiState
    }

    // A private MutableStateFlow that will hold the current UI state.
    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    // A public, read-only StateFlow that the UI can observe.
    val uiState = _uiState.asStateFlow()

    // This function triggers the fetching of contacts.
    fun fetchContacts() {
        // We set the state to Loading before we start.
        _uiState.value = ContactsUiState.Loading
        // We launch a coroutine in the viewModelScope. This scope is automatically
        // cancelled when the ViewModel is cleared, preventing memory leaks.
        viewModelScope.launch {
            try {
                val contacts = repository.getContacts()
                _uiState.value = ContactsUiState.Success(contacts)
            } catch (e: Exception) {
                // If anything goes wrong, we set the state to Error.
                _uiState.value = ContactsUiState.Error
            }
        }
    }
}
