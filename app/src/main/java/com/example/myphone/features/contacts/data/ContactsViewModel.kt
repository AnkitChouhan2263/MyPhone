package com.example.myphone.features.contacts.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    // Sealed interface for the main UI state (Loading, Error, Success).
    sealed interface ContactsUiState {
        object Loading : ContactsUiState
        data class Success(val contacts: List<Contact>) : ContactsUiState
        object Error : ContactsUiState
    }

    private val repository = ContactsRepository(application.contentResolver)

    // Private state for holding the search query.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Private state for holding the full, unfiltered list of contacts.
    private val _allContacts = MutableStateFlow<List<Contact>>(emptyList())

    // The main UI state exposed to the screen.
    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val uiState: StateFlow<ContactsUiState> = _uiState

    // When the ViewModel is created, fetch the contacts.
    fun fetchContacts() {
        viewModelScope.launch {
            _uiState.value = ContactsUiState.Loading
            try {
                // Fetch the full list and store it in _allContacts.
                val contacts = repository.getContacts()
                _allContacts.value = contacts
                // Initial state is Success with the full list.
                _uiState.value = ContactsUiState.Success(contacts)
                // Start observing changes to the search query to filter the list.
                observeSearchQuery()
            } catch (_: Exception) {
                _uiState.value = ContactsUiState.Error
            }
        }
    }

    // Function to update the search query from the UI.
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            // 'combine' is a powerful flow operator. It listens to changes in both
            // the search query and the full contact list. If either changes,
            // it re-runs the logic to produce a new, filtered list.
            searchQuery.combine(_allContacts) { query, contacts ->
                if (query.isBlank()) {
                    // If the query is empty, show the full list.
                    ContactsUiState.Success(contacts)
                } else {
                    // Otherwise, filter the list.
                    val filteredList = contacts.filter {
                        it.name.contains(query, ignoreCase = true)
                    }
                    ContactsUiState.Success(filteredList)
                }
            }.collect { filteredState ->
                // Emit the new filtered state to the UI.
                _uiState.value = filteredState
            }
        }
    }
}
