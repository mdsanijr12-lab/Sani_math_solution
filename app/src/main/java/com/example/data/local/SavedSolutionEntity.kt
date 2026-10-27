package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_solutions")
data class SavedSolutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionText: String,
    val topic: String,
    val shortAnswer: String,
    val fullSolutionJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
