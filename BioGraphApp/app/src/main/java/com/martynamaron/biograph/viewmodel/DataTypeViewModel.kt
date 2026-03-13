package com.martynamaron.biograph.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.martynamaron.biograph.data.InputType
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.data.local.MultipleChoiceOptionEntity
import com.martynamaron.biograph.data.repository.DailyEntryRepository
import com.martynamaron.biograph.data.repository.DataTypeRepository
import com.martynamaron.biograph.data.repository.MultipleChoiceRepository
import com.martynamaron.biograph.ui.components.OptionUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ConfirmMigrationState {
    data object Hidden : ConfirmMigrationState()
    data class Pending(val affectedCount: Int, val newInputType: InputType) : ConfirmMigrationState()
}

data class DataTypeUiState(
    val emoji: String = "",
    val description: String = "",
    val inputType: InputType? = null,
    val options: List<OptionUiModel> = listOf(
        OptionUiModel(key = 0, emoji = "", label = ""),
        OptionUiModel(key = 1, emoji = "", label = "")
    ),
    val optionsError: String? = null,
    val emojiError: String? = null,
    val descriptionError: String? = null,
    val inputTypeError: String? = null,
    val saveError: String? = null,
    val isDialogOpen: Boolean = false,
    val editingDataType: DataTypeEntity? = null,
    val pendingDeleteDataType: DataTypeEntity? = null,
    val confirmMigrationState: ConfirmMigrationState = ConfirmMigrationState.Hidden
)

private var nextOptionKey = 100

class DataTypeViewModel(
    private val repository: DataTypeRepository,
    private val multipleChoiceRepository: MultipleChoiceRepository,
    private val dailyEntryRepository: DailyEntryRepository
) : ViewModel() {

    val dataTypes: StateFlow<List<DataTypeEntity>> = repository.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(DataTypeUiState())
    val uiState: StateFlow<DataTypeUiState> = _uiState.asStateFlow()

    fun openAddDialog() {
        _uiState.value = DataTypeUiState(isDialogOpen = true)
    }

    fun openEditDialog(dataType: DataTypeEntity) {
        val type = InputType.valueOf(dataType.inputType)
        // Pre-fill Mood → Scale per FR-011, Exercise → MC per FR-012
        val suggestedType = when {
            type == InputType.TOGGLE && dataType.description == "Mood" -> InputType.SCALE
            type == InputType.TOGGLE && dataType.description == "Exercise" -> InputType.MULTIPLE_CHOICE
            else -> type
        }
        val suggestedOptions = if (type == InputType.TOGGLE && dataType.description == "Exercise") {
            listOf(
                OptionUiModel(key = 0, emoji = "\uD83C\uDFC3", label = "Running"),
                OptionUiModel(key = 1, emoji = "\uD83C\uDFCA", label = "Swimming"),
                OptionUiModel(key = 2, emoji = "\uD83C\uDFBE", label = "Tennis"),
                OptionUiModel(key = 3, emoji = "\uD83D\uDC83", label = "Dancing")
            )
        } else {
            DataTypeUiState().options
        }
        _uiState.value = DataTypeUiState(
            emoji = dataType.emoji,
            description = dataType.description,
            inputType = suggestedType,
            options = suggestedOptions,
            isDialogOpen = true,
            editingDataType = dataType
        )
        if (type == InputType.MULTIPLE_CHOICE) {
            viewModelScope.launch {
                val existing = multipleChoiceRepository.getOptions(dataType.id)
                if (existing.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        options = existing.mapIndexed { i, opt ->
                            OptionUiModel(key = i, emoji = opt.emoji, label = opt.label)
                        }
                    )
                    nextOptionKey = existing.size
                }
            }
        }
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

    fun updateInputType(inputType: InputType) {
        _uiState.value = _uiState.value.copy(inputType = inputType, inputTypeError = null, saveError = null)
    }

    fun updateOptionEmoji(key: Int, emoji: String) {
        _uiState.value = _uiState.value.copy(
            options = _uiState.value.options.map { if (it.key == key) it.copy(emoji = emoji) else it },
            optionsError = null
        )
    }

    fun updateOptionLabel(key: Int, label: String) {
        if (label.length <= 30) {
            _uiState.value = _uiState.value.copy(
                options = _uiState.value.options.map { if (it.key == key) it.copy(label = label) else it },
                optionsError = null
            )
        }
    }

    fun removeOption(key: Int) {
        _uiState.value = _uiState.value.copy(
            options = _uiState.value.options.filter { it.key != key },
            optionsError = null
        )
    }

    fun addOption() {
        if (_uiState.value.options.size < 10) {
            _uiState.value = _uiState.value.copy(
                options = _uiState.value.options + OptionUiModel(key = nextOptionKey++, emoji = "", label = ""),
                optionsError = null
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
        var optionsError: String? = null
        var inputTypeError: String? = null

        if (emoji.isEmpty()) {
            emojiError = "Select an emoji"
            hasError = true
        }
        if (description.isEmpty()) {
            descriptionError = "Enter a description"
            hasError = true
        }
        if (state.inputType == null) {
            inputTypeError = "Select an input type"
            hasError = true
        }

        if (state.inputType == InputType.MULTIPLE_CHOICE) {
            val validOptions = state.options.filter { it.emoji.isNotBlank() && it.label.isNotBlank() }
            val hasEmptyLabel = state.options.any { it.emoji.isNotBlank() && it.label.isBlank() }
            val duplicates = state.options
                .filter { it.emoji.isNotBlank() && it.label.isNotBlank() }
                .groupBy { "${it.emoji}${it.label}" }
                .any { it.value.size > 1 }

            if (validOptions.size < 2) {
                optionsError = "At least 2 complete options required"
                hasError = true
            } else if (hasEmptyLabel) {
                optionsError = "All options must have a label"
                hasError = true
            } else if (duplicates) {
                optionsError = "Duplicate options are not allowed"
                hasError = true
            }
        }

        if (hasError) {
            _uiState.value = state.copy(
                emojiError = emojiError,
                descriptionError = descriptionError,
                optionsError = optionsError,
                inputTypeError = inputTypeError
            )
            return
        }

        val inputType = state.inputType!!

        viewModelScope.launch {
            val editing = state.editingDataType

            // Check if this is a type migration (existing Toggle → Scale or MC)
            if (editing != null && editing.inputType == InputType.TOGGLE.name && inputType != InputType.TOGGLE) {
                val count = dailyEntryRepository.countEntriesForDataType(editing.id)
                _uiState.value = state.copy(
                    confirmMigrationState = ConfirmMigrationState.Pending(
                        affectedCount = count,
                        newInputType = inputType
                    )
                )
                return@launch
            }

            performSave(state)
        }
    }

    fun dismissMigrationDialog() {
        _uiState.value = _uiState.value.copy(
            confirmMigrationState = ConfirmMigrationState.Hidden
        )
    }

    fun confirmMigration() {
        val state = _uiState.value
        val migration = state.confirmMigrationState as? ConfirmMigrationState.Pending ?: return
        val editing = state.editingDataType ?: return

        viewModelScope.launch {
            try {
                when (migration.newInputType) {
                    InputType.SCALE -> repository.migrateDataTypeToScale(editing.id)
                    InputType.MULTIPLE_CHOICE -> {
                        val validOptions = state.options.filter { it.emoji.isNotBlank() && it.label.isNotBlank() }
                        multipleChoiceRepository.migrateDataTypeToMultipleChoice(
                            editing.id,
                            validOptions.mapIndexed { index, opt ->
                                MultipleChoiceOptionEntity(
                                    dataTypeId = editing.id,
                                    emoji = opt.emoji,
                                    label = opt.label,
                                    sortOrder = index
                                )
                            }
                        )
                    }
                    else -> {}
                }
                // Update emoji/description if changed
                val updated = repository.getById(editing.id)
                if (updated != null) {
                    val emoji = state.emoji.trim()
                    val description = state.description.trim()
                    if (emoji != updated.emoji || description != updated.description) {
                        repository.update(updated.copy(emoji = emoji, description = description))
                    }
                }
                _uiState.value = DataTypeUiState()
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    confirmMigrationState = ConfirmMigrationState.Hidden,
                    saveError = e.message ?: "Migration failed"
                )
            }
        }
    }

    private suspend fun performSave(state: DataTypeUiState) {
        val emoji = state.emoji.trim()
        val description = state.description.trim()
        val editing = state.editingDataType
        val inputType = state.inputType!!

        val result = if (editing != null) {
            repository.update(editing.copy(emoji = emoji, description = description, inputType = inputType.name))
        } else {
            repository.insert(DataTypeEntity(emoji = emoji, description = description, inputType = inputType.name))
        }

        result.fold(
            onSuccess = { idOrUnit ->
                if (inputType == InputType.MULTIPLE_CHOICE) {
                    val dataTypeId = if (editing != null) editing.id else idOrUnit as Long
                    val validOptions = state.options.filter { it.emoji.isNotBlank() && it.label.isNotBlank() }
                    if (editing != null) {
                        multipleChoiceRepository.deleteAllOptionsForType(dataTypeId)
                    }
                    multipleChoiceRepository.saveOptions(
                        validOptions.mapIndexed { index, opt ->
                            MultipleChoiceOptionEntity(
                                dataTypeId = dataTypeId,
                                emoji = opt.emoji,
                                label = opt.label,
                                sortOrder = index
                            )
                        }
                    )
                }
                _uiState.value = DataTypeUiState()
            },
            onFailure = { e ->
                _uiState.value = state.copy(saveError = e.message ?: "Save failed")
            }
        )
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

    class Factory(
        private val repository: DataTypeRepository,
        private val multipleChoiceRepository: MultipleChoiceRepository,
        private val dailyEntryRepository: DailyEntryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DataTypeViewModel(repository, multipleChoiceRepository, dailyEntryRepository) as T
        }
    }
}
