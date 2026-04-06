package com.example.expensetracker.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.Added
import com.example.expensetracker.ui.theme.Greenn
import com.example.expensetracker.viewmodel.HomeViewModel
import com.example.expensetracker.viewmodel.HomeViewModelFactory
import com.example.expensetracker.widget.ExpenseTextView
import java.text.SimpleDateFormat
import java.util.Locale

enum class TransactionSortOption(val label: String) {
    NEWEST_FIRST("Newest first"),
    OLDEST_FIRST("Oldest first"),
    HIGH_TO_LOW("High to low"),
    LOW_TO_HIGH("Low to high")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(navController: NavController) {
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(context))
    val expensesState by homeViewModel.expenses.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf(TransactionSortOption.NEWEST_FIRST) }
    var showSortMenu by remember { mutableStateOf(false) }

    fun parseDateToMillis(dateStr: String): Long {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    val filteredBySearch = expensesState.filter { item ->
        if (searchQuery.isBlank()) true
        else {
            item.title.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true) ||
                item.date.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredByType = filteredBySearch.filter { item ->
        when (selectedType) {
            "Income" -> item.type.equals("Income", ignoreCase = true)
            "Expense" -> item.type.equals("Expense", ignoreCase = true)
            else -> true
        }
    }

    val allTransactions = when (selectedSort) {
        TransactionSortOption.NEWEST_FIRST -> filteredByType.sortedByDescending { parseDateToMillis(it.date) }
        TransactionSortOption.OLDEST_FIRST -> filteredByType.sortedBy { parseDateToMillis(it.date) }
        TransactionSortOption.HIGH_TO_LOW -> filteredByType.sortedByDescending { it.amount }
        TransactionSortOption.LOW_TO_HIGH -> filteredByType.sortedBy { it.amount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ExpenseTextView(
                        text = "All Transactions",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Added)
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color(0xFFF6F8FB)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    placeholder = { ExpenseTextView(text = "Search transactions", fontSize = 13.sp, color = Color(0xFF8D97A8)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF7B8698)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Greenn,
                        unfocusedBorderColor = Color(0xFFCED5E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChipLike(
                            text = "All",
                            selected = selectedType == "All",
                            onClick = { selectedType = "All" }
                        )
                        FilterChipLike(
                            text = "Income",
                            selected = selectedType == "Income",
                            onClick = { selectedType = "Income" }
                        )
                        FilterChipLike(
                            text = "Expense",
                            selected = selectedType == "Expense",
                            onClick = { selectedType = "Expense" }
                        )
                    }

                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable { showSortMenu = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Sort By",
                                tint = Greenn,
                                modifier = Modifier.size(18.dp)
                            )
                            ExpenseTextView(
                                text = "Filter",
                                fontSize = 13.sp,
                                color = Greenn,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            TransactionSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        ExpenseTextView(
                                            text = option.label,
                                            fontSize = 14.sp,
                                            fontWeight = if (selectedSort == option) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (selectedSort == option) Greenn else Color(0xFF334155)
                                        )
                                    },
                                    onClick = {
                                        selectedSort = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (allTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ExpenseTextView(
                            text = "No transactions found",
                            fontSize = 16.sp,
                            color = Color(0xFF6D7686)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF6F8FB)),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(allTransactions) { item ->
                            TransactionItem(
                                item = item,
                                icon = if (item.type.equals("Income", ignoreCase = true)) R.drawable.profit else R.drawable.loss,
                                color = if (item.type.equals("Income", ignoreCase = true)) Added else Color(0xFFE45757),
                                onUpdate = { updatedItem -> homeViewModel.updateExpense(updatedItem) },
                                onDelete = { homeViewModel.deleteExpense(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipLike(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Greenn.copy(alpha = 0.16f) else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        ExpenseTextView(
            text = text,
            fontSize = 13.sp,
            color = if (selected) Greenn else Color(0xFF6B778C),
            fontWeight = FontWeight.SemiBold
        )
    }
}
