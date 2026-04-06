package com.example.expensetracker.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.data.model.ExpenseEntity
import com.example.expensetracker.viewmodel.HomeViewModel
import com.example.expensetracker.viewmodel.HomeViewModelFactory
import com.google.ai.client.generativeai.GenerativeModel
import com.example.expensetracker.ui.theme.Greenn
import com.example.expensetracker.widget.ExpenseTextView
import com.example.expensetracker.BuildConfig
import kotlinx.coroutines.launch

private data class ChatMessage(val id: Long, val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(navController: NavController) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var input by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(context))
    val expensesState by homeViewModel.expenses.collectAsState(initial = emptyList())

    val suggestions = listOf(
        "Monthly summary",
        "Natural-language query",
        "Anomaly detection & explainers",
        "Forecast & what-if",
        "Storytelling timeline"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { ExpenseTextView(text = "AI Chat", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Greenn)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (msg.isUser) Greenn else Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(modifier = Modifier
                                .widthIn(max = 300.dp)
                                .background(Color.Transparent)
                                .padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    color = if (msg.isUser) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { suggestion ->
                    Card(
                        modifier = Modifier.clickable {
                            input = suggestion
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE9EEF6))
                    ) {
                        ExpenseTextView(
                            text = suggestion,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            color = Color(0xFF4D5A6B)
                        )
                    }
                }
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { ExpenseTextView(text = "Ask: e.g. 'Monthly summary'", fontSize = 12.sp) }
                )

                Spacer(modifier = Modifier.widthIn(8.dp))

                Button(onClick = {
                    val trimmed = input.trim()
                    if (trimmed.isEmpty()) return@Button
                    val userId = System.currentTimeMillis()
                    messages.add(ChatMessage(userId, trimmed, isUser = true))
                    input = ""

                    scope.launch {
                        val reply = try {
                            getGeminiResponse(trimmed, expensesState)
                        } catch (e: Exception) {
                            "Sorry, something went wrong: ${e.localizedMessage}"
                        }
                        messages.add(ChatMessage(System.currentTimeMillis(), reply, isUser = false))
                    }
                },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    ExpenseTextView(text = "Send", fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

suspend fun getGeminiResponse(prompt: String, expenses: List<ExpenseEntity>): String {
    val apiKey = BuildConfig.GEMINI_API_KEY

    if (apiKey.isEmpty()) {
        return "API Key is missing. Please add GEMINI_API_KEY to local.properties."
    }

    val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    val transactionsContext = if (expenses.isEmpty()) {
        "No transactions yet."
    } else {
        expenses.joinToString(separator = "\n") {
            "Date: ${it.date}, Title: ${it.title}, Amount: ${it.amount}, Type: ${it.type}, Category: ${it.category}"
        }
    }

    val fullPrompt = """
        You are an expert AI financial assistant. Below is the user's transaction data.
        
        Transactions:
        $transactionsContext

        Based on these transactions, please respond to the following user request:
        "$prompt"

        If the user asks for a summary, anomaly detection, forecast, or storytelling timeline, use the provided transaction data to provide a personalized, accurate, and concise response. Do not invent transactions. Keep the tone helpful.
    """.trimIndent()

    val response = generativeModel.generateContent(fullPrompt)
    return response.text ?: "I'm sorry, I couldn't generate a response."
}
