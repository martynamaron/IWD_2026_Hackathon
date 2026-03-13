package com.martynamaron.biograph.util

import com.martynamaron.biograph.data.InputType
import com.martynamaron.biograph.data.local.DailyEntryEntity
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.data.local.MultiChoiceSelectionEntity
import com.martynamaron.biograph.data.repository.DailyEntryRepository
import com.martynamaron.biograph.data.repository.DataTypeRepository
import com.martynamaron.biograph.data.repository.MultipleChoiceRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class MockDataGenerator(
    private val dataTypeRepository: DataTypeRepository,
    private val dailyEntryRepository: DailyEntryRepository,
    private val multipleChoiceRepository: MultipleChoiceRepository
) {
    suspend fun generate(dataTypes: List<DataTypeEntity>) {
        if (dataTypes.isEmpty()) return

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now()
        val twoMonthsAgo = today.minusMonths(2)
        val startMonth = YearMonth.from(twoMonthsAgo)
        val startDate = startMonth.atDay(1)

        val entries = mutableListOf<DailyEntryEntity>()
        var date = startDate
        while (!date.isAfter(today)) {
            val dateString = date.format(formatter)
            for (dataType in dataTypes) {
                val inputType = try {
                    InputType.valueOf(dataType.inputType)
                } catch (_: IllegalArgumentException) {
                    InputType.TOGGLE
                }
                when (inputType) {
                    InputType.TOGGLE -> {
                        if (Random.nextFloat() < 0.5f) {
                            entries.add(DailyEntryEntity(date = dateString, dataTypeId = dataType.id))
                        }
                    }
                    InputType.SCALE -> {
                        if (Random.nextFloat() < 0.6f) {
                            entries.add(
                                DailyEntryEntity(
                                    date = dateString,
                                    dataTypeId = dataType.id,
                                    scaleValue = Random.nextInt(0, 6)
                                )
                            )
                        }
                    }
                    InputType.MULTIPLE_CHOICE -> {
                        // MC selections handled per-date below
                    }
                }
            }
            date = date.plusDays(1)
        }

        // Insert all toggle/scale entries in batches by date
        entries.groupBy { it.date }.forEach { (dateString, dayEntries) ->
            dailyEntryRepository.replaceEntriesForDate(dateString, dayEntries)
        }

        // Generate MC selections
        val mcTypes = dataTypes.filter {
            try { InputType.valueOf(it.inputType) == InputType.MULTIPLE_CHOICE } catch (_: Exception) { false }
        }
        for (mcType in mcTypes) {
            val options = multipleChoiceRepository.getOptions(mcType.id)
            if (options.isEmpty()) continue
            date = startDate
            while (!date.isAfter(today)) {
                val dateString = date.format(formatter)
                if (Random.nextFloat() < 0.5f) {
                    val selectedIds = options.filter { Random.nextFloat() < 0.4f }.map { it.id }.toSet()
                    if (selectedIds.isNotEmpty()) {
                        multipleChoiceRepository.saveSelections(dateString, mcType.id, selectedIds)
                    }
                }
                date = date.plusDays(1)
            }
        }
    }
}
