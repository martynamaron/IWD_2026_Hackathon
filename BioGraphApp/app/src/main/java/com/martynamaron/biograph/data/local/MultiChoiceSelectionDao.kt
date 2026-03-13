package com.martynamaron.biograph.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MultiChoiceSelectionDao {
    @Query("SELECT * FROM multi_choice_selections WHERE date = :date AND dataTypeId = :dataTypeId")
    fun getSelectionsFlow(date: String, dataTypeId: Long): Flow<List<MultiChoiceSelectionEntity>>

    @Query("SELECT DISTINCT date FROM multi_choice_selections WHERE date BETWEEN :startDate AND :endDate")
    fun getDatesWithSelectionsFlow(startDate: String, endDate: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM multi_choice_selections WHERE dataTypeId = :dataTypeId")
    suspend fun countSelectionsForDataType(dataTypeId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(selections: List<MultiChoiceSelectionEntity>)

    @Query("DELETE FROM multi_choice_selections WHERE date = :date AND dataTypeId = :dataTypeId")
    suspend fun deleteAllForDateAndType(date: String, dataTypeId: Long)

    @Query("DELETE FROM multi_choice_selections WHERE dataTypeId = :dataTypeId")
    suspend fun deleteAllForDataType(dataTypeId: Long)

    @Query("SELECT COUNT(*) FROM multi_choice_selections")
    suspend fun getTotalCount(): Int

    @Query("SELECT * FROM multi_choice_selections ORDER BY date")
    suspend fun getAllSelections(): List<MultiChoiceSelectionEntity>
}
