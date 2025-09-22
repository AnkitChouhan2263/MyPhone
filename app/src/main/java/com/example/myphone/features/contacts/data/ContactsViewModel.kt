package com.example.myphone.features.contacts.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    data class ContactSearchResult(val contact: Contact, val matchedQuery: String)

    sealed interface ContactsUiState {
        object Loading : ContactsUiState
        data class Success(val results: List<ContactSearchResult>) : ContactsUiState
        object Error : ContactsUiState
    }

    private val repository = ContactsRepository(application.contentResolver)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _allContacts = MutableStateFlow<List<Contact>>(emptyList())
    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val uiState: StateFlow<ContactsUiState> = _uiState

    fun fetchContacts() {
        viewModelScope.launch {
            _uiState.value = ContactsUiState.Loading
            try {
                val contacts = repository.getContacts()
                _allContacts.value = contacts
                val initialResults = contacts.map { ContactSearchResult(it, "") }
                _uiState.value = ContactsUiState.Success(initialResults)
                observeSearchQuery()
            } catch (_: Exception) {
                _uiState.value = ContactsUiState.Error
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQuery.combine(_allContacts) { query, contacts ->
                if (query.isBlank()) {
                    val results = contacts.map { ContactSearchResult(it, "") }
                    ContactsUiState.Success(results)
                } else {
                    val filteredResults = contacts.mapNotNull { contact ->
                        findMatch(contact, query)?.let { matchedPart ->
                            ContactSearchResult(contact, matchedPart)
                        }
                    }
                    ContactsUiState.Success(filteredResults)
                }
            }.collect { filteredState ->
                _uiState.value = filteredState
            }
        }
    }

    private fun findMatch(contact: Contact, query: String): String? {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return null

        if (contact.name.contains(trimmedQuery, ignoreCase = true)) {
            return trimmedQuery
        }

        val digitsOnlyQuery = trimmedQuery.replace(Regex("\\D"), "")
        if (digitsOnlyQuery.isNotEmpty()) {
            // UPDATED: Check if ANY number in the list matches the query.
            if (contact.numbers.any { it.replace(Regex("\\D"), "").contains(digitsOnlyQuery) }) {
                return trimmedQuery
            }
        }

        val queryParts = trimmedQuery.split(" ").filter { it.isNotBlank() }
        val nameParts = contact.name.split(" ").filter { it.isNotBlank() }

        if (queryParts.isNotEmpty() && nameParts.isNotEmpty()) {
            val allInitialsMatch = queryParts.all { queryPart ->
                nameParts.any { namePart -> namePart.startsWith(queryPart, ignoreCase = true) }
            }
            if (allInitialsMatch) {
                return trimmedQuery
            }
        }

        return null
    }
}

