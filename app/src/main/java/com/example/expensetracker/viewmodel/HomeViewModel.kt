@file:Suppress("RemoveRedundantQualifierName")

package com.example.expensetracker.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.ExpenseDatabase
import com.example.expensetracker.data.dao.ExpenseDao
import com.example.expensetracker.data.dao.Repo
import com.example.expensetracker.data.model.ExpenseEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Suppress("unused")
class HomeViewModel(private val dao: ExpenseDao, private val repo: Repo) : ViewModel() {
    val expenses = dao.getAllExpenses()
    // expose distinct categories directly from the DAO
    val categories = dao.getDistinctCategories()

    @SuppressLint("SuspiciousIndentation")
    fun getSummary(list: List<ExpenseEntity>): Triple<String, String, String> {
        var income = 0.0
        var expense = 0.0

            list.forEach {
                if (it.type.equals("Income", ignoreCase = true)) {
                    income += it.amount
                } else {
                    expense += it.amount
                }
            }

        val balance = income - expense
            return Triple(
                "₹ %.2f".format(balance),
                "₹ %.2f".format(income),
                "₹ %.2f".format(expense)
            )
    }

    fun updateExpense(expenseEntity: ExpenseEntity) {
        viewModelScope.launch {
            Log.d("UpdateFunction", "Updating expense with ID: ${expenseEntity.id}")
            repo.updateExpense(expenseEntity)
        }
    }

    fun deleteExpense(id: Int) {
        viewModelScope.launch {
            Log.d("DeleteFunction", "Deleting expense with ID: $id")
            repo.deleteExpense(id)
        }
    }
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // Attempt to get a real DAO; if anything goes wrong (e.g. DB init failure),
            // provide a safe fallback DAO implementation that returns empty flows and no-op suspend functions.
            val dao: ExpenseDao = try {
                ExpenseDatabase.getDatabase(context).expenseDao()
            } catch (e: Exception) {
                // Fallback implementation
                Log.e("HomeViewModelFactory", "Failed to get ExpenseDao from DB, using fallback.", e)
                object : ExpenseDao {
                    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = flowOf(emptyList())
                    override fun getDistinctCategories(): Flow<List<String>> = flowOf(emptyList())
                    override suspend fun insertExpense(expenseEntity: ExpenseEntity) {}
                    override suspend fun updateExpense(expenseEntity: ExpenseEntity) {}
                    override suspend fun deleteExpense(expenseId: Int) {}
                }
            }

            return HomeViewModel(dao, Repo(dao)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
