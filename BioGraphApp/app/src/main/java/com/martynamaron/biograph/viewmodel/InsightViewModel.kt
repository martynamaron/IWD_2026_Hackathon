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
import com.martynamaron.biograph.data.repository.UserPreferenceRepository
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

enum class InsightSortMode {
    BY_STRENGTH,
    BY_DATA_TYPE
}

enum class StrengthTier(val label: String) {
    STRONG("Strong"),
    MODERATE("Moderate"),
    MILD("Mild");

    companion object {
        fun fromCoefficient(coefficient: Double): StrengthTier {
            val abs = kotlin.math.abs(coefficient)
            return when {
                abs >= 0.80 -> STRONG
                abs >= 0.60 -> MODERATE
                else -> MILD
            }
        }
    }
}

data class GroupedInsight(
    val insight: InsightEntity,
    val alsoInDataTypeName: String?
)

data class DataTypeInsightGroup(
    val dataTypeName: String,
    val insightCount: Int,
    val insights: List<GroupedInsight>
)

sealed interface InsightSortState {
    data class ByStrength(
        val insights: List<InsightEntity>
    ) : InsightSortState

    data class ByDataType(
        val groups: List<DataTypeInsightGroup>
    ) : InsightSortState
}

class InsightViewModel(
    private val insightRepository: InsightRepository,
    private val dataTypeRepository: DataTypeRepository,
    private val dailyEntryRepository: DailyEntryRepository,
    private val multipleChoiceRepository: MultipleChoiceRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    private val _state = MutableStateFlow<InsightPanelState>(InsightPanelState.Loading)
    val stateFlow: StateFlow<InsightPanelState> = _state.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(InsightPeriod.LAST_3_MONTHS)
    val selectedPeriod: StateFlow<InsightPeriod> = _selectedPeriod.asStateFlow()

    private val _sortMode = MutableStateFlow(InsightSortMode.BY_STRENGTH)
    val sortMode: StateFlow<InsightSortMode> = _sortMode.asStateFlow()

    private val _sortState = MutableStateFlow<InsightSortState>(InsightSortState.ByStrength(emptyList()))
    val sortState: StateFlow<InsightSortState> = _sortState.asStateFlow()

    private var cachedDataTypes: List<DataTypeEntity> = emptyList()

    private val correlationEngine = CorrelationEngine()
    private val textGenerator = InsightTextGenerator()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        loadPersistedSortMode()
        checkAndAnalyse()
    }

    private fun loadPersistedSortMode() {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                userPreferenceRepository.getSortMode()
            }
            if (saved != null) {
                try {
                    _sortMode.value = InsightSortMode.valueOf(saved)
                } catch (_: IllegalArgumentException) {
                    // Invalid stored value — keep default
                }
            }
        }
    }

    fun setSortMode(mode: InsightSortMode) {
        if (_sortMode.value != mode) {
            _sortMode.value = mode
            recomputeSortState()
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    userPreferenceRepository.setSortMode(mode.name)
                }
            }
        }
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
                cachedDataTypes = dataTypes
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
                    recomputeSortState()
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
        private val multipleChoiceRepository: MultipleChoiceRepository,
        private val userPreferenceRepository: UserPreferenceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InsightViewModel(insightRepository, dataTypeRepository, dailyEntryRepository, multipleChoiceRepository, userPreferenceRepository) as T
        }
    }

    private fun recomputeSortState() {
        val currentState = _state.value
        if (currentState !is InsightPanelState.Success) return
        val insights = currentState.insights

        when (_sortMode.value) {
            InsightSortMode.BY_STRENGTH -> {
                val sorted = insights.sortedWith(
                    compareByDescending<InsightEntity> { kotlin.math.abs(it.correlationCoefficient) }
                        .thenByDescending { it.computedAt }
                )
                _sortState.value = InsightSortState.ByStrength(sorted)
            }
            InsightSortMode.BY_DATA_TYPE -> {
                _sortState.value = buildGroupedState(insights)
            }
        }
    }

    private fun buildGroupedState(insights: List<InsightEntity>): InsightSortState.ByDataType {
        val dataTypeMap = cachedDataTypes.associateBy { it.id }

        // Build a map: dataTypeId -> list of insights involving that data type
        val groupMap = mutableMapOf<Long, MutableList<InsightEntity>>()
        for (insight in insights) {
            groupMap.getOrPut(insight.dataType1Id) { mutableListOf() }.add(insight)
            if (insight.dataType2Id != insight.dataType1Id) {
                groupMap.getOrPut(insight.dataType2Id) { mutableListOf() }.add(insight)
            }
        }

        val groups = groupMap.mapNotNull { (dataTypeId, groupInsights) ->
            val dataType = dataTypeMap[dataTypeId] ?: return@mapNotNull null
            val displayName = "${dataType.emoji} ${dataType.description}"

            // Sort within group by |coefficient| desc, then computedAt desc
            val sortedInsights = groupInsights.sortedWith(
                compareByDescending<InsightEntity> { kotlin.math.abs(it.correlationCoefficient) }
                    .thenByDescending { it.computedAt }
            )

            // Build GroupedInsight with "Also in" cross-reference
            val grouped = sortedInsights.map { insight ->
                val otherDataTypeId = if (insight.dataType1Id == dataTypeId) insight.dataType2Id else insight.dataType1Id
                val otherDataType = dataTypeMap[otherDataTypeId]
                val alsoIn = otherDataType?.let { "${it.emoji} ${it.description}" }
                GroupedInsight(insight = insight, alsoInDataTypeName = alsoIn)
            }

            DataTypeInsightGroup(
                dataTypeName = displayName,
                insightCount = grouped.size,
                insights = grouped
            )
        }.sortedBy { it.dataTypeName }

        return InsightSortState.ByDataType(groups)
    }
}
