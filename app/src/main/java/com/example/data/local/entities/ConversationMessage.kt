package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val response: String,
    val intentAction: String? = null,
    val actionPayload: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "en",
    val isSuccess: Boolean = true
)
