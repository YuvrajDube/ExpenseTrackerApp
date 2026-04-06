package com.example.expensetracker.data.dao

import com.example.expensetracker.data.model.ExpenseEntity

class Repo(private val expenseDao: ExpenseDao) {
    suspend fun deleteExpense(expenseId: Int) {
        expenseDao.deleteExpense(expenseId)
    }
    suspend fun updateExpense(expenseEntity: ExpenseEntity) {
        expenseDao.updateExpense(expenseEntity)
    }
}
