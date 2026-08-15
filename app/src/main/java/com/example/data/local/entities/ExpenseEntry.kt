package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val item: String,
    val amount: Double,
    val category: String = "General",
    val currency: String = "৳",
    val date: Long = System.currentTimeMillis()
)
