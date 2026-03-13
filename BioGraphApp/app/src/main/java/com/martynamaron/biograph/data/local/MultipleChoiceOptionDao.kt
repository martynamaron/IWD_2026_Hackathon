package com.martynamaron.biograph.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MultipleChoiceOptionDao {
    @Query("SELECT * FROM multiple_choice_options WHERE dataTypeId = :dataTypeId ORDER BY sortOrder")
    fun getOptionsForDataTypeFlow(dataTypeId: Long): Flow<List<MultipleChoiceOptionEntity>>

    @Query("SELECT * FROM multiple_choice_options WHERE dataTypeId = :dataTypeId ORDER BY sortOrder")
    suspend fun getOptionsForDataType(dataTypeId: Long): List<MultipleChoiceOptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(options: List<MultipleChoiceOptionEntity>)

    @Delete
    suspend fun delete(option: MultipleChoiceOptionEntity)

    @Query("DELETE FROM multiple_choice_options WHERE dataTypeId = :dataTypeId")
    suspend fun deleteAllForDataType(dataTypeId: Long)
}
