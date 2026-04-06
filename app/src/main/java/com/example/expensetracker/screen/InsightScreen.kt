package com.example.expensetracker.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.ExpenseEntity
import com.example.expensetracker.ui.theme.Added
import com.example.expensetracker.ui.theme.Greenn
import com.example.expensetracker.util.DEFAULT_CATEGORIES
import com.example.expensetracker.viewmodel.HomeViewModel
import com.example.expensetracker.viewmodel.HomeViewModelFactory
import com.example.expensetracker.widget.ExpenseTextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightScreen(navController: NavController) {
    var expandedIndex by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val expenses: List<ExpenseEntity> = if (isPreview) {
        remember {
            listOf(
                ExpenseEntity(id = 1, title = "Coffee", amount = 120.0, date = "01/04/2026", category = "Food", type = "Expense"),
                ExpenseEntity(id = 2, title = "Salary", amount = 5000.0, date = "01/04/2026", category = "Salary", type = "Income"),
                ExpenseEntity(id = 3, title = "Groceries", amount = 300.0, date = "15/03/2026", category = "Food", type = "Expense"),
                ExpenseEntity(id = 4, title = "Transport", amount = 180.0, date = "18/03/2026", category = "Travel", type = "Expense")
            )
        }
    } else {
        val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(context))
        val expensesState by homeViewModel.expenses.collectAsState(initial = emptyList())
        expensesState
    }

    fun monthKey(dateStr: String): String {
        return try {
            val fmtIn = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fmtOut = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            fmtOut.format(fmtIn.parse(dateStr) ?: return "")
        } catch (_: Exception) {
            ""
        }
    }

    fun lastNMonthsLabels(n: Int): List<String> {
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return (n - 1 downTo 0).map { i ->
            val c = cal.clone() as Calendar
            c.add(Calendar.MONTH, -i)
            fmt.format(c.time)
        }
    }

    val expenseOnly = remember(expenses) { expenses.filter { it.type.equals("Expense", ignoreCase = true) } }
    val incomeOnly = remember(expenses) { expenses.filter { it.type.equals("Income", ignoreCase = true) } }
    val currentMonth = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time) }
    val thisMonthSpend = remember(expenses) { expenseOnly.filter { monthKey(it.date) == currentMonth }.sumOf { it.amount } }

    val categoryTotals = remember(expenses) {
        val source = if (expenseOnly.isEmpty()) {
            DEFAULT_CATEGORIES.associateWith { 0.0 }
        } else {
            expenseOnly.groupBy { it.category.ifBlank { "Other" } }.mapValues { it.value.sumOf { e -> e.amount } }
        }
        source.entries.sortedByDescending { it.value }
    }

    val topCategory = categoryTotals.firstOrNull()?.key ?: "N/A"
    val totalIncome = incomeOnly.sumOf { it.amount }
    val totalExpense = expenseOnly.sumOf { it.amount }
    val savingsRate = if (totalIncome <= 0.0) 0 else (((totalIncome - totalExpense) / totalIncome) * 100).toInt().coerceIn(-999, 999)

    val labels = listOf(
        "Highest spending category",
        "Monthly trend",
        "Spending by month",
        "Frequent transaction type"
    )

    val fabScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ExpenseTextView(
                        text = "Insights",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Greenn)
            )
        },
        bottomBar = { BottomBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { fabScope.launch { navController.navigate("/aiChat") } },
//                containerColor = Greenn,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.gemini_color), contentDescription = "AI Chat", tint = Color.Unspecified,modifier = Modifier.size(40.dp))
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color(0xFFF6F8FB)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                item {
                    SummaryHeroCard(
                        thisMonthSpend = thisMonthSpend,
                        topCategory = topCategory,
                        savingsRate = savingsRate
                    )
                }

                items(labels.indices.toList()) { index ->
                    val isExpanded = expandedIndex == index
                    InsightAccordionItem(
                        title = labels[index],
                        expanded = isExpanded,
                        onClick = { expandedIndex = if (isExpanded) -1 else index }
                    ) {
                        when (index) {
                            0 -> HighestSpendingSection(
                                categoryTotals = categoryTotals,
                                onCategoryClick = { category -> navController.navigate("/categoryDetail/$category") }
                            )

                            1 -> MonthlyTrendSection(
                                expenses = expenses,
                                lastNMonthsLabels = ::lastNMonthsLabels,
                                monthKey = ::monthKey
                            )

                            2 -> SpendingByMonthSection(
                                expenses = expenses,
                                lastNMonthsLabels = ::lastNMonthsLabels,
                                monthKey = ::monthKey,
                                onMonthClick = { month -> navController.navigate("/monthDetail/$month") }
                            )

                            else -> FrequentTransactionTypeSection(
                                incomeCount = incomeOnly.size,
                                expenseCount = expenseOnly.size,
                                onTypeClick = { type -> navController.navigate("/transactionTypeDetail/$type") }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SummaryHeroCard(thisMonthSpend: Double, topCategory: String, savingsRate: Int) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF6FD9CC), Greenn)
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ExpenseTextView(text = "Your financial snapshot", fontSize = 13.sp, color = Color(0xFFEAFBF8))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricPill(label = "This month", value = "₹${"%.0f".format(thisMonthSpend)}")
                    MetricPill(label = "Top category", value = topCategory)
                    MetricPill(label = "Savings", value = "$savingsRate%")
                }
            }
        }
    }
}

@Composable
private fun MetricPill(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExpenseTextView(text = label, fontSize = 11.sp, color = Color(0xFFEAFBF8))
        ExpenseTextView(text = value, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InsightAccordionItem(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "arrow_rotation"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 6.dp else 2.dp),
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExpenseTextView(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAF4F2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Greenn,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFD))
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun HighestSpendingSection(
    categoryTotals: List<Map.Entry<String, Double>>,
    onCategoryClick: (String) -> Unit
) {
    if (categoryTotals.isEmpty()) {
        ExpenseTextView(text = "No categories found", fontSize = 14.sp, color = Color(0xFF6C788C))
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categoryTotals.take(6).forEachIndexed { index, entry ->
            val amount = entry.value
            val leading = if (index == 0) "🔥" else "•"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategoryClick(entry.key) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpenseTextView(text = "$leading ${entry.key}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    ExpenseTextView(text = "₹ ${"%.2f".format(amount)}", fontSize = 14.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MonthlyTrendSection(
    expenses: List<ExpenseEntity>,
    lastNMonthsLabels: (Int) -> List<String>,
    monthKey: (String) -> String
) {
    val months = remember { lastNMonthsLabels(4) }
    val totalsMap = remember(expenses) {
        val map = months.associateWith { 0.0 }.toMutableMap()
        expenses.filter { it.type.equals("Expense", ignoreCase = true) }.forEach { e ->
            val key = monthKey(e.date)
            if (map.containsKey(key)) map[key] = map[key]!! + e.amount
        }
        map.toMap()
    }

    val maxTotal = totalsMap.values.maxOrNull() ?: 0.0
    val maxHeight = 150.dp

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            ExpenseTextView(
                text = "Total ₹ ${"%.0f".format(totalsMap.values.sum())}",
                fontSize = 12.sp,
                color = Color(0xFF617086),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            months.forEachIndexed { index, month ->
                val value = totalsMap[month] ?: 0.0
                val fraction = if (maxTotal <= 0.0) 0f else (value / maxTotal).toFloat()
                val animated by animateFloatAsState(targetValue = fraction, label = "bar_$index")

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ExpenseTextView(text = "₹${"%.0f".format(value)}", fontSize = 10.sp, color = Color(0xFF677589))
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .height(maxHeight)
                            .width(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE9EEF6)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(maxHeight * animated)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (index == months.lastIndex) Greenn else Added)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ExpenseTextView(text = month.substringBefore(' '), fontSize = 11.sp, color = Color(0xFF4D5A6B), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SpendingByMonthSection(
    expenses: List<ExpenseEntity>,
    lastNMonthsLabels: (Int) -> List<String>,
    monthKey: (String) -> String,
    onMonthClick: (String) -> Unit
) {
    val months = remember { lastNMonthsLabels(5) }
    val totals = remember(expenses) {
        val map = months.associateWith { 0.0 }.toMutableMap()
        expenses.filter { it.type.equals("Expense", ignoreCase = true) }.forEach { e ->
            val key = monthKey(e.date)
            if (map.containsKey(key)) map[key] = map[key]!! + e.amount
        }
        map.toMap()
    }

    val maxTotal = totals.values.maxOrNull() ?: 1.0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        months.forEach { month ->
            val amount = totals[month] ?: 0.0
            val fraction = (amount / maxTotal).toFloat().coerceIn(0f, 1f)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMonthClick(month) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ExpenseTextView(text = month, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        ExpenseTextView(text = "₹ ${"%.2f".format(amount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE9EEF6))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Added)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FrequentTransactionTypeSection(
    incomeCount: Int,
    expenseCount: Int,
    onTypeClick: (String) -> Unit
) {
    val total = (incomeCount + expenseCount).coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TypeFrequencyRow(
            title = "Income",
            count = incomeCount,
            percent = (incomeCount * 100) / total,
            tint = Added,
            onClick = { onTypeClick("Income") }
        )
        TypeFrequencyRow(
            title = "Expense",
            count = expenseCount,
            percent = (expenseCount * 100) / total,
            tint = Color(0xFFED6A5A),
            onClick = { onTypeClick("Expense") }
        )
    }
}

@Composable
private fun TypeFrequencyRow(
    title: String,
    count: Int,
    percent: Int,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpenseTextView(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                ExpenseTextView(text = "$count txns • $percent%", fontSize = 13.sp, color = Color(0xFF647284))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                drawRoundRect(
                    color = Color(0xFFE9EEF6),
                    cornerRadius = CornerRadius(20f, 20f)
                )
                drawRoundRect(
                    color = tint,
                    size = size.copy(width = size.width * (percent / 100f)),
                    cornerRadius = CornerRadius(20f, 20f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InsightScreenPreview() {
    InsightScreen(navController = rememberNavController())
}
