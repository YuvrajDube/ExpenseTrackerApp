package com.example.expensetracker.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.ExpenseDatabase
import com.example.expensetracker.data.dao.ExpenseDao
import com.example.expensetracker.data.dao.Repo
import com.example.expensetracker.data.model.ExpenseEntity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class HomeViewModel(private val dao: ExpenseDao, private val repo: Repo) : ViewModel() {
    val expenses = dao.getAllExpenses()

    fun getSummary(list: List<ExpenseEntity>): Triple<String, String, String> {
        var income = 0.0
        var expense = 0.0

            list.forEach {
                if (it.type == "Income") {
                    income += it.amount
                } else {
                    expense += it.amount
                }
            }

        val balance = income - expense
            return Triple(
                "$ %.2f".format(balance),
                "$ %.2f".format(income),
                "$ %.2f".format(expense)
            )
    }

    fun deleteExpense(id: Int) {
        viewModelScope.launch {
            Log.d("DeleteFunction", "Deleting expense with ID: $id")
            repo.deleteExpense(id)
        }
    }
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val dao = ExpenseDatabase.getDatabase(context).expenseDao()
            return HomeViewModel(dao,Repo(dao)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
