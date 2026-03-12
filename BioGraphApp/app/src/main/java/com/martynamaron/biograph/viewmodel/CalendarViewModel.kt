package com.martynamaron.biograph.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.martynamaron.biograph.data.local.DailyEntryEntity
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.data.repository.DailyEntryRepository
import com.martynamaron.biograph.data.repository.DataTypeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val dataTypeRepository: DataTypeRepository,
    private val dailyEntryRepository: DailyEntryRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    val dataTypes: StateFlow<List<DataTypeEntity>> = dataTypeRepository.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val datesWithEntries: StateFlow<List<String>> = _currentMonth.flatMapLatest { month ->
        val start = month.atDay(1).format(dateFormatter)
        val end = month.atEndOfMonth().format(dateFormatter)
        dailyEntryRepository.getDatesWithEntries(start, end)
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

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun clearSelectedDate() {
        _selectedDate.value = null
    }

    fun saveEntriesForDate(date: LocalDate, activeDataTypeIds: Set<Long>) {
        viewModelScope.launch {
            val dateString = date.format(dateFormatter)
            val entries = activeDataTypeIds.map { dataTypeId ->
                DailyEntryEntity(date = dateString, dataTypeId = dataTypeId)
            }
            dailyEntryRepository.replaceEntriesForDate(dateString, entries)
        }
    }

    class Factory(
        private val dataTypeRepository: DataTypeRepository,
        private val dailyEntryRepository: DailyEntryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(dataTypeRepository, dailyEntryRepository) as T
        }
    }
}
