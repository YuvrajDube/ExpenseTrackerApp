package com.example.expensetracker.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.Utils
import com.example.expensetracker.data.model.ExpenseEntity
import com.example.expensetracker.ui.theme.Greenn
import com.example.expensetracker.viewmodel.AddExpenseViewModel
import com.example.expensetracker.viewmodel.AddExpenseViewModelFactory
import com.example.expensetracker.widget.ExpenseTextView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun AddExpense(navController: NavController) {
    val viewModel: AddExpenseViewModel = viewModel(factory = AddExpenseViewModelFactory(LocalContext.current))
    val coroutineScope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }
    var completedFields by remember { mutableIntStateOf(0) }
    val totalFields = 5

    Scaffold(
        containerColor = Color(0xFFF6F8FB)
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color(0xFFF6F8FB)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AddExpenseHeader(
                    onBack = { navController.popBackStack() },
                    stepLabel = "Step ${completedFields.coerceAtLeast(1)} of $totalFields"
                )

                DataForm(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                    isSaving = isSaving,
                    onProgressChanged = { completed, _ ->
                        completedFields = completed
                    },
                    onAddExpenseClick = { model ->
                        if (isSaving) return@DataForm
                        coroutineScope.launch {
                            isSaving = true
                            val success = viewModel.addExpense(model)
                            isSaving = false
                            if (success) {
                                Toast.makeText(ctx, "Expense added", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(ctx, "Failed to add expense", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AddExpenseHeader(onBack: () -> Unit, stepLabel: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6FD9CC), Greenn)
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            ExpenseTextView(
                text = stepLabel,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.92f)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 6.dp)
        ) {
            ExpenseTextView(
                text = "Add Transaction",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            ExpenseTextView(
                text = "Track spending in seconds",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun DataForm(
    modifier: Modifier,
    isSaving: Boolean,
    onProgressChanged: (completed: Int, total: Int) -> Unit,
    onAddExpenseClick: (model: ExpenseEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<Long?>(null) }
    var dateDialogVisibility by remember { mutableStateOf(false) }

    val categoriesList = listOf("Food", "Travel", "Bills", "Entertainment", "Shopping", "Others")
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") }

    val parsedAmount = amount.toDoubleOrNull()
    val isNameValid = name.trim().isNotEmpty()
    val isAmountValid = parsedAmount != null && parsedAmount > 0
    val isDateSelected = date != null
    val isCategorySelected = category.isNotBlank()

    val completedFields = listOf(
        isNameValid,
        isAmountValid,
        isDateSelected,
        isCategorySelected,
        type.isNotBlank()
    ).count { it }
    val totalFields = 5

    LaunchedEffect(completedFields) {
        onProgressChanged(completedFields, totalFields)
    }

    val isFormValid = isNameValid && isAmountValid && isDateSelected && isCategorySelected

    val dateFieldInteractionSource = remember { MutableInteractionSource() }
    LaunchedEffect(dateFieldInteractionSource) {
        dateFieldInteractionSource.interactions.collectLatest { interaction ->
            if (interaction is PressInteraction.Release) {
                dateDialogVisibility = true
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExpenseTextView(
                text = "Basic info",
                fontSize = 13.sp,
                color = Color(0xFF697588),
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { ExpenseTextView(text = "Name", fontSize = 12.sp) },
                placeholder = { ExpenseTextView(text = "e.g. Grocery shopping", fontSize = 12.sp) },
                isError = !isNameValid && name.isNotEmpty(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Greenn,
                    unfocusedBorderColor = Color(0xFFCED5E0)
                )
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { ExpenseTextView(text = "Amount", fontSize = 12.sp) },
                placeholder = { ExpenseTextView(text = "Amount in ₹", fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = !isAmountValid && amount.isNotEmpty(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Greenn,
                    unfocusedBorderColor = Color(0xFFCED5E0)
                )
            )

            OutlinedTextField(
                value = date?.let { Utils.formatDateToHumanReadable(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                interactionSource = dateFieldInteractionSource,
                modifier = Modifier.fillMaxWidth(),
                label = { ExpenseTextView(text = "Date", fontSize = 12.sp) },
                placeholder = { ExpenseTextView(text = "Select date", fontSize = 12.sp, color = Color(0xFF9DA8B8)) },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                trailingIcon = {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.clickable { dateDialogVisibility = true }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Greenn,
                    unfocusedBorderColor = Color(0xFFCED5E0)
                )
            )

//            ExpenseTextView(
//                text = "Classification",
//                fontSize = 13.sp,
//                color = Color(0xFF697588),
//                fontWeight = FontWeight.Medium
//            )

            ExpenseDropDrown(
                label = "Category",
                listofItems = categoriesList,
                selectedItem = category,
                leadingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) },
                onItemSelected = { category = it }
            )

            TypeSegmentedSelector(
                selectedType = type,
                onTypeSelected = { type = it }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val model = ExpenseEntity(
                        id = 0,
                        title = name.trim(),
                        amount = parsedAmount ?: 0.0,
                        date = Utils.formatDateToHumanReadable(date ?: System.currentTimeMillis()),
                        category = category,
                        type = type
                    )
                    onAddExpenseClick(model)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isFormValid && !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "Income") Color(0xFF4CAF50) else Greenn,
                    disabledContainerColor = Color(0xFFBFC8D6)
                )
            ) {
                ExpenseTextView(
                    text = if (isSaving) "Saving..." else "Save Transaction",
                    fontSize = 15.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (dateDialogVisibility) {
        ExpenseDataPickerDialog(
            initialDate = date ?: System.currentTimeMillis(),
            onDateSelected = {
                date = it
                dateDialogVisibility = false
            },
            onDismiss = { dateDialogVisibility = false }
        )
    }
}

@Composable
private fun TypeSegmentedSelector(selectedType: String, onTypeSelected: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        TypeChip(
            modifier = Modifier.weight(1f),
            text = "Expense",
            selected = selectedType == "Expense",
            selectedColor = Greenn,
            onClick = { onTypeSelected("Expense") }
        )
        TypeChip(
            modifier = Modifier.weight(1f),
            text = "Income",
            selected = selectedType == "Income",
            selectedColor = Color(0xFF4CAF50),
            onClick = { onTypeSelected("Income") }
        )
    }
}

@Composable
private fun TypeChip(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) selectedColor.copy(alpha = 0.15f) else Color(0xFFF3F6FA))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        ExpenseTextView(
            text = text,
            fontSize = 14.sp,
            color = if (selected) selectedColor else Color(0xFF617086),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDataPickerDialog(
    initialDate: Long,
    onDateSelected: (date: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis ?: initialDate) }) {
                ExpenseTextView(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                ExpenseTextView(text = "Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDropDrown(
    label: String,
    listofItems: List<String>,
    selectedItem: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    onItemSelected: (item: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { ExpenseTextView(text = label, fontSize = 12.sp) },
            placeholder = {
                if (selectedItem.isBlank()) {
                    ExpenseTextView(text = "Select category", fontSize = 12.sp, color = Color(0xFF9DA8B8))
                }
            },
            leadingIcon = leadingIcon,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Greenn,
                unfocusedBorderColor = Color(0xFFCED5E0),
                focusedContainerColor = Color(0xFFF9FBFD),
                unfocusedContainerColor = Color(0xFFF9FBFD)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            listofItems.forEach {
                DropdownMenuItem(
                    text = {
                        ExpenseTextView(
                            text = it,
                            fontSize = 14.sp,
                            color = if (it == selectedItem) Greenn else Color(0xFF344054),
                            fontWeight = if (it == selectedItem) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onItemSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewAddExpense() {
    AddExpense(navController = rememberNavController())
}
