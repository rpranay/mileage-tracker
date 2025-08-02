package com.example.myapplication

import androidx.room.PrimaryKey
import androidx.room.Entity
import java.util.Date


@Entity(tableName = "mileage_entries")
data class MileageEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-generated ID
    val miles: Int,
    val date: Date
)