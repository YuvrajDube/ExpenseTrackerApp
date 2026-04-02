package com.example.expensetracker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.data.model.ExpenseEntity
import com.example.expensetracker.ui.theme.Grayy
import com.example.expensetracker.ui.theme.Greenn
import com.example.expensetracker.viewmodel.HomeViewModel
import com.example.expensetracker.viewmodel.HomeViewModelFactory
import com.example.expensetracker.widget.ExpenseTextView

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel =
        HomeViewModelFactory(LocalContext.current).create(HomeViewModel::class.java)

    Surface(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (nameRow, list, card, topBar, add) = createRefs()
            Image(painter = painterResource(id = R.drawable.rectangle),
                contentDescription = "null",
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })
            Image(painter = painterResource(id = R.drawable.design), contentDescription = "")
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 32.dp, start = 16.dp, end = 16.dp
                )
                .constrainAs(nameRow) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
                Column {
                    ExpenseTextView(
                        text = "Good afternoon", fontSize = 17.sp, color = Color.White
                    )
                    ExpenseTextView(
                        text = "Yuvraj Dube",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.frame_4),
                    contentDescription = "",
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            val state by viewModel.expenses.collectAsState(initial = emptyList())
            val (balance, income, expenses) = viewModel.getSummary(state)
            CardItem(
                modifier = Modifier.constrainAs(card) {
                    top.linkTo(nameRow.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, balance, income, expenses
            )
            TransactionList(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(list) {
                        top.linkTo(card.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    },
                state,
                viewModel

            )
            Image(painter = painterResource(id = R.drawable.baseline_add_circle_outline_24),
                contentDescription = "",
                modifier = Modifier
                    .constrainAs(add) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
                    .size(80.dp)
                    .clip(CircleShape)
                    .padding(end = 25.dp, bottom = 25.dp)
                    .clickable {
                        navController.navigate("/add")
                    }
            )
        }
    }
}

@Composable
fun TransactionList(modifier: Modifier, list: List<ExpenseEntity>, viewModel: HomeViewModel) {
    val viewModel: HomeViewModel =
        HomeViewModelFactory(LocalContext.current).create(HomeViewModel::class.java)
    val state = viewModel.expenses.collectAsState(initial = emptyList()) // ✅ Live update

    LazyColumn(modifier = modifier.padding(horizontal = 15.dp)) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                ExpenseTextView(
                    text = "Recent Transaction", fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                ExpenseTextView(
                    text = "See All",
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
        items(state.value) { item ->
            TransactionItems(
                title = item.title!!,
                amount = item.amount.toString(),
                icon = if (item.type == "Income") R.drawable.profit else R.drawable.loss,
                date = item.date,
                color = if (item.type == "Income") Color.Green else Color.Red,
                onDelete = {
                    viewModel.deleteExpense(item.id)
                }
            )
        }
    }
}



@Composable
fun TransactionItems(
    title: String,
    amount: String,
    icon: Int,
    date: String,
    color: Color,
    onDelete: () -> Unit // ✅ Correct the parameter type
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row {
            Box(
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = "",
                    modifier = Modifier.padding(5.dp).size(50.dp)
                        .background(Color.White),

                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                ExpenseTextView(text = title, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                ExpenseTextView(text = date, fontSize = 18.sp)
            }
        }
        ExpenseTextView(
            text = amount,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 30.dp),
            color = color,
            fontWeight = FontWeight.SemiBold
        )

        // ✅ Fix onDelete call
        IconButton(
            onClick = { onDelete() }, // ✅ Call the lambda function
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(25.dp),
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
        }
    }
}

@Composable
fun CardItem(modifier: Modifier, balance: String, income: String, expenses: String) {
    Column(
        modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .height(200.dp)
            .background(color = Greenn)
            .padding(15.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Box {
                    ExpenseTextView(
                        text = "Total Balance", color = Color.White, fontSize = 18.sp
                    )
                    Image(
                        painter = painterResource(id = R.drawable.chevron_down),
                        contentDescription = "",
                        modifier = Modifier
                            .padding(start = 120.dp)
                            .align(Alignment.CenterEnd)
                    )
                }
                ExpenseTextView(
                    text = balance,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Image(
                painter = painterResource(id = R.drawable.dots),
                contentDescription = "null",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .align(Alignment.CenterEnd)
                    .size(30.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {


            CardItemRow(
                modifier = Modifier.align(Alignment.CenterStart),
                title = "Income",
                amount = income,
                image = R.drawable.up
            )

            CardItemRow(
                modifier = Modifier.align(Alignment.CenterEnd),
                title = "Expenses",
                amount = expenses,
                image = R.drawable.down
            )
        }
    }
}

@Composable
fun CardItemRow(modifier: Modifier, title: String, amount: String, image: Int) {
    Column(modifier = modifier) {
        Row {
            Image(
                painter = painterResource(id = image),
                contentDescription = "",
            )
            Spacer(modifier = Modifier.size(8.dp))
            ExpenseTextView(
                text = title, color = Color.White, fontSize = 18.sp
            )
        }
        ExpenseTextView(
            text = amount, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
        )
    }
}


@Composable
@Preview
fun PreviewHomeScreen(modifier: Modifier = Modifier) {
    HomeScreen(navController = rememberNavController())
}