package com.martynamaron.biograph.data.repository

import com.martynamaron.biograph.data.InputType
import com.martynamaron.biograph.data.local.DataTypeDao
import com.martynamaron.biograph.data.local.MultiChoiceSelectionDao
import com.martynamaron.biograph.data.local.MultiChoiceSelectionEntity
import com.martynamaron.biograph.data.local.MultipleChoiceOptionDao
import com.martynamaron.biograph.data.local.MultipleChoiceOptionEntity
import kotlinx.coroutines.flow.Flow

class MultipleChoiceRepository(
    private val optionDao: MultipleChoiceOptionDao,
    private val selectionDao: MultiChoiceSelectionDao,
    private val dataTypeDao: DataTypeDao? = null,
    private val dailyEntryRepository: DailyEntryRepository? = null
) {
    fun getOptionsFlow(dataTypeId: Long): Flow<List<MultipleChoiceOptionEntity>> =
        optionDao.getOptionsForDataTypeFlow(dataTypeId)

    suspend fun getOptions(dataTypeId: Long): List<MultipleChoiceOptionEntity> =
        optionDao.getOptionsForDataType(dataTypeId)

    suspend fun saveOptions(options: List<MultipleChoiceOptionEntity>) {
        optionDao.insertAll(options)
    }

    fun getSelections(date: String, dataTypeId: Long): Flow<List<MultiChoiceSelectionEntity>> =
        selectionDao.getSelectionsFlow(date, dataTypeId)

    suspend fun saveSelections(date: String, dataTypeId: Long, selectedOptionIds: Set<Long>) {
        selectionDao.deleteAllForDateAndType(date, dataTypeId)
        if (selectedOptionIds.isNotEmpty()) {
            val entities = selectedOptionIds.map { optionId ->
                MultiChoiceSelectionEntity(
                    date = date,
                    dataTypeId = dataTypeId,
                    optionId = optionId
                )
            }
            selectionDao.insertAll(entities)
        }
    }

    suspend fun countSelections(dataTypeId: Long): Int =
        selectionDao.countSelectionsForDataType(dataTypeId)

    suspend fun deleteAllSelectionsForType(dataTypeId: Long) {
        selectionDao.deleteAllForDataType(dataTypeId)
    }

    suspend fun deleteAllOptionsForType(dataTypeId: Long) {
        optionDao.deleteAllForDataType(dataTypeId)
    }

    suspend fun migrateDataTypeToMultipleChoice(
        dataTypeId: Long,
        options: List<MultipleChoiceOptionEntity>
    ) {
        val entryRepo = requireNotNull(dailyEntryRepository) {
            "DailyEntryRepository required for migration"
        }
        val dtDao = requireNotNull(dataTypeDao) {
            "DataTypeDao required for migration"
        }
        entryRepo.deleteAllForDataType(dataTypeId)
        selectionDao.deleteAllForDataType(dataTypeId)
        optionDao.deleteAllForDataType(dataTypeId)
        optionDao.insertAll(options)
        val existing = dtDao.getById(dataTypeId)
            ?: throw IllegalArgumentException("Data type $dataTypeId not found")
        dtDao.update(existing.copy(inputType = InputType.MULTIPLE_CHOICE.name))
    }

    suspend fun getAllSelections(): List<MultiChoiceSelectionEntity> =
        selectionDao.getAllSelections()

    suspend fun deleteAllSelections() {
        selectionDao.deleteAll()
    }

    suspend fun deleteAllOptions() {
        optionDao.deleteAll()
    }
}
