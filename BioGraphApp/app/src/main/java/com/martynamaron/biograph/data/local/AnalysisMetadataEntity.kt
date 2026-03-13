package com.martynamaron.biograph.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_metadata")
data class AnalysisMetadataEntity(
    @PrimaryKey val id: Int = 0,
    val lastAnalysisTimestamp: Long = 0,
    val lastDataCount: Int = 0
)
