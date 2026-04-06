package com.example.expensetracker.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.Added
import com.example.expensetracker.viewmodel.HomeViewModel
import com.example.expensetracker.viewmodel.HomeViewModelFactory
import com.example.expensetracker.widget.ExpenseTextView
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(navController: NavController, categoryName: String) {
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(context))
    val expensesState by homeViewModel.expenses.collectAsState(initial = emptyList())

    // helper: parse date
    fun parseDateToMillis(dateStr: String): Long {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr)?.time ?: 0L
        } catch (_: Exception) { 0L }
    }

    val categoryExpenses = expensesState
        .filter { it.category == categoryName && it.type.equals("Expense", ignoreCase = true) }
        .sortedByDescending { parseDateToMillis(it.date) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ExpenseTextView(
                        text = categoryName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Added)
            )
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (categoryExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ExpenseTextView(text = "No expenses found for $categoryName", fontSize = 16.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    items(categoryExpenses) { item ->
                        TransactionItem(
                            item = item,
                            icon = R.drawable.loss,
                            color = Color.Red,
                            onUpdate = { updatedItem -> homeViewModel.updateExpense(updatedItem) },
                            onDelete = { homeViewModel.deleteExpense(item.id) }
                        )
                    }
                }
            }
        }
    }
}

