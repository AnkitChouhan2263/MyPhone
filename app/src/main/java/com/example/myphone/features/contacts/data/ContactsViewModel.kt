package com.example.myphone.features.contacts.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    data class ContactSearchResult(
        val contact: Contact,
        val matchedQuery: String,
        val matchedNumber: String? = null
    )

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
                    val trimmedQuery = query.trim()
                    // Use flatMap to generate a result for each match (name or number).
                    val filteredResults = contacts.flatMap { contact ->
                        val resultsForContact = mutableListOf<ContactSearchResult>()

                        // Check for a name match (including initials)
                        if (matchesName(contact.name, trimmedQuery)) {
                            resultsForContact.add(ContactSearchResult(contact, trimmedQuery, null))
                        }

                        // Check for number matches
                        val digitsOnlyQuery = trimmedQuery.replace(Regex("\\D"), "")
                        if (digitsOnlyQuery.isNotEmpty()) {
                            contact.numbers
                                .filter { it.replace(Regex("\\D"), "").contains(digitsOnlyQuery) }
                                .forEach { matchingNumber ->
                                    resultsForContact.add(ContactSearchResult(contact, trimmedQuery, matchingNumber))
                                }
                        }
                        resultsForContact
                    }
                    // Use distinct() to prevent showing the same result twice (e.g., if name and number both match the same query)
                    ContactsUiState.Success(filteredResults.distinct())
                }
            }.collect { filteredState ->
                _uiState.value = filteredState
            }
        }
    }

    private fun matchesName(name: String, query: String): Boolean {
        if (name.contains(query, ignoreCase = true)) {
            return true
        }
        val queryParts = query.split(" ").filter { it.isNotBlank() }
        val nameParts = name.split(" ").filter { it.isNotBlank() }
        if (queryParts.isNotEmpty() && nameParts.isNotEmpty()) {
            return queryParts.all { queryPart ->
                nameParts.any { namePart -> namePart.startsWith(queryPart, ignoreCase = true) }
            }
        }
        return false
    }
}

