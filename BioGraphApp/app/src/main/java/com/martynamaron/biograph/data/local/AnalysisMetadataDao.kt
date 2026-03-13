package com.martynamaron.biograph.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisMetadataDao {
    @Query("SELECT * FROM analysis_metadata WHERE id = 0")
    suspend fun getMetadata(): AnalysisMetadataEntity?

    @Query("SELECT * FROM analysis_metadata WHERE id = 0")
    fun getMetadataFlow(): Flow<AnalysisMetadataEntity?>

    @Upsert
    suspend fun upsert(metadata: AnalysisMetadataEntity)
}
