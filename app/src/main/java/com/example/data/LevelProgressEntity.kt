package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelId: Int,
    val gameType: String, // "WORD_CONNECT" or "CROSSWORD"
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val foundWords: String = "",
    val revealedIndices: String = ""
)
