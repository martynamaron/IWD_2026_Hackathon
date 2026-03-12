package com.martynamaron.biograph.data.repository

import com.martynamaron.biograph.data.local.DataTypeDao
import com.martynamaron.biograph.data.local.DataTypeEntity
import kotlinx.coroutines.flow.Flow

class DataTypeRepository(private val dao: DataTypeDao) {

    fun getAllFlow(): Flow<List<DataTypeEntity>> = dao.getAllFlow()

    suspend fun getCount(): Int = dao.getCount()

    suspend fun insert(dataType: DataTypeEntity): Result<Long> {
        val duplicate = dao.findDuplicate(dataType.emoji, dataType.description)
        if (duplicate != null) {
            return Result.failure(IllegalArgumentException("Data type already exists"))
        }
        return Result.success(dao.insert(dataType))
    }

    suspend fun update(dataType: DataTypeEntity): Result<Unit> {
        val duplicate = dao.findDuplicate(dataType.emoji, dataType.description)
        if (duplicate != null && duplicate.id != dataType.id) {
            return Result.failure(IllegalArgumentException("Data type already exists"))
        }
        dao.update(dataType)
        return Result.success(Unit)
    }

    suspend fun delete(dataType: DataTypeEntity) {
        dao.delete(dataType)
    }
}
