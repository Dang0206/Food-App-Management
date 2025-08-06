package com.example.finalproject.presentation.screen.groupfood

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finalproject.data.entity.GroupFood
import com.example.finalproject.presentation.screen.food.FacebookColors
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupFoodScreen(
    navController: NavController,
    groupFoodId: Long,
    state: GroupFoodState,
    onEvent: (GroupFoodEvent) -> Unit,
    viewModel: GroupFoodViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // States for feedback dialogs
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showConfirmExitDialog by remember { mutableStateOf(false) }

    // Fetch the group food data when screen is opened
    LaunchedEffect(key1 = groupFoodId) {
        if (groupFoodId > 0) {
            onEvent(GroupFoodEvent.GetGroupFoodById(groupFoodId))
        }
    }

    // Predefined icons/emojis for food categories
    val categoryIcons = listOf(
        "🍎" to "Fruits",
        "🥦" to "Vegetables",
        "🥩" to "Meats",
        "🥛" to "Dairy",
        "🍞" to "Bakery",
        "🥫" to "Canned",
        "🍕" to "Fast Food",
        "🍝" to "Pasta",
        "🍰" to "Desserts",
        "☕" to "Beverages",
        "🧂" to "Condiments",
        "🍫" to "Snacks"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(FacebookColors.FacebookBackground)
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        containerColor = FacebookColors.FacebookBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(FacebookColors.White)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        // Show confirm dialog if changes were made
                        if (state.id > 0) {
                            showConfirmExitDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowLeft,
                        contentDescription = "Back",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF1877F2)
                    )
                }

                Text(
                    text = "Edit Category",
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp, end = 48.dp),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1877F2),
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FacebookColors.FacebookBackground)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Name Input
                    FacebookAnimatedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.name,
                        onValueChange = { onEvent(GroupFoodEvent.OnNameChange(it)) },
                        placeholder = { Text("Category Name") }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Icon Selection Section
                    Text(
                        text = "Choose an Icon",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Selected icon display
                    if (state.icon.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = FacebookColors.LightBlue),
                            border = BorderStroke(2.dp, FacebookColors.PrimaryBlue)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.icon,
                                    fontSize = 32.sp,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(
                                    text = "Selected Icon",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = FacebookColors.PrimaryBlue
                                )
                            }
                        }
                    }

                    // Icon grid
                    categoryIcons.chunked(3).forEach { rowIcons ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowIcons.forEach { (icon, label) ->
                                IconCard(
                                    icon = icon,
                                    label = label,
                                    isSelected = state.icon == icon,
                                    onClick = {
                                        onEvent(GroupFoodEvent.OnIconChange(icon))
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill empty spaces in the last row
                            repeat(3 - rowIcons.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Update button
                    val interactionSource = remember { MutableInteractionSource() }
                    var isPressed by remember { mutableStateOf(false) }

                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else 1f,
                        label = "buttonScale"
                    )

                    Button(
                        onClick = {
                            // Validate input before saving
                            when {
                                state.name.isBlank() -> {
                                    errorMessage = "Category name cannot be empty"
                                    showErrorDialog = true
                                }
                                state.icon.isBlank() -> {
                                    errorMessage = "Please select an icon for the category"
                                    showErrorDialog = true
                                }
                                else -> {
                                    // All validations passed, update the category
                                    isPressed = true
                                    // Reset the press state after a short delay and save
                                    MainScope().launch {
                                        delay(100)
                                        isPressed = false

                                        // Create updated GroupFood object
                                        val updatedGroupFood = GroupFood(
                                            id = state.id,
                                            icon = state.icon,
                                            name = state.name,
                                            createdDate = System.currentTimeMillis(), // Will be ignored on update
                                            createdBy = null,
                                            updatedDate = System.currentTimeMillis(),
                                            updatedBy = null,
                                            deletedDate = null,
                                            deletedBy = null
                                        )

                                        onEvent(GroupFoodEvent.OnUpdateGroupFood(updatedGroupFood))
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale),
                        interactionSource = interactionSource,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FacebookColors.PrimaryBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Update Category",
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Error dialog
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Validation Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("OK", color = FacebookColors.PrimaryBlue)
                    }
                },
                containerColor = FacebookColors.White,
                titleContentColor = Color.Black,
                textContentColor = Color.DarkGray
            )
        }

        // Success dialog
        var showSuccessDialog by remember { mutableStateOf(false) }

        // Collect UI events from ViewModel
        LaunchedEffect(key1 = true) {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is GroupFoodViewModel.UiEvent.ShowSuccess -> {
                        if (event.message.contains("updated")) {
                            showSuccessDialog = true
                            delay(1500) // Show success message for 1.5 seconds
                            showSuccessDialog = false
                            navController.popBackStack() // Navigate back
                        }
                    }
                    is GroupFoodViewModel.UiEvent.ShowError -> {
                        errorMessage = event.message
                        showErrorDialog = true
                    }
                    else -> {}
                }
            }
        }

        if (showSuccessDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            "Category Updated Successfully!",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Confirm Exit Dialog
        if (showConfirmExitDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmExitDialog = false },
                title = { Text("Discard Changes?") },
                text = { Text("Are you sure you want to exit without saving your changes?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmExitDialog = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Discard")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showConfirmExitDialog = false }
                    ) {
                        Text("Continue Editing")
                    }
                }
            )
        }
    }
}