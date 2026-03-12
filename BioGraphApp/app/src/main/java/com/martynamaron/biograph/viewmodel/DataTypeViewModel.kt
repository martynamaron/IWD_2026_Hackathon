package com.martynamaron.biograph.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.data.repository.DataTypeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DataTypeUiState(
    val emoji: String = "",
    val description: String = "",
    val emojiError: String? = null,
    val descriptionError: String? = null,
    val saveError: String? = null,
    val isDialogOpen: Boolean = false,
    val editingDataType: DataTypeEntity? = null,
    val pendingDeleteDataType: DataTypeEntity? = null
)

class DataTypeViewModel(private val repository: DataTypeRepository) : ViewModel() {

    val dataTypes: StateFlow<List<DataTypeEntity>> = repository.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(DataTypeUiState())
    val uiState: StateFlow<DataTypeUiState> = _uiState.asStateFlow()

    fun openAddDialog() {
        _uiState.value = DataTypeUiState(isDialogOpen = true)
    }

    fun openEditDialog(dataType: DataTypeEntity) {
        _uiState.value = DataTypeUiState(
            emoji = dataType.emoji,
            description = dataType.description,
            isDialogOpen = true,
            editingDataType = dataType
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(isDialogOpen = false)
    }

    fun updateEmoji(emoji: String) {
        _uiState.value = _uiState.value.copy(emoji = emoji, emojiError = null, saveError = null)
    }

    fun updateDescription(description: String) {
        if (description.length <= 60) {
            _uiState.value = _uiState.value.copy(
                description = description,
                descriptionError = null,
                saveError = null
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val emoji = state.emoji.trim()
        val description = state.description.trim()

        var hasError = false
        var emojiError: String? = null
        var descriptionError: String? = null

        if (emoji.isEmpty()) {
            emojiError = "Select an emoji"
            hasError = true
        }
        if (description.isEmpty()) {
            descriptionError = "Enter a description"
            hasError = true
        }

        if (hasError) {
            _uiState.value = state.copy(emojiError = emojiError, descriptionError = descriptionError)
            return
        }

        viewModelScope.launch {
            val editing = state.editingDataType
            val result = if (editing != null) {
                repository.update(editing.copy(emoji = emoji, description = description))
            } else {
                repository.insert(DataTypeEntity(emoji = emoji, description = description)).map { }
            }

            result.fold(
                onSuccess = {
                    _uiState.value = DataTypeUiState()
                },
                onFailure = { e ->
                    _uiState.value = state.copy(saveError = e.message ?: "Save failed")
                }
            )
        }
    }

    fun requestDelete(dataType: DataTypeEntity) {
        _uiState.value = _uiState.value.copy(pendingDeleteDataType = dataType)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(pendingDeleteDataType = null)
    }

    fun confirmDelete() {
        val dataType = _uiState.value.pendingDeleteDataType ?: return
        viewModelScope.launch {
            repository.delete(dataType)
            _uiState.value = _uiState.value.copy(pendingDeleteDataType = null)
        }
    }

    class Factory(private val repository: DataTypeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DataTypeViewModel(repository) as T
        }
    }
}
