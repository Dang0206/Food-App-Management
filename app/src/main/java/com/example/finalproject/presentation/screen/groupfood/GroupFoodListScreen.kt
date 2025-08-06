package com.example.finalproject.presentation.screen.groupfood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finalproject.data.entity.GroupFood

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupFoodListScreen(
    navController: NavController,
    state: GroupFoodState,
    onEvent: (GroupFoodEvent) -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    val filteredGroups = remember(state.groupFoods, searchQuery.value) {
        if (searchQuery.value.isEmpty()) {
            state.groupFoods
        } else {
            state.groupFoods.filter {
                it.name.contains(searchQuery.value, ignoreCase = true)
            }
        }
    }

    // State for delete confirmation dialog
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var groupToDelete by remember { mutableStateOf<GroupFood?>(null) }

    // Load all groups when the screen becomes visible
    LaunchedEffect(key1 = true) {
        onEvent(GroupFoodEvent.LoadAllGroupFoods)
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
            // Header
            Text(
                "Food Categories",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1877F2)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                placeholder = { Text("Search categories...") },
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

            // Groups count
            Text(
                "${filteredGroups.size} ${if (filteredGroups.size == 1) "category" else "categories"}",
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Group List
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1877F2))
                }
            } else if (filteredGroups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.value.isEmpty())
                            "No categories added yet"
                        else
                            "No categories matching \"${searchQuery.value}\"",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredGroups) { group ->
                        GroupFoodItemCard(
                            groupFood = group,
                            foodCount = state.foodCounts[group.id] ?: 0,
                            onItemClick = {
                                // Navigate to foods in this category
                                navController.navigate("foods/group/${group.id}")
                            },
                            onEditClick = {
                                // Navigate to edit screen
                                navController.navigate("editGroupFood/${group.id}")
                            },
                            onDeleteClick = {
                                // Show delete confirmation dialog
                                groupToDelete = group
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

        // Add Group button
        FloatingActionButton(
            onClick = { navController.navigate("addGroupFood") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF1877F2)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Category",
                tint = Color.White
            )
        }

        // Delete confirmation dialog
        if (showDeleteConfirmDialog && groupToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmDialog = false
                    groupToDelete = null
                },
                title = { Text("Confirm Delete") },
                text = {
                    val foodCount = state.foodCounts[groupToDelete?.id] ?: 0
                    if (foodCount > 0) {
                        Text("This category contains $foodCount food(s). Deleting it will not delete the foods but they will have no category. Are you sure you want to delete ${groupToDelete?.name}?")
                    } else {
                        Text("Are you sure you want to delete ${groupToDelete?.name}?")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            groupToDelete?.let { group ->
                                onEvent(GroupFoodEvent.OnSoftDeleteGroupFood(group.id))
                            }
                            showDeleteConfirmDialog = false
                            groupToDelete = null
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
                            groupToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun GroupFoodItemCard(
    groupFood: GroupFood,
    foodCount: Int,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
            // Icon/Emoji placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                groupFood.icon?.let {
                    Text(
                        text = it,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Group details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = groupFood.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$foodCount ${if (foodCount == 1) "item" else "items"}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
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