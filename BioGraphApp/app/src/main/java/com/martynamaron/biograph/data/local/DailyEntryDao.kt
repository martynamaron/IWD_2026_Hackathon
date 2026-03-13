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

    @Query("SELECT * FROM daily_entries WHERE date = :date")
    suspend fun getEntriesForDate(date: String): List<DailyEntryEntity>

    @Query("SELECT DISTINCT date FROM daily_entries WHERE date BETWEEN :startDate AND :endDate")
    fun getDatesWithEntriesFlow(startDate: String, endDate: String): Flow<List<String>>

    @Query("""
        SELECT DISTINCT date FROM daily_entries WHERE date BETWEEN :startDate AND :endDate
        UNION
        SELECT DISTINCT date FROM multi_choice_selections WHERE date BETWEEN :startDate AND :endDate
    """)
    fun getDatesWithAnyEntryFlow(startDate: String, endDate: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DailyEntryEntity>)

    @Query("DELETE FROM daily_entries WHERE date = :date")
    suspend fun deleteAllForDate(date: String)

    @Query("DELETE FROM daily_entries WHERE dataTypeId NOT IN (SELECT id FROM data_types)")
    suspend fun deleteOrphanedEntries()

    @Query("SELECT COUNT(*) FROM daily_entries WHERE dataTypeId = :dataTypeId")
    suspend fun countEntriesForDataType(dataTypeId: Long): Int

    @Query("DELETE FROM daily_entries WHERE dataTypeId = :dataTypeId")
    suspend fun deleteAllForDataType(dataTypeId: Long)

    @Query("SELECT COUNT(*) FROM daily_entries")
    suspend fun getTotalCount(): Int

    @Query("SELECT * FROM daily_entries ORDER BY date")
    suspend fun getAllEntries(): List<DailyEntryEntity>
}
