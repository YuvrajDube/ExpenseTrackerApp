package com.example.expensetracker

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.expensetracker.screen.AddExpense
import com.example.expensetracker.screen.HomeScreen
import com.example.expensetracker.screen.InsightScreen
import com.example.expensetracker.screen.SettingsScreen
import com.example.expensetracker.screen.AllTransactionsScreen
import com.example.expensetracker.screen.CategoryDetailScreen
import com.example.expensetracker.screen.MonthDetailScreen
import com.example.expensetracker.screen.TransactionTypeDetailScreen
import com.example.expensetracker.screen.ProfileScreen
import com.example.expensetracker.screen.ChatBotScreen
import com.example.expensetracker.screen.SavingsGoalScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun NavHostScreen() {
    val navController= rememberNavController()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("ExpenseTrackerPrefs", Context.MODE_PRIVATE)
    val isFirstTime = sharedPreferences.getBoolean("IS_FIRST_TIME", true)

    val startDest = if (isFirstTime) "/profile" else "/home"

    NavHost(navController = navController, startDestination = startDest) {
        composable(route="/profile"){
            ProfileScreen(navController)
        }
        composable(route="/home"){
            HomeScreen(navController)
        }
        composable(route="/add"){
            AddExpense(navController)
        }
        composable(route="/insight"){
            InsightScreen(navController)
        }
        composable(route="/settings"){
            SettingsScreen(navController)
        }
        composable(route="/allTransactions"){
            AllTransactionsScreen(navController)
        }
        composable(route="/aiChat"){
            ChatBotScreen(navController)
        }
        composable(route="/savingsGoal"){
            SavingsGoalScreen(navController)
        }
        composable(
            route="/categoryDetail/{categoryName}",
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryDetailScreen(navController, categoryName)
        }
        composable(
            route="/monthDetail/{monthKey}",
            arguments = listOf(navArgument("monthKey") { type = NavType.StringType })
        ) { backStackEntry ->
            val monthKey = backStackEntry.arguments?.getString("monthKey") ?: ""
            MonthDetailScreen(navController, monthKey)
        }
        composable(
            route="/transactionTypeDetail/{transactionType}",
            arguments = listOf(navArgument("transactionType") { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionType = backStackEntry.arguments?.getString("transactionType") ?: ""
            TransactionTypeDetailScreen(navController, transactionType)
        }
    }
}