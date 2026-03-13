package com.martynamaron.biograph.data.repository

import com.martynamaron.biograph.data.local.DailyEntryDao
import com.martynamaron.biograph.data.local.DailyEntryEntity
import kotlinx.coroutines.flow.Flow

class DailyEntryRepository(private val dao: DailyEntryDao) {

    fun getEntriesForDate(date: String): Flow<List<DailyEntryEntity>> =
        dao.getEntriesForDateFlow(date)

    suspend fun getEntriesForDateSnapshot(date: String): List<DailyEntryEntity> =
        dao.getEntriesForDate(date)

    fun getDatesWithEntries(startDate: String, endDate: String): Flow<List<String>> =
        dao.getDatesWithEntriesFlow(startDate, endDate)

    fun getDatesWithAnyEntry(startDate: String, endDate: String): Flow<List<String>> =
        dao.getDatesWithAnyEntryFlow(startDate, endDate)

    suspend fun replaceEntriesForDate(date: String, entries: List<DailyEntryEntity>) {
        dao.deleteAllForDate(date)
        if (entries.isNotEmpty()) {
            dao.insertAll(entries)
        }
    }

    suspend fun saveEntries(
        date: String,
        toggleIds: Set<Long>,
        scaleValues: Map<Long, Int>
    ) {
        dao.deleteAllForDate(date)
        val toggleEntries = toggleIds.map { dataTypeId ->
            DailyEntryEntity(date = date, dataTypeId = dataTypeId)
        }
        val scaleEntries = scaleValues.map { (dataTypeId, value) ->
            DailyEntryEntity(date = date, dataTypeId = dataTypeId, scaleValue = value)
        }
        val allEntries = toggleEntries + scaleEntries
        if (allEntries.isNotEmpty()) {
            dao.insertAll(allEntries)
        }
    }

    suspend fun insertAll(entries: List<DailyEntryEntity>) {
        dao.insertAll(entries)
    }

    suspend fun deleteAllForDate(date: String) {
        dao.deleteAllForDate(date)
    }

    suspend fun countEntriesForDataType(dataTypeId: Long): Int =
        dao.countEntriesForDataType(dataTypeId)

    suspend fun deleteAllForDataType(dataTypeId: Long) {
        dao.deleteAllForDataType(dataTypeId)
    }
}
