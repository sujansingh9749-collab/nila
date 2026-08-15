package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_macros")
data class VoiceMacro(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val triggerPhrase: String,
    val description: String,
    val stepsJson: String,
    val isEnabled: Boolean = true,
    val iconName: String = "routine"
)
