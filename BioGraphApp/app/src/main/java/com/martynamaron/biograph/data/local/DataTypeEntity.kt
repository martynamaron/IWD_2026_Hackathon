package com.martynamaron.biograph.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.martynamaron.biograph.data.InputType

@Entity(
    tableName = "data_types",
    indices = [Index(value = ["emoji", "description"], unique = true)]
)
data class DataTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emoji: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "TOGGLE")
    val inputType: String = InputType.TOGGLE.name
)
