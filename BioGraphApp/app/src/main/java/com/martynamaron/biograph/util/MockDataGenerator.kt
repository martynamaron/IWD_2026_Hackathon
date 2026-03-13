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

        // Build lookup maps by description for correlation seeding
        val byDesc = dataTypes.associateBy { it.description.lowercase() }

        val entries = mutableListOf<DailyEntryEntity>()
        var date = startDate
        while (!date.isAfter(today)) {
            val dateString = date.format(formatter)

            // Day-level random state used to drive correlated values
            val leftHouse = Random.nextFloat() < 0.6f
            val sawFriends = if (leftHouse) Random.nextFloat() < 0.55f else Random.nextFloat() < 0.15f
            val headache = if (!leftHouse) Random.nextFloat() < 0.55f else Random.nextFloat() < 0.15f
            val exercised = if (leftHouse) Random.nextFloat() < 0.5f else Random.nextFloat() < 0.1f
            // Mood correlates positively with seeing friends and leaving the house
            val baseMood = when {
                sawFriends && leftHouse -> Random.nextInt(3, 6)
                leftHouse -> Random.nextInt(2, 5)
                else -> Random.nextInt(0, 3)
            }

            for (dataType in dataTypes) {
                val inputType = try {
                    InputType.valueOf(dataType.inputType)
                } catch (_: IllegalArgumentException) {
                    InputType.TOGGLE
                }
                val desc = dataType.description.lowercase()
                when (inputType) {
                    InputType.TOGGLE -> {
                        val present = when (desc) {
                            "left the house" -> leftHouse
                            "saw friends" -> sawFriends
                            "headache" -> headache
                            else -> Random.nextFloat() < 0.5f
                        }
                        if (present) {
                            entries.add(DailyEntryEntity(date = dateString, dataTypeId = dataType.id))
                        }
                    }
                    InputType.SCALE -> {
                        if (Random.nextFloat() < 0.7f) {
                            val value = when (desc) {
                                "mood" -> baseMood.coerceIn(0, 5)
                                else -> Random.nextInt(0, 6)
                            }
                            entries.add(
                                DailyEntryEntity(
                                    date = dateString,
                                    dataTypeId = dataType.id,
                                    scaleValue = value
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

        // Generate MC selections with exercise correlation
        val mcTypes = dataTypes.filter {
            try { InputType.valueOf(it.inputType) == InputType.MULTIPLE_CHOICE } catch (_: Exception) { false }
        }
        for (mcType in mcTypes) {
            val options = multipleChoiceRepository.getOptions(mcType.id)
            if (options.isEmpty()) continue
            date = startDate
            while (!date.isAfter(today)) {
                val dateString = date.format(formatter)
                // Check if this day had "left the house" to correlate exercise
                val dayEntries = entries.filter { it.date == dateString }
                val leftHouseType = byDesc["left the house"]
                val dayLeftHouse = leftHouseType != null && dayEntries.any { it.dataTypeId == leftHouseType.id }
                val prob = if (dayLeftHouse) 0.5f else 0.1f
                if (Random.nextFloat() < prob) {
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
