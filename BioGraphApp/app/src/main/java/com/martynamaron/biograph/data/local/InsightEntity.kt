package com.martynamaron.biograph.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "insights",
    foreignKeys = [
        ForeignKey(
            entity = DataTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["dataType1Id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DataTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["dataType2Id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["dataType1Id"]),
        Index(value = ["dataType2Id"]),
        Index(value = ["computedAt"])
    ]
)
data class InsightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dataType1Id: Long,
    val dataType2Id: Long,
    val optionId: Long? = null,
    val correlationCoefficient: Double,
    val correlationMethod: String,
    val insightText: String,
    val sampleSize: Int,
    val computedAt: Long = System.currentTimeMillis()
)
