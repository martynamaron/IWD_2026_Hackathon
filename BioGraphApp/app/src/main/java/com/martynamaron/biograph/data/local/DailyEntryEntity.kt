package com.martynamaron.biograph.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_entries",
    foreignKeys = [
        ForeignKey(
            entity = DataTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["dataTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["date", "dataTypeId"], unique = true),
        Index(value = ["dataTypeId"])
    ]
)
data class DailyEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val dataTypeId: Long
)
