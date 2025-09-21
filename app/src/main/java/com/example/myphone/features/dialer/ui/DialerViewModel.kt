package com.example.myphone.features.dialer.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the Dialer screen.
 * It holds the state of the entered number and handles user actions.
 */
class DialerViewModel : ViewModel() {

    // Private mutable state flow, only the ViewModel can change it.
    private val _uiState = MutableStateFlow(DialerUiState())
    // Public, read-only state flow for the UI to observe.
    val uiState: StateFlow<DialerUiState> = _uiState.asStateFlow()

    /**
     * Handles all actions coming from the UI.
     * This follows a Unidirectional Data Flow pattern.
     */
    fun onAction(action: DialerAction) {
        when (action) {
            is DialerAction.NumberPressed -> {
                // Appends the new digit to the existing number.
                _uiState.update { it.copy(enteredNumber = it.enteredNumber + action.number) }
            }
            DialerAction.Delete -> {
                // Removes the last character from the number string.
                _uiState.update {
                    it.copy(enteredNumber = it.enteredNumber.dropLast(1))
                }
            }
        }
    }
}

/**
 * Represents the state of the Dialer UI.
 */
data class DialerUiState(
    val enteredNumber: String = ""
)

/**
 * Defines all possible user actions on the Dialer screen.
 * Using a sealed interface makes our action handling type-safe.
 */
sealed interface DialerAction {
    data class NumberPressed(val number: Char) : DialerAction
    object Delete : DialerAction
}
