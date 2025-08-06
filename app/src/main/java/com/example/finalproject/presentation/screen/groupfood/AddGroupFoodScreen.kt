package com.example.finalproject.presentation.screen.groupfood

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finalproject.presentation.screen.food.FacebookColors
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupFoodScreen(
    navController: NavController,
    state: GroupFoodState,
    onEvent: (GroupFoodEvent) -> Unit,
    viewModel: GroupFoodViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // States for feedback dialogs
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
                    onClick = { navController.popBackStack() },
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
                    text = "New Category",
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp, end = 48.dp),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1877F2),
                    textAlign = TextAlign.Center
                )
            }
        },
        bottomBar = {
            val interactionSource = remember { MutableInteractionSource() }
            var isPressed by remember { mutableStateOf(false) }

            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                label = "buttonScale"
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = FacebookColors.White,
                shadowElevation = 8.dp
            ) {
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
                                // All validations passed, save the category
                                isPressed = true
                                // Reset the press state after a short delay and save
                                MainScope().launch {
                                    delay(100)
                                    isPressed = false
                                    onEvent(GroupFoodEvent.OnSaveGroupFood)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .scale(scale),
                    interactionSource = interactionSource,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FacebookColors.PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Save Category",
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                    .padding(horizontal = 16.dp),
                // Use contentPadding to add space at the bottom
                contentPadding = PaddingValues(bottom = 16.dp)
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
                    is GroupFoodViewModel.UiEvent.SaveGroupFood -> {
                        showSuccessDialog = true
                        delay(1500) // Show success message for 1.5 seconds
                        showSuccessDialog = false
                        navController.popBackStack() // Navigate back
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
                modifier = Modifier.fillMaxSize(),
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Category Saved Successfully!",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IconCard(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) FacebookColors.LightBlue else Color.White,
        label = "backgroundColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) FacebookColors.PrimaryBlue else Color.LightGray,
        label = "borderColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (isSelected) FacebookColors.PrimaryBlue else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacebookAnimatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Animations for click/focus effects
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 4.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "elevation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.01f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused)
            FacebookColors.LightBlue
        else
            FacebookColors.White,
        label = "backgroundColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused)
            FacebookColors.PrimaryBlue
        else
            Color.LightGray,
        label = "borderColor"
    )

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier
            .scale(scale)
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            interactionSource = interactionSource,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = FacebookColors.Black,
                unfocusedTextColor = FacebookColors.Black,
                focusedPlaceholderColor = FacebookColors.DarkGray,
                unfocusedPlaceholderColor = FacebookColors.DarkGray
            ),
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            textStyle = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            ),
            placeholder = placeholder,
            trailingIcon = trailingIcon
        )
    }
}