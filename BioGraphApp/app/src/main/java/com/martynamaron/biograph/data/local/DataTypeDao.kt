package com.martynamaron.biograph.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DataTypeDao {
    @Query("SELECT * FROM data_types ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<DataTypeEntity>>

    @Query("SELECT COUNT(*) FROM data_types")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(dataType: DataTypeEntity): Long

    @Update
    suspend fun update(dataType: DataTypeEntity)

    @Delete
    suspend fun delete(dataType: DataTypeEntity)

    @Query("SELECT * FROM data_types WHERE emoji = :emoji AND description = :description LIMIT 1")
    suspend fun findDuplicate(emoji: String, description: String): DataTypeEntity?

    @Query("SELECT * FROM data_types WHERE id = :id")
    suspend fun getById(id: Long): DataTypeEntity?

    @Query("SELECT * FROM data_types WHERE inputType != 'TOGGLE' ORDER BY createdAt DESC")
    fun getNonToggleTypes(): Flow<List<DataTypeEntity>>
}
