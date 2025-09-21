package com.example.myphone.features.recents.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myphone.features.recents.data.CallLogEntry
import com.example.myphone.features.recents.data.RecentsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecentsViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface RecentsUiState {
        object Loading : RecentsUiState
        data class Success(val callLog: List<CallLogEntry>) : RecentsUiState
        object Error : RecentsUiState
    }

    private val _uiState = MutableStateFlow<RecentsUiState>(RecentsUiState.Loading)
    val uiState: StateFlow<RecentsUiState> = _uiState

    private val repository = RecentsRepository(application.contentResolver)

    fun fetchRecents() {
        viewModelScope.launch {
            _uiState.value = RecentsUiState.Loading
            try {
                _uiState.value = RecentsUiState.Success(repository.getCallLog())
            } catch (_: Exception) {
                _uiState.value = RecentsUiState.Error
            }
        }
    }
}
