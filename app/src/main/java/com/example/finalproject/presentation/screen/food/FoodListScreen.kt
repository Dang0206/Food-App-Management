package com.example.finalproject.presentation.screen.food

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finalproject.R
import com.example.finalproject.data.entity.Food
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListScreen(
    navController: NavController,
    state: FoodState,
    onEvent: (FoodEvent) -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    val filteredFoods = remember(state.foods, searchQuery.value) {
        if (searchQuery.value.isEmpty()) {
            state.foods
        } else {
            state.foods.filter {
                it.name.contains(searchQuery.value, ignoreCase = true)
            }
        }
    }

    // State for delete confirmation dialog
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var foodToDelete by remember { mutableStateOf<Food?>(null) }

    // Load all foods when the screen becomes visible
    LaunchedEffect(key1 = true) {
        onEvent(FoodEvent.LoadAllFoods)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with Categories button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Food List",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1877F2)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))





            // Foods count
            Text(
                "${filteredFoods.size} ${if (filteredFoods.size == 1) "item" else "items"}",
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Food List
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1877F2))
                }
            } else if (filteredFoods.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.value.isEmpty())
                            "No foods added yet"
                        else
                            "No foods matching \"${searchQuery.value}\"",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredFoods) { food ->
                        FoodItemCard(
                            food = food,
                            groupName = state.groupFoods.find { it.id == food.groupFoodId }?.name,
                            groupIcon = state.groupFoods.find { it.id == food.groupFoodId }?.icon,
                            onItemClick = {
                                // View details or navigate to food detail screen if you have one
                            },
                            onEditClick = {
                                // Navigate to edit screen
                                navController.navigate("editFood/${food.id}")
                            },
                            onDeleteClick = {
                                // Show delete confirmation dialog
                                foodToDelete = food
                                showDeleteConfirmDialog = true
                            }
                        )
                    }

                    // Add some bottom padding for better UX with FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Add Food button
        FloatingActionButton(
            onClick = { navController.navigate("addFood") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF1877F2)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Food",
                tint = Color.White
            )
        }

        // Delete confirmation dialog
        if (showDeleteConfirmDialog && foodToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmDialog = false
                    foodToDelete = null
                },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete ${foodToDelete?.name}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            foodToDelete?.let { food ->
                                onEvent(FoodEvent.OnSoftDeleteFood(food.id))
                            }
                            showDeleteConfirmDialog = false
                            foodToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ) ) {
                        Text("Delete")
                    } },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteConfirmDialog = false
                            foodToDelete = null }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }


    }



@Composable
fun FoodItemCard(
    food: Food,
    groupName: String?,
    groupIcon: String?,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val daysLeft = calculateDaysLeft(food.expiryDate)
    val daysLeftColor = when {
        daysLeft <= 0 -> Color.Red
        daysLeft <= 3 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFF4CAF50) // Green
    }

    val defaultImageResId = R.drawable.ic_launcher_foreground // Fallback to app icon if no specific image

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Food Image or placeholder with category icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (groupIcon != null && groupIcon.isNotEmpty()) {
                    Text(
                        text = groupIcon,
                        fontSize = 32.sp
                    )
                } else {
                    Image(
                        painter = painterResource(id = defaultImageResId),
                        contentDescription = food.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Food details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = food.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (daysLeft > 0) "$daysLeft days left" else "Expired",
                        color = daysLeftColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (groupName != null) {
                        Text(
                            text = " | ",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )

                        Text(
                            text = groupName,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (food.note?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = food.note,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action buttons
            Row {
                // Edit button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF1877F2)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

// Helper function to calculate days left until expiry
private fun calculateDaysLeft(expiryDate: Long?): Int {
    if (expiryDate == null) return Int.MAX_VALUE

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    return max(0, ((expiryDate - today) / (24 * 60 * 60 * 1000)).toInt())
}

