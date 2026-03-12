package com.martynamaron.biograph.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.martynamaron.biograph.data.DEFAULT_SUGGESTIONS
import com.martynamaron.biograph.data.OnboardingSuggestion
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.data.repository.DataTypeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val suggestions: List<OnboardingSuggestion> = DEFAULT_SUGGESTIONS,
    val selectedIndices: Set<Int> = DEFAULT_SUGGESTIONS.indices
        .filter { DEFAULT_SUGGESTIONS[it].selectedByDefault }
        .toSet(),
    val isCreating: Boolean = false
)

class OnboardingViewModel(private val repository: DataTypeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun toggleSuggestion(index: Int) {
        val current = _uiState.value.selectedIndices
        _uiState.value = _uiState.value.copy(
            selectedIndices = if (index in current) current - index else current + index
        )
    }

    fun createSelectedTypes(onComplete: () -> Unit) {
        val state = _uiState.value
        if (state.isCreating) return

        _uiState.value = state.copy(isCreating = true)

        viewModelScope.launch {
            state.selectedIndices.forEach { index ->
                val suggestion = state.suggestions[index]
                repository.insert(
                    DataTypeEntity(emoji = suggestion.emoji, description = suggestion.description)
                )
            }
            onComplete()
        }
    }

    class Factory(private val repository: DataTypeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(repository) as T
        }
    }
}
