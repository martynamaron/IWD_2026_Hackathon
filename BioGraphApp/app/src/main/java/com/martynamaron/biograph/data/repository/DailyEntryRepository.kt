package com.martynamaron.biograph.data.repository

import com.martynamaron.biograph.data.local.DailyEntryDao
import com.martynamaron.biograph.data.local.DailyEntryEntity
import kotlinx.coroutines.flow.Flow

class DailyEntryRepository(private val dao: DailyEntryDao) {

    fun getEntriesForDate(date: String): Flow<List<DailyEntryEntity>> =
        dao.getEntriesForDateFlow(date)

    fun getDatesWithEntries(startDate: String, endDate: String): Flow<List<String>> =
        dao.getDatesWithEntriesFlow(startDate, endDate)

    suspend fun replaceEntriesForDate(date: String, entries: List<DailyEntryEntity>) {
        dao.deleteAllForDate(date)
        if (entries.isNotEmpty()) {
            dao.insertAll(entries)
        }
    }

    suspend fun insertAll(entries: List<DailyEntryEntity>) {
        dao.insertAll(entries)
    }

    suspend fun deleteAllForDate(date: String) {
        dao.deleteAllForDate(date)
    }
}
