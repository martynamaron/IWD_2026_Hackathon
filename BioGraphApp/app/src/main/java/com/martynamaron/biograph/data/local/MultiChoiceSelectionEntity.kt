package com.martynamaron.biograph.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "multi_choice_selections",
    foreignKeys = [
        ForeignKey(
            entity = DataTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["dataTypeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MultipleChoiceOptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["optionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["date", "dataTypeId", "optionId"], unique = true),
        Index(value = ["dataTypeId"]),
        Index(value = ["optionId"])
    ]
)
data class MultiChoiceSelectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val dataTypeId: Long,
    val optionId: Long
)
