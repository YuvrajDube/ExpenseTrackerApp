package com.example.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses_table")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val title: String,
    val amount: Double,
    val date: String,
    val category: String,
    val type: String
)

