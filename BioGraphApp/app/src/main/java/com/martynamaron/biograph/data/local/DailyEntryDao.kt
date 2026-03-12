package com.martynamaron.biograph.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {
    @Query("SELECT * FROM daily_entries WHERE date = :date")
    fun getEntriesForDateFlow(date: String): Flow<List<DailyEntryEntity>>

    @Query("SELECT DISTINCT date FROM daily_entries WHERE date BETWEEN :startDate AND :endDate")
    fun getDatesWithEntriesFlow(startDate: String, endDate: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DailyEntryEntity>)

    @Query("DELETE FROM daily_entries WHERE date = :date")
    suspend fun deleteAllForDate(date: String)

    @Query("DELETE FROM daily_entries WHERE dataTypeId NOT IN (SELECT id FROM data_types)")
    suspend fun deleteOrphanedEntries()
}
