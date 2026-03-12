package com.martynamaron.biograph.util

import com.martynamaron.biograph.data.local.DailyEntryEntity
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.data.repository.DailyEntryRepository
import com.martynamaron.biograph.data.repository.DataTypeRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class MockDataGenerator(
    private val dataTypeRepository: DataTypeRepository,
    private val dailyEntryRepository: DailyEntryRepository
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
                // ~40-60% random toggle probability
                if (Random.nextFloat() < 0.5f) {
                    entries.add(DailyEntryEntity(date = dateString, dataTypeId = dataType.id))
                }
            }
            date = date.plusDays(1)
        }

        // Insert all in batches by date to use replace semantics
        entries.groupBy { it.date }.forEach { (dateString, dayEntries) ->
            dailyEntryRepository.replaceEntriesForDate(dateString, dayEntries)
        }
    }
}
