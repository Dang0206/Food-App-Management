package com.example.finalproject.presentation.screen.food

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finalproject.data.entity.Food
import com.example.finalproject.data.entity.GroupFood
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFoodScreen(
    navController: NavController,
    foodId: Long,
    state: FoodState,
    onEvent: (FoodEvent) -> Unit,
    viewModel: FoodViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val groupExpanded = remember { mutableStateOf(false) }
    val remindExpanded = remember { mutableStateOf(false) }

    // Fetch the food data when screen is opened
    LaunchedEffect(key1 = foodId) {
        if (foodId > 0) {
            onEvent(FoodEvent.GetFoodById(foodId))
        }
    }

    val groups = state.groupFoods

    val selectedGroupName = remember(state.groupFoodId) {
        groups.find { it.id == state.groupFoodId }?.name ?: "Select Group"
    }

    // Remind Before options
    val remindOptions = remember { RemindBeforeOption.entries.toTypedArray() }
    val selectedRemindOption = remember(state.remindBefore, state.remindOptionId) {
        state.remindOptionId?.let { RemindBeforeOption.getById(it) }
            ?: RemindBeforeOption.getByDays(state.remindBefore)
    }

    // Custom reminder text for display
    val remindBeforeDisplayText = remember(state.remindBefore, selectedRemindOption) {
        if (selectedRemindOption == RemindBeforeOption.OTHER && state.remindBefore != null && state.remindBefore > 0) {
            "Other (${state.remindBefore} days)"
        } else {
            selectedRemindOption.displayName
        }
    }

    // Date picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.expiryDate
    )

    // States for feedback dialogs
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showConfirmExitDialog by remember { mutableStateOf(false) }

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
                    text = "Edit Food",
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
            ) {
                item {
                    // Facebook-themed TextField for Name
                    FacebookAnimatedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        value = state.name,
                        onValueChange = { onEvent(FoodEvent.OnNameChange(it)) },
                        placeholder = { Text("Food Name") }
                    )
                }

                item {
                    // Facebook-themed TextField for Expiry Date
                    FacebookAnimatedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        value = if (state.expiryDate != null)
                            DateFormat.getDateInstance().format(Date(state.expiryDate))
                        else "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Expiry Date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Filled.DateRange,
                                    "Select date",
                                    tint = FacebookColors.PrimaryBlue
                                )
                            }
                        }
                    )
                    if (showDatePicker) {
                        FacebookDatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            onDateSelected = {
                                onEvent(FoodEvent.OnExpiryDateChange(it))
                                showDatePicker = false
                            },
                            datePickerState = datePickerState
                        )
                    }
                }

                item {
                    // Remind Before Dropdown with Facebook Theme
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()

                    val elevation by animateDpAsState(
                        targetValue = if (isFocused || remindExpanded.value) 4.dp else 1.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "elevation"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isFocused || remindExpanded.value) 1.01f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "scale"
                    )

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isFocused || remindExpanded.value)
                            FacebookColors.LightBlue
                        else
                            FacebookColors.White,
                        label = "backgroundColor"
                    )

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                        border = BorderStroke(1.dp, if (isFocused || remindExpanded.value)
                            FacebookColors.PrimaryBlue
                        else
                            Color.LightGray),
                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            .scale(scale)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = remindExpanded.value,
                            onExpandedChange = { remindExpanded.value = !remindExpanded.value }
                        ) {
                            TextField(
                                value = remindBeforeDisplayText,
                                onValueChange = {},
                                readOnly = true,
                                interactionSource = interactionSource,
                                label = { Text("Remind Before", color = FacebookColors.DarkGray) },
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = FacebookColors.Black,
                                    unfocusedTextColor = FacebookColors.Black
                                ),
                                textStyle = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp
                                ),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = FacebookColors.PrimaryBlue
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = remindExpanded.value,
                                onDismissRequest = { remindExpanded.value = false }
                            ) {
                                remindOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option.displayName) },
                                        onClick = {
                                            onEvent(FoodEvent.OnRemindOptionIdChange(option.id))
                                            if (option == RemindBeforeOption.OTHER) {
                                                // Only set remindBefore to -1 as a placeholder for custom value
                                                onEvent(FoodEvent.OnRemindBeforeChange(-1))
                                            } else {
                                                onEvent(FoodEvent.OnRemindBeforeChange(option.days))
                                            }
                                            remindExpanded.value = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Only show custom days input field if "Other" is selected
                item {
                    if (selectedRemindOption == RemindBeforeOption.OTHER || RemindBeforeOption.isCustomValue(state.remindBefore)) {
                        FacebookAnimatedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                            value = if (state.remindBefore != null && state.remindBefore > 0)
                                state.remindBefore.toString()
                            else "",
                            onValueChange = { daysStr ->
                                val days = daysStr.toIntOrNull() ?: 0
                                if (days > 0) {
                                    onEvent(FoodEvent.OnRemindBeforeChange(days))
                                }
                            },
                            placeholder = { Text("Enter custom days") }
                        )
                    }
                }

                item {
                    // Facebook-themed Time Picker for Notify Time
                    val timeInteractionSource = remember { MutableInteractionSource() }
                    val isTimeFocused by timeInteractionSource.collectIsFocusedAsState()

                    val timeElevation by animateDpAsState(
                        targetValue = if (isTimeFocused) 4.dp else 1.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "timeElevation"
                    )

                    val timeScale by animateFloatAsState(
                        targetValue = if (isTimeFocused) 1.01f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "timeScale"
                    )

                    val timeBackgroundColor by animateColorAsState(
                        targetValue = if (isTimeFocused)
                            FacebookColors.LightBlue
                        else
                            FacebookColors.White,
                        label = "timeBackgroundColor"
                    )

                    val timeBorderColor by animateColorAsState(
                        targetValue = if (isTimeFocused)
                            FacebookColors.PrimaryBlue
                        else
                            Color.LightGray,
                        label = "timeBorderColor"
                    )

                    // Format time for display
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val displayTime = remember(state.notifyTime) {
                        if (state.notifyTime != null && state.notifyTime > 0) {
                            val calendar = Calendar.getInstance()
                            val hour = (state.notifyTime / 100).toInt()
                            val minute = (state.notifyTime % 100).toInt()
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            timeFormat.format(calendar.time)
                        } else {
                            ""
                        }
                    }

                    // Time picker state
                    var showTimePicker by remember { mutableStateOf(false) }
                    val initialHour = if (state.notifyTime != null && state.notifyTime > 0)
                        (state.notifyTime / 100).toInt() else 12
                    val initialMinute = if (state.notifyTime != null && state.notifyTime > 0)
                        (state.notifyTime % 100).toInt() else 0

                    val timePickerState = rememberTimePickerState(
                        initialHour = initialHour,
                        initialMinute = initialMinute,
                        is24Hour = false
                    )

                    // Time picker field
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = timeElevation),
                        border = BorderStroke(1.dp, timeBorderColor),
                        colors = CardDefaults.cardColors(containerColor = timeBackgroundColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            .scale(timeScale)
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            interactionSource = timeInteractionSource,
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
                            value = displayTime,
                            onValueChange = {},
                            readOnly = true,
                            textStyle = TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            ),
                            placeholder = { Text("Notify Time") },
                            trailingIcon = {
                                IconButton(onClick = { showTimePicker = true }) {
                                    Icon(
                                        Icons.Outlined.AccessTime,
                                        "Select time",
                                        tint = FacebookColors.PrimaryBlue
                                    )
                                }
                            }
                        )
                    }

                    // Show time picker dialog
                    if (showTimePicker) {
                        FacebookTimePickerDialog(
                            onDismissRequest = { showTimePicker = false },
                            onTimeSelected = { hour, minute ->
                                // Convert hour and minute to a numeric value for storage
                                val timeValue = (hour * 100 + minute).toLong()
                                onEvent(FoodEvent.OnNotifyTimeChange(timeValue))
                            },
                            timePickerState = timePickerState
                        )
                    }
                }

                item {
                    // Group Food Dropdown with Facebook Theme
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()

                    val elevation by animateDpAsState(
                        targetValue = if (isFocused || groupExpanded.value) 4.dp else 1.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "elevation"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isFocused || groupExpanded.value) 1.01f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "scale"
                    )

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isFocused || groupExpanded.value)
                            FacebookColors.LightBlue
                        else
                            FacebookColors.White,
                        label = "backgroundColor"
                    )

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                        border = BorderStroke(1.dp, if (isFocused || groupExpanded.value)
                            FacebookColors.PrimaryBlue
                        else
                            Color.LightGray),
                        colors = CardDefaults.cardColors(containerColor = backgroundColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            .scale(scale)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = groupExpanded.value,
                            onExpandedChange = { groupExpanded.value = !groupExpanded.value }
                        ) {
                            TextField(
                                value = selectedGroupName,
                                onValueChange = {},
                                readOnly = true,
                                interactionSource = interactionSource,
                                label = { Text("Select Group", color = FacebookColors.DarkGray) },
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = FacebookColors.Black,
                                    unfocusedTextColor = FacebookColors.Black
                                ),
                                textStyle = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp
                                ),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = FacebookColors.PrimaryBlue
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = groupExpanded.value,
                                onDismissRequest = { groupExpanded.value = false }
                            ) {
                                groups.forEach { group ->
                                    DropdownMenuItem(
                                        text = { Text(text = group.name) },
                                        onClick = {
                                            onEvent(FoodEvent.OnGroupFoodIdChange(group.id))
                                            groupExpanded.value = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Facebook-themed TextField for Notes
                    FacebookAnimatedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                        value = state.note ?: "",
                        onValueChange = { onEvent(FoodEvent.OnNoteChange(it)) },
                        placeholder = { Text("Notes") }
                    )
                }

                item {
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
                                    errorMessage = "Food name cannot be empty"
                                    showErrorDialog = true
                                }
                                state.expiryDate == null -> {
                                    errorMessage = "Please select an expiry date"
                                    showErrorDialog = true
                                }
                                state.remindBefore == null || (state.remindBefore == -1 && selectedRemindOption == RemindBeforeOption.OTHER) -> {
                                    errorMessage = "Please specify remind days"
                                    showErrorDialog = true
                                }
                                state.notifyTime == null || state.notifyTime == 0L -> {
                                    errorMessage = "Please select a notification time"
                                    showErrorDialog = true
                                }
                                state.groupFoodId == null -> {
                                    errorMessage = "Please select a food group"
                                    showErrorDialog = true
                                }
                                else -> {
                                    // All validations passed, update the food
                                    isPressed = true
                                    // Reset the press state after a short delay and save
                                    MainScope().launch {
                                        delay(100)
                                        isPressed = false

                                        // Create updated Food object
                                        val updatedFood = Food(
                                            id = state.id,
                                            name = state.name,
                                            expiryDate = state.expiryDate,
                                            remindBefore = state.remindBefore,
                                            remindOptionId = state.remindOptionId,
                                            notifyTime = state.notifyTime,
                                            groupFoodId = state.groupFoodId,
                                            note = state.note,
                                            createdDate = System.currentTimeMillis(), // Will be ignored on update
                                            createdBy = null,
                                            updatedDate = System.currentTimeMillis(),
                                            updatedBy = null,
                                            deletedDate = null,
                                            deletedBy = null
                                        )

                                        onEvent(FoodEvent.OnUpdateFood(updatedFood))
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .scale(scale),
                        interactionSource = interactionSource,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FacebookColors.PrimaryBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Update Food",
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                                is FoodViewModel.UiEvent.ShowSuccess -> {
                                    if (event.message.contains("updated")) {
                                        showSuccessDialog = true
                                        delay(1500) // Show success message for 1.5 seconds
                                        showSuccessDialog = false
                                        navController.popBackStack() // Navigate back
                                    }
                                }
                                is FoodViewModel.UiEvent.ShowError -> {
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
                                .background(Color.White), // Overlay màu tối bán trong suốt
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
                                        "Food Updated Successfully!",
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
