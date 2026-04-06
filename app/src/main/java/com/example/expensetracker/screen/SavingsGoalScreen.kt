package com.example.expensetracker.screen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensetracker.ui.theme.Greenn
import com.example.expensetracker.widget.ExpenseTextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("ExpenseTrackerPrefs", Context.MODE_PRIVATE)

    val currentMonthStr = remember { SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time) }
    val goalKey = "MONTHLY_GOAL_$currentMonthStr"
    var goalAmount by remember { mutableStateOf(prefs.getString(goalKey, "") ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { ExpenseTextView(text = "Monthly Saving Goal", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Greenn)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ExpenseTextView(
                text = "Set your saving goal for this month",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = goalAmount,
                onValueChange = { goalAmount = it },
                label = { ExpenseTextView(text = "Target Amount (₹)", fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    prefs.edit().putString(goalKey, goalAmount).apply()
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Greenn),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                ExpenseTextView(text = "Save Goal", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
