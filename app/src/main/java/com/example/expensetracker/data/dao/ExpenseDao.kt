package com.example.expensetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.model.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses_table")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert()
    suspend fun insertExpense(expenseEntity: ExpenseEntity)

    
    @Update()
    suspend fun updateExpense(expenseEntity: ExpenseEntity)


        @Query("DELETE FROM expenses_table WHERE id = :expenseId")
        suspend fun deleteExpense(expenseId: Int)
}