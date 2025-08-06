package com.example.finalproject.presentation.screen.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finalproject.data.entity.Food

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodsByGroupScreen(
    navController: NavController,
    state: FoodState,
    onEvent: (FoodEvent) -> Unit,
    groupId: Long
) {
    val searchQuery = remember { mutableStateOf("") }

    // Filter foods theo groupId and search query
    val filteredFoods = remember(state.foods, searchQuery.value) {
        state.foods.filter { food ->
            // Check if food belongs to the group and matches search query
            food.groupFoodId == groupId &&
                    (searchQuery.value.isEmpty() || food.name.contains(searchQuery.value, ignoreCase = true))
        }
    }

    // Search group
    val groupName = state.groupFoods.find { it.id == groupId }?.name ?: "Category"

    // State for delete confirmation dialog
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var foodToDelete by remember { mutableStateOf<Food?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = groupName,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1877F2)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1877F2)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addFood") },
                containerColor = Color(0xFF1877F2)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Food",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    placeholder = { Text("Search foods in $groupName...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF1877F2),
                        unfocusedIndicatorColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Foods count
                Text(
                    "${filteredFoods.size} ${if (filteredFoods.size == 1) "item" else "items"} in $groupName",
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (searchQuery.value.isEmpty())
                                    "No foods in $groupName yet"
                                else
                                    "No foods matching \"${searchQuery.value}\" in $groupName",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate("addFood") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1877F2)
                                )
                            ) {
                                Text("Add First Food", color = Color.White)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFoods) { food ->
                            FoodItemCard(
                                food = food,
                                groupName = groupName,
                                groupIcon = state.groupFoods.find { it.id == food.groupFoodId }?.icon,
                                onItemClick = {
                                    // View details if needed
                                },
                                onEditClick = {
                                    navController.navigate("editFood/${food.id}")
                                },
                                onDeleteClick = {
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
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = {
                                showDeleteConfirmDialog = false
                                foodToDelete = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}