package com.example.expensetracker.screen

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.R
import com.example.expensetracker.data.model.ExpenseEntity
import com.example.expensetracker.ui.theme.Added
import com.example.expensetracker.ui.theme.Greenn
import com.example.expensetracker.viewmodel.HomeViewModel
import com.example.expensetracker.viewmodel.HomeViewModelFactory
import com.example.expensetracker.widget.ExpenseTextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current))
    val state by viewModel.expenses.collectAsState(initial = emptyList())

    var selectedTimeRange by remember { mutableStateOf("This Month") }
    val timeRanges = listOf("This Month", "Last Month", "All Time")

    val currentMonthStr = remember { SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time) }
    val lastMonthStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(cal.time)
    }

    val filteredState = remember(state, selectedTimeRange) {
        when (selectedTimeRange) {
            "This Month" -> state.filter { it.date.endsWith(currentMonthStr) }
            "Last Month" -> state.filter { it.date.endsWith(lastMonthStr) }
            else -> state
        }
    }

    fun parseDateToMillis(dateStr: String): Long {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    val (balance, income, expenses) = viewModel.getSummary(filteredState)
    val allTransactions = filteredState.sortedByDescending { parseDateToMillis(it.date) }
    val recentTransactions = allTransactions.take(3)

    val allExpenseTransactions = allTransactions.filter { it.type.equals("Expense", ignoreCase = true) }
    val expenseItemsForChart = allExpenseTransactions

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("ExpenseTrackerPrefs", Context.MODE_PRIVATE)

    var goalKeyStr = ""
    if (selectedTimeRange == "This Month") {
        goalKeyStr = "MONTHLY_GOAL_$currentMonthStr"
    } else if (selectedTimeRange == "Last Month") {
        goalKeyStr = "MONTHLY_GOAL_$lastMonthStr"
    }

    val goalStr = prefs.getString(goalKeyStr, "0") ?: "0"
    val goal = goalStr.toDoubleOrNull() ?: 0.0

    val balanceDouble = balance.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0

    // Calculate progress towards saving goal based on balance
    val goalProgress = if (goal > 0) (balanceDouble / goal).toFloat() else 0f

    Scaffold(
        bottomBar = { BottomBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("/add") },
                containerColor = Greenn,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add expense", tint = Color.White)
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 10.dp)
            ) {
                item {
                    HeroSection(
                        balance = balance,
                        income = income,
                        expenses = expenses,
                        selectedTimeRange = selectedTimeRange,
                        timeRanges = timeRanges,
                        onTimeRangeSelected = { selectedTimeRange = it },
                        onGoalIconClick = {navController.navigate("/savingsGoal")}
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExpenseTextView(
                            text = "Recent Transactions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        ExpenseTextView(
                            text = "View All",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Greenn,
                            modifier = Modifier.clickable { navController.navigate("/allTransactions") }
                        )
                    }
                }

                if (recentTransactions.isEmpty()) {
                    item { EmptyTransactionsState() }
                } else {
                    items(recentTransactions) { item ->
                        TransactionItem(
                            item = item,
                            icon = if (item.type.equals("Income", ignoreCase = true)) R.drawable.profit else R.drawable.loss,
                            color = if (item.type.equals("Income", ignoreCase = true)) Added else Color(0xFFE45757),
                            onUpdate = { updatedItem -> viewModel.updateExpense(updatedItem) },
                            onDelete = { viewModel.deleteExpense(item.id) }
                        )
                    }
                }

                item {
                    ProgressTrackerCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        progress = goalProgress,
                        balance = balanceDouble,
                        goal = goal,
                        selectedTimeRange = selectedTimeRange
                    )
                }

                item {
                    PieChartCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        items = expenseItemsForChart
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
    balance: String,
    income: String,
    expenses: String,
    selectedTimeRange: String,
    timeRanges: List<String>,
    onTimeRangeSelected: (String) -> Unit,
    onGoalIconClick: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("ExpenseTrackerPrefs", Context.MODE_PRIVATE)
    val userName = prefs.getString("USER_NAME", "Yuvraj Dube") ?: "Yuvraj Dube"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF6BD6C8), Greenn)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 14.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    ExpenseTextView(text = "Good afternoon,", fontSize = 14.sp, color = Color.White)
                    ExpenseTextView(
                        text = userName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = onGoalIconClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Set Monthly Goal",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        CardItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter),
            balance = balance,
            income = income,
            expenses = expenses,
            selectedTimeRange = selectedTimeRange,
            timeRanges = timeRanges,
            onTimeRangeSelected = onTimeRangeSelected
        )
    }
}

@Composable
fun CardItem(
    modifier: Modifier = Modifier,
    balance: String,
    income: String,
    expenses: String,
    selectedTimeRange: String,
    timeRanges: List<String>,
    onTimeRangeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Greenn),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpenseTextView(text = "Total Balance", color = Color.White, fontSize = 16.sp)
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { expanded = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExpenseTextView(text = selectedTimeRange, color = Color.White, fontSize = 14.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Time Range", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        timeRanges.forEach { range ->
                            DropdownMenuItem(
                                text = { ExpenseTextView(text = range, fontSize = 14.sp) },
                                onClick = {
                                    onTimeRangeSelected(range)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            ExpenseTextView(
                text = balance,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    ExpenseTextView(text = "Income", color = Color(0xFFDFF7F4), fontSize = 13.sp)
                    ExpenseTextView(text = income, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    ExpenseTextView(text = "Expenses", color = Color(0xFFDFF7F4), fontSize = 13.sp)
                    ExpenseTextView(text = expenses, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    item: ExpenseEntity,
    icon: Int,
    color: Color,
    onUpdate: (ExpenseEntity) -> Unit,
    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(item.title) }
    var editedAmount by remember { mutableStateOf(item.amount.toString()) }
    var editedDate by remember { mutableStateOf(item.date) }
    var editedType by remember { mutableStateOf(item.type) }
    var expandedType by remember { mutableStateOf(false) }
    val types = listOf("Income", "Expense")

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color.White,
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExpenseTextView(
                        text = "Edit Transaction",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    ExpenseTextView(
                        text = "Update details or remove this entry",
                        fontSize = 13.sp,
                        color = Color(0xFF7A8598)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        singleLine = true,
                        label = { ExpenseTextView(text = "Title", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editedAmount,
                        onValueChange = { editedAmount = it },
                        singleLine = true,
                        label = { ExpenseTextView(text = "Amount", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editedDate,
                        onValueChange = { editedDate = it },
                        singleLine = true,
                        label = { ExpenseTextView(text = "Date (dd/MM/yyyy)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = { expandedType = !expandedType }
                    ) {
                        OutlinedTextField(
                            value = editedType,
                            onValueChange = { },
                            readOnly = true,
                            singleLine = true,
                            label = { ExpenseTextView(text = "Type", fontSize = 12.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            types.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { ExpenseTextView(text = selectionOption, fontSize = 14.sp) },
                                    onClick = {
                                        editedType = selectionOption
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showDialog = false }) {
                        ExpenseTextView(
                            text = "CANCEL",
                            fontSize = 13.sp,
                            color = Color(0xFF7A8598),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(onClick = {
                        showDialog = false
                        onDelete()
                    }) {
                        ExpenseTextView(
                            text = "DELETE",
                            fontSize = 13.sp,
                            color = Color(0xFFE45757),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedAmount = editedAmount.toDoubleOrNull() ?: item.amount
                        onUpdate(
                            item.copy(
                                title = editedTitle,
                                amount = updatedAmount,
                                date = editedDate,
                                type = editedType
                            )
                        )
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Greenn),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    ExpenseTextView(
                        text = "UPDATE",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    val amountText = if (item.type.equals("Income", ignoreCase = true)) {
        "+₹${"%.2f".format(abs(item.amount))}"
    } else {
        "-₹${"%.2f".format(abs(item.amount))}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF4F6FA)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    ExpenseTextView(text = item.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    ExpenseTextView(text = item.date, fontSize = 13.sp, color = Color(0xFF8D97A8))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ExpenseTextView(
                    text = amountText,
                    fontSize = 16.sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Greenn)
                }
            }
        }
    }
}

@Composable
fun ProgressTrackerCard(modifier: Modifier = Modifier, progress: Float, balance: Double, goal: Double, selectedTimeRange: String) {
    val safeProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress,
        animationSpec = tween(durationMillis = 700),
        label = "progress"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            ExpenseTextView(text = "Savings Progress ($selectedTimeRange)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = Added,
                trackColor = Color(0xFFE8ECF2)
            )
            Spacer(modifier = Modifier.height(10.dp))
            ExpenseTextView(text = "₹${"%.2f".format(balance)} / ₹${"%.2f".format(goal)} saved", fontSize = 14.sp, fontWeight = FontWeight.Medium)

            if (goal <= 0) {
                 ExpenseTextView(
                    text = "No saving goal set for this period",
                    fontSize = 13.sp,
                    color = Color(0xFF6D7686)
                )
            } else if (balance >= goal) {
                ExpenseTextView(
                    text = "Great job! You reached your savings goal 🎉",
                    fontSize = 13.sp,
                    color = Added,
                    fontWeight = FontWeight.Medium
                )
            } else {
                val remaining = goal - balance
                ExpenseTextView(
                    text = "₹${"%.2f".format(remaining)} more to hit your goal",
                    fontSize = 13.sp,
                    color = Color(0xFF6D7686)
                )
            }
        }
    }
}

@Composable
fun PieChartCard(modifier: Modifier = Modifier, items: List<ExpenseEntity>) {
    val totals = items.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
    val total = totals.values.sum()

    val sliceColors = listOf(
        Color(0xFFEF5350),
        Color(0xFF42A5F5),
        Color(0xFF66BB6A),
        Color(0xFFFFCA28),
        Color(0xFFAB47BC),
        Color(0xFF26A69A)
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(top = 14.dp, end = 14.dp, start = 14.dp, bottom = 14.dp)) {
            ExpenseTextView(text = "Spending Chart", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            if (total <= 0.0) {
                ExpenseTextView(text = "No expense data yet", fontSize = 14.sp, color = Color(0xFF6D7686))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(150.dp)) {
                            var startAngle = -90f
                            totals.values.forEachIndexed { index, value ->
                                val sweep = (value / total * 360f).toFloat()
                                drawArc(
                                    color = sliceColors[index % sliceColors.size],
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = 32f)
                                )
                                startAngle += sweep
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ExpenseTextView(text = "Total", fontSize = 12.sp, color = Color(0xFF7A8598))
                            ExpenseTextView(
                                text = "₹${"%.0f".format(total)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        totals.entries.sortedByDescending { it.value }.take(5).forEachIndexed { index, entry ->
                            val percent = (entry.value / total * 100).toInt()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(sliceColors[index % sliceColors.size])
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                ExpenseTextView(
                                    text = "${entry.key}: $percent%",
                                    fontSize = 13.sp,
                                    color = Color(0xFF3F4A5A)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactionsState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExpenseTextView(text = "No transactions yet", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            ExpenseTextView(
                text = "Tap + to add your first income or expense",
                fontSize = 13.sp,
                color = Color(0xFF7A8598)
            )
        }
    }
}

@Composable
fun BottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    data class BottomItem(val route: String, val icon: ImageVector, val label: String)

    val items = listOf(
        BottomItem(route = "/home", icon = Icons.Default.Home, label = "Home"),
        BottomItem(route = "/insight", icon = Icons.Default.Refresh, label = "Insights"),
        BottomItem(route = "/settings", icon = Icons.Default.Settings, label = "Settings")
    )

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route || (currentRoute == null && item.route == "/home"),
                onClick = { navController.navigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { ExpenseTextView(text = item.label, fontSize = 12.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Greenn,
                    selectedTextColor = Greenn,
                    indicatorColor = Greenn.copy(alpha = 0.14f),
                    unselectedIconColor = Color(0xFF7F8A9A),
                    unselectedTextColor = Color(0xFF7F8A9A)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}
