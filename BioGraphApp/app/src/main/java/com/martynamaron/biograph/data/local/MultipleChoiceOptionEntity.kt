package com.martynamaron.biograph.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "multiple_choice_options",
    foreignKeys = [
        ForeignKey(
            entity = DataTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["dataTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["dataTypeId"])]
)
data class MultipleChoiceOptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dataTypeId: Long,
    val emoji: String,
    val label: String,
    val sortOrder: Int = 0
)
