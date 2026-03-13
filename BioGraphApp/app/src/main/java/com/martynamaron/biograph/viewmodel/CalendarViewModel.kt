package com.martynamaron.biograph.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.martynamaron.biograph.data.InputType
import com.martynamaron.biograph.data.local.DailyEntryEntity
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.data.repository.DailyEntryRepository
import com.martynamaron.biograph.data.repository.DataTypeRepository
import com.martynamaron.biograph.data.repository.MultipleChoiceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val dataTypeRepository: DataTypeRepository,
    private val dailyEntryRepository: DailyEntryRepository,
    private val multipleChoiceRepository: MultipleChoiceRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _scaleValues = MutableStateFlow<Map<Long, Int?>>(emptyMap())
    val scaleValues: StateFlow<Map<Long, Int?>> = _scaleValues.asStateFlow()

    private val _multiChoiceSelections = MutableStateFlow<Map<Long, Set<Long>>>(emptyMap())
    val multiChoiceSelections: StateFlow<Map<Long, Set<Long>>> = _multiChoiceSelections.asStateFlow()

    val dataTypes: StateFlow<List<DataTypeEntity>> = dataTypeRepository.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val datesWithEntries: StateFlow<List<String>> = _currentMonth.flatMapLatest { month ->
        val start = month.atDay(1).format(dateFormatter)
        val end = month.atEndOfMonth().format(dateFormatter)
        dailyEntryRepository.getDatesWithAnyEntry(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entriesForSelectedDate: StateFlow<List<DailyEntryEntity>> = _selectedDate.flatMapLatest { date ->
        if (date != null) {
            dailyEntryRepository.getEntriesForDate(date.format(dateFormatter))
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateToPreviousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun navigateToNextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun setCurrentMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        viewModelScope.launch {
            val dateString = date.format(dateFormatter)
            // Load scale values
            val entries = dailyEntryRepository.getEntriesForDateSnapshot(dateString)
            val scales = entries
                .filter { it.scaleValue != null }
                .associate { it.dataTypeId to it.scaleValue }
            _scaleValues.value = scales

            // Load MC selections for all MC data types on this date
            val allDataTypes = dataTypes.value
            val mcTypes = allDataTypes.filter {
                try { InputType.valueOf(it.inputType) == InputType.MULTIPLE_CHOICE } catch (_: Exception) { false }
            }
            val selections = mutableMapOf<Long, Set<Long>>()
            for (dt in mcTypes) {
                val sels = multipleChoiceRepository.getSelections(dateString, dt.id).first()
                if (sels.isNotEmpty()) {
                    selections[dt.id] = sels.map { it.optionId }.toSet()
                }
            }
            _multiChoiceSelections.value = selections
        }
    }

    fun clearSelectedDate() {
        _selectedDate.value = null
        _scaleValues.value = emptyMap()
        _multiChoiceSelections.value = emptyMap()
    }

    fun setScaleValue(dataTypeId: Long, value: Int?) {
        _scaleValues.value = _scaleValues.value.toMutableMap().apply {
            put(dataTypeId, value)
        }
    }

    fun clearScaleValue(dataTypeId: Long) {
        _scaleValues.value = _scaleValues.value.toMutableMap().apply {
            remove(dataTypeId)
        }
    }

    fun toggleMultiChoiceOption(dataTypeId: Long, optionId: Long) {
        val current = _multiChoiceSelections.value[dataTypeId] ?: emptySet()
        val updated = if (optionId in current) current - optionId else current + optionId
        _multiChoiceSelections.value = _multiChoiceSelections.value.toMutableMap().apply {
            put(dataTypeId, updated)
        }
    }

    fun saveEntriesForDate(date: LocalDate, activeDataTypeIds: Set<Long>) {
        viewModelScope.launch {
            val dateString = date.format(dateFormatter)
            val scaleMap = _scaleValues.value
                .filterValues { it != null }
                .mapValues { it.value!! }
            dailyEntryRepository.saveEntries(dateString, activeDataTypeIds, scaleMap)

            // Save MC selections
            val mcSelections = _multiChoiceSelections.value
            for ((dataTypeId, selectedIds) in mcSelections) {
                multipleChoiceRepository.saveSelections(dateString, dataTypeId, selectedIds)
            }
        }
    }

    class Factory(
        private val dataTypeRepository: DataTypeRepository,
        private val dailyEntryRepository: DailyEntryRepository,
        private val multipleChoiceRepository: MultipleChoiceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(dataTypeRepository, dailyEntryRepository, multipleChoiceRepository) as T
        }
    }
}
