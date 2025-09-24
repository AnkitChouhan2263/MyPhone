package com.example.myphone.features.contacts.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myphone.features.contacts.data.ContactDetails
import com.example.myphone.features.contacts.data.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactDetailsViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // Add a new sealed interface for UI events to handle navigation.
    sealed interface UiEvent {
        object NavigateBack : UiEvent
    }

    data class ContactDetailsState(
        val isLoading: Boolean = true,
        val contactDetails: ContactDetails? = null,
        val error: String? = null,
        val showDeleteConfirmDialog: Boolean = false,
        val contactDeleted: Boolean = false,
        // New state to signal a refresh is needed.
        val needsRefresh: Boolean = false
    )

    private val _uiState = MutableStateFlow(ContactDetailsState())
    val uiState: StateFlow<ContactDetailsState> = _uiState.asStateFlow()

    private val repository = ContactsRepository(application.contentResolver)

    /**
     * Public function that can be called by the UI to load or reload the contact's details.
     * This is the key to ensuring the data is always fresh.
     */
    fun loadContactDetails() {
        val contactId: String? = savedStateHandle["contactId"]
        if (contactId == null) {
            _uiState.update { it.copy(isLoading = false, error = "Contact not found.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val details = repository.getContactDetails(contactId)
                _uiState.update { it.copy(isLoading = false, contactDetails = details) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load details.") }
            }
        }
    }

    fun onAction(action: DetailsAction) {
        when (action) {
            DetailsAction.ShowDeleteDialog -> _uiState.update { it.copy(showDeleteConfirmDialog = true) }
            DetailsAction.HideDeleteDialog -> _uiState.update { it.copy(showDeleteConfirmDialog = false) }
            DetailsAction.ConfirmDelete -> deleteContact()
            DetailsAction.ToggleFavorite -> toggleFavorite()
        }
    }

    private fun toggleFavorite() {
        val currentContact = _uiState.value.contactDetails ?: return
        val newFavoriteState = !currentContact.isFavorite

        viewModelScope.launch {
            val success = repository.setFavoriteStatus(currentContact.id, newFavoriteState)
            if (success) {
                // Update the local UI state immediately for a responsive feel.
                _uiState.update {
                    it.copy(
                        contactDetails = currentContact.copy(isFavorite = newFavoriteState),
                        needsRefresh = true // Signal that the main list needs to refresh.
                    )
                }
            }
        }
    }

    private fun deleteContact() {
        val contactId: String? = savedStateHandle["contactId"]
        if (contactId == null) {
            // Cannot delete if we don't know the ID
            _uiState.update { it.copy(showDeleteConfirmDialog = false) }
            return
        }
        viewModelScope.launch {
            val success = repository.deleteContact(contactId)
            if (success) {
                _uiState.update { it.copy(contactDeleted = true, showDeleteConfirmDialog = false) }
            } else {
                // Optionally handle the error case, e.g., show a toast
                _uiState.update { it.copy(showDeleteConfirmDialog = false) }
            }
        }
    }
}

// A new sealed interface for actions on this screen
sealed interface DetailsAction {
    object ShowDeleteDialog : DetailsAction
    object HideDeleteDialog : DetailsAction
    object ConfirmDelete : DetailsAction
    object ToggleFavorite : DetailsAction // New action
}

