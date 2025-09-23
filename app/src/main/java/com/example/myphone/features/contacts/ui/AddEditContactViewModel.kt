package com.example.myphone.features.contacts.ui

import android.app.Application
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myphone.features.contacts.data.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditContactViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = ContactsRepository(application.contentResolver)

    private val _uiState = MutableStateFlow(AddEditContactUiState())
    val uiState: StateFlow<AddEditContactUiState> = _uiState.asStateFlow()

    /**
     * An explicit command from the UI to prepare the ViewModel for editing an existing contact.
     */
    fun loadContactForEditing(id: String) {
        // This check prevents redundant reloads if the function is called during recomposition.
        if (_uiState.value.id == id) return

        _uiState.update { it.copy(isLoading = true, screenTitle = "Edit Contact") }
        viewModelScope.launch {
            try {
                val details = repository.getContactDetails(id)
                val nameParts = details.name.split(" ", limit = 2)
                val firstName = nameParts.getOrNull(0) ?: ""
                val lastName = nameParts.getOrNull(1) ?: ""

                _uiState.update {
                    it.copy(
                        id = id,
                        isLoading = false,
                        firstName = firstName,
                        lastName = lastName,
                        phone = details.phoneNumbers.firstOrNull() ?: "",
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onAction(action: AddEditContactAction) {
        when (action) {
            is AddEditContactAction.UpdateFirstName -> _uiState.update { it.copy(firstName = action.name) }
            is AddEditContactAction.UpdateLastName -> _uiState.update { it.copy(lastName = action.name) }
            is AddEditContactAction.UpdatePhone -> _uiState.update { it.copy(phone = action.phone) }
            is AddEditContactAction.UpdateEmail -> _uiState.update { it.copy(email = action.email) }
            is AddEditContactAction.SaveContact -> saveContact()
        }
    }

    private fun saveContact() {
        // Get the ID from the handle at the moment of saving.
        val contactId: String? = savedStateHandle["contactId"]
        if (_uiState.value.firstName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val success = if (contactId == null) {
                repository.addContact(
                    firstName = _uiState.value.firstName,
                    lastName = _uiState.value.lastName,
                    phoneNumber = _uiState.value.phone,
                    phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                    email = _uiState.value.email,
                    emailType = ContactsContract.CommonDataKinds.Email.TYPE_HOME
                )
            } else {
                repository.updateContact(
                    contactId = contactId,
                    firstName = _uiState.value.firstName,
                    lastName = _uiState.value.lastName,
                    phoneNumber = _uiState.value.phone
                )
            }
            _uiState.update { it.copy(isSaving = false, didSave = success) }
        }
    }
}

data class AddEditContactUiState(
    val id: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val didSave: Boolean = false,
    val screenTitle: String = "Add Contact"
)

sealed interface AddEditContactAction {
    data class UpdateFirstName(val name: String) : AddEditContactAction
    data class UpdateLastName(val name: String) : AddEditContactAction
    data class UpdatePhone(val phone: String) : AddEditContactAction
    data class UpdateEmail(val email: String) : AddEditContactAction
    object SaveContact : AddEditContactAction
}

