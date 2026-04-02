package com.example.expensetracker.data.dao


class Repo(private val expenseDao: ExpenseDao) {
    suspend fun deleteExpense(expenseId: Int) {
        expenseDao.deleteExpense(expenseId)
    }
}

