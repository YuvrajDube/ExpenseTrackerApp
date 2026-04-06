package com.example.expensetracker.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.R
import com.example.expensetracker.ui.theme.Greenn
import com.example.expensetracker.widget.ExpenseTextView
import android.content.Context

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sharedPreferences = context.getSharedPreferences("ExpenseTrackerPrefs", Context.MODE_PRIVATE)

    var name by remember { mutableStateOf("User") }
    var age by remember { mutableStateOf("N/A") }
    var gender by remember { mutableStateOf("N/A") }
    var email by remember { mutableStateOf("N/A") }

    fun loadProfileData() {
        name = sharedPreferences.getString("USER_NAME", "User") ?: "User"
        age = sharedPreferences.getString("USER_AGE", "N/A") ?: "N/A"
        gender = sharedPreferences.getString("USER_GENDER", "N/A") ?: "N/A"
        email = sharedPreferences.getString("USER_EMAIL", "N/A") ?: "N/A"
    }

    LaunchedEffect(Unit) {
        loadProfileData()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                loadProfileData()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (header, profileCard, settingsList, dummyText) = createRefs()

                // Header background similar to HomeScreen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .constrainAs(header) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.rectangle),
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .constrainAs(profileCard) {
                            top.linkTo(parent.top, margin = 50.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(Greenn)
                        .padding(20.dp)
                ) {
                    Column {
                        ExpenseTextView(
                            text = name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ExpenseTextView(
                            text = "Age: $age",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ExpenseTextView(
                            text = "Gender: $gender",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ExpenseTextView(
                            text = "Email: $email",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .constrainAs(settingsList) {
                            top.linkTo(profileCard.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(dummyText.top, margin = 16.dp)
                        }
                ) {
                    SettingsItem(
                        icon = Icons.Default.AccountCircle,
                        title = "Edit Profile",
                        onClick = { navController.navigate("/profile") }
                    )
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.List,
                        title = "Categories",
                        onClick = { /* dummy */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Reminders & Notifications",
                        onClick = { /* dummy */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Data Export",
                        onClick = { /* dummy */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Warning,
                        title = "Expense Limits",
                        onClick = { /* dummy finance related */ }
                    )
                }

                ExpenseTextView(
                    text = "All button added on settings screen are dummy",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.constrainAs(dummyText) {
                        bottom.linkTo(parent.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Greenn,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            ExpenseTextView(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Forward",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(navController = rememberNavController())
}
