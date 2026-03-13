package com.martynamaron.biograph.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.martynamaron.biograph.data.analysis.CorrelationEngine
import com.martynamaron.biograph.data.analysis.InsightTextGenerator
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.data.local.InsightEntity
import com.martynamaron.biograph.data.local.MultipleChoiceOptionEntity
import com.martynamaron.biograph.data.repository.DataTypeRepository
import com.martynamaron.biograph.data.repository.InsightRepository
import com.martynamaron.biograph.data.repository.DailyEntryRepository
import com.martynamaron.biograph.data.repository.MultipleChoiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed interface InsightPanelState {
    data object Loading : InsightPanelState
    data class Success(val insights: List<InsightEntity>) : InsightPanelState
    data class InsufficientData(val message: String) : InsightPanelState
    data class Error(val message: String) : InsightPanelState
    data object Hidden : InsightPanelState
}

enum class InsightPeriod(val label: String, val months: Long) {
    LAST_MONTH("Last month", 1),
    LAST_3_MONTHS("Last 3 months", 3),
    LAST_YEAR("Last year", 12)
}

class InsightViewModel(
    private val insightRepository: InsightRepository,
    private val dataTypeRepository: DataTypeRepository,
    private val dailyEntryRepository: DailyEntryRepository,
    private val multipleChoiceRepository: MultipleChoiceRepository
) : ViewModel() {

    private val _state = MutableStateFlow<InsightPanelState>(InsightPanelState.Loading)
    val stateFlow: StateFlow<InsightPanelState> = _state.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(InsightPeriod.LAST_3_MONTHS)
    val selectedPeriod: StateFlow<InsightPeriod> = _selectedPeriod.asStateFlow()

    private val correlationEngine = CorrelationEngine()
    private val textGenerator = InsightTextGenerator()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        checkAndAnalyse()
    }

    fun refresh() {
        checkAndAnalyse()
    }

    fun selectPeriod(period: InsightPeriod) {
        if (_selectedPeriod.value != period) {
            _selectedPeriod.value = period
            checkAndAnalyse()
        }
    }

    private fun checkAndAnalyse() {
        viewModelScope.launch {
            try {
                // Check data type count
                val dataTypes = dataTypeRepository.getAllFlow().first()
                if (dataTypes.isEmpty()) {
                    _state.value = InsightPanelState.Hidden
                    return@launch
                }
                if (dataTypes.size < 2) {
                    _state.value = InsightPanelState.InsufficientData(
                        "Track more than one data type to discover patterns."
                    )
                    return@launch
                }

                // Check day count
                val allEntries = withContext(Dispatchers.IO) {
                    dailyEntryRepository.getAllEntries()
                }

                // Filter entries by selected time period
                val cutoffDate = LocalDate.now().minusMonths(_selectedPeriod.value.months)
                    .format(dateFormatter)
                val entries = allEntries.filter { it.date >= cutoffDate }

                val distinctDays = entries.map { it.date }.distinct().size
                if (distinctDays < 7) {
                    _state.value = InsightPanelState.InsufficientData(
                        "Keep tracking! Insights will appear once there's enough data to find patterns."
                    )
                    return@launch
                }

                // Show loading state
                _state.value = InsightPanelState.Loading

                // Run analysis on background thread
                val results = withContext(Dispatchers.Default) {
                    val allSelections = withContext(Dispatchers.IO) {
                        multipleChoiceRepository.getAllSelections()
                    }.filter { it.date >= cutoffDate }
                    val allDates = entries.map { it.date }.distinct().sorted()
                    val optionsMap = mutableMapOf<Long, List<MultipleChoiceOptionEntity>>()
                    for (dt in dataTypes) {
                        val opts = withContext(Dispatchers.IO) {
                            multipleChoiceRepository.getOptions(dt.id)
                        }
                        if (opts.isNotEmpty()) optionsMap[dt.id] = opts
                    }

                    correlationEngine.analyseAll(
                        entries = entries,
                        selections = allSelections,
                        dataTypes = dataTypes,
                        options = optionsMap,
                        dates = allDates
                    )
                }

                // Convert to entities with text
                val insights = results.map { result ->
                    InsightEntity(
                        dataType1Id = result.dataType1Id,
                        dataType2Id = result.dataType2Id,
                        optionId = result.optionId,
                        correlationCoefficient = result.coefficient,
                        correlationMethod = result.method,
                        insightText = textGenerator.generate(result),
                        sampleSize = result.sampleSize
                    )
                }

                // Persist
                withContext(Dispatchers.IO) {
                    insightRepository.replaceAllInsights(insights)
                    insightRepository.recordAnalysisCompleted()
                }

                if (insights.isEmpty()) {
                    _state.value = InsightPanelState.InsufficientData(
                        "No patterns found yet — try tracking varied data over time."
                    )
                } else {
                    _state.value = InsightPanelState.Success(insights)
                }
            } catch (e: Exception) {
                _state.value = InsightPanelState.Error(
                    "Couldn't generate insights right now. Try again later."
                )
            }
        }
    }

    class Factory(
        private val insightRepository: InsightRepository,
        private val dataTypeRepository: DataTypeRepository,
        private val dailyEntryRepository: DailyEntryRepository,
        private val multipleChoiceRepository: MultipleChoiceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InsightViewModel(insightRepository, dataTypeRepository, dailyEntryRepository, multipleChoiceRepository) as T
        }
    }
}
