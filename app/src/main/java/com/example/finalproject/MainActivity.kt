package com.example.finalproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.finalproject.data.AppDatabase
import com.example.finalproject.presentation.screen.food.*
import com.example.finalproject.presentation.screen.groupfood.*
import com.example.finalproject.presentation.screen.recipe.RecipeScreen
import com.example.finalproject.presentation.screen.recipe.RecipeViewModel
import com.example.finalproject.presentation.screen.recipe.RecipeViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class BottomNavScreen(val route: String, val title: String, val icon: ImageVector) {
    data object Food : BottomNavScreen("foodList", "Home", Icons.Filled.Home)
    data object Group : BottomNavScreen("groupFoodList", "Group Item", Icons.AutoMirrored.Filled.List)
    data object Recipe : BottomNavScreen("recipe", "Recipe", Icons.Filled.LocalCafe)
}

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MainActivity onCreate ")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(application)
                db.groupFoodDao().getAllGroupFood().collect { groups ->
                    Log.d(TAG, "Find ${groups.size} group in database")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
            }
        }

        setContent {
            RequestNotificationPermission()

            MaterialTheme {
                val navController = rememberNavController()
                val foodFactory = FoodViewModel.Factory(application)
                val foodViewModel: FoodViewModel = viewModel(factory = foodFactory)
                val groupFoodFactory = GroupFoodViewModel.Factory(application)
                val groupFoodViewModel: GroupFoodViewModel = viewModel(factory = groupFoodFactory)

                // Debug navigation changes
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                LaunchedEffect(currentBackStackEntry) {
                    Log.d(TAG, "Current route: ${currentBackStackEntry?.destination?.route}")
                }

                LaunchedEffect(true) {
                    foodViewModel.scheduleAllNotifications()
                }

                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavScreen.Food.route,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        composable(BottomNavScreen.Food.route) {
                            Log.d(TAG, "Navigated to Food List Screen")
                            LaunchedEffect(Unit) {
                                // Reset state and load all foods
                                foodViewModel.onEvent(FoodEvent.ResetFormState)
                                foodViewModel.onEvent(FoodEvent.LoadAllFoods)
                            }
                            FoodListScreen(
                                navController = navController,
                                state = foodViewModel.state,
                                onEvent = foodViewModel::onEvent
                            )
                        }

                        composable(BottomNavScreen.Group.route) {
                            Log.d(TAG, "Navigated to Group Food List Screen")
                            LaunchedEffect(Unit) {
                                groupFoodViewModel.onEvent(GroupFoodEvent.LoadAllGroupFoods)
                            }
                            GroupFoodListScreen(
                                navController = navController,
                                state = groupFoodViewModel.state,
                                onEvent = groupFoodViewModel::onEvent
                            )
                        }

                        composable(BottomNavScreen.Recipe.route) {
                            val viewModel = viewModel<RecipeViewModel>(
                                factory = RecipeViewModelFactory(application)
                            )
                            RecipeScreen(viewModel = viewModel)
                        }

                        composable("addFood") {
                            Log.d(TAG, "Navigated to Add Food Screen")
                            // Reset form state and load group foods
                            LaunchedEffect(Unit) {
                                foodViewModel.resetFormState()
                                foodViewModel.onEvent(FoodEvent.LoadAllGroupFoods)
                            }
                            AddFoodScreen(
                                navController = navController,
                                state = foodViewModel.state,
                                onEvent = foodViewModel::onEvent,
                                viewModel = foodViewModel
                            )
                        }

                        composable(
                            route = "editFood/{foodId}",
                            arguments = listOf(navArgument("foodId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val foodId = backStackEntry.arguments?.getLong("foodId") ?: 0L
                            Log.d(TAG, "Navigated to Edit Food Screen for ID: $foodId")
                            // Reset form state và load group foods
                            LaunchedEffect(foodId) {
                                foodViewModel.resetFormState()
                                foodViewModel.onEvent(FoodEvent.LoadAllGroupFoods)
                            }
                            EditFoodScreen(
                                navController = navController,
                                foodId = foodId,
                                state = foodViewModel.state,
                                onEvent = foodViewModel::onEvent,
                                viewModel = foodViewModel
                            )
                        }

                        composable("addGroupFood") {
                            Log.d(TAG, "Navigated to Add Group Food Screen")
                            // Reset form state
                            LaunchedEffect(Unit) {
                                groupFoodViewModel.onEvent(GroupFoodEvent.LoadAllGroupFoods)
                            }
                            AddGroupFoodScreen(
                                navController = navController,
                                state = groupFoodViewModel.state,
                                onEvent = groupFoodViewModel::onEvent,
                                viewModel = groupFoodViewModel
                            )
                        }

                        composable(
                            route = "editGroupFood/{groupFoodId}",
                            arguments = listOf(navArgument("groupFoodId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val groupFoodId = backStackEntry.arguments?.getLong("groupFoodId") ?: 0L
                            Log.d(TAG, "Navigated to Edit Group Food Screen for ID: $groupFoodId")
                            EditGroupFoodScreen(
                                navController = navController,
                                groupFoodId = groupFoodId,
                                state = groupFoodViewModel.state,
                                onEvent = groupFoodViewModel::onEvent,
                                viewModel = groupFoodViewModel
                            )
                        }


                        composable(
                            route = "foods/group/{groupId}",
                            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                            Log.d(TAG, "Navigated to Foods by Group Screen for Group ID: $groupId")


                            LaunchedEffect(groupId) {
                                Log.d(TAG, "Loading foods for group ID: $groupId")
                                foodViewModel.resetFormState()
                                foodViewModel.onEvent(FoodEvent.LoadAllGroupFoods)
                                foodViewModel.onEvent(FoodEvent.GetFoodsByGroupId(groupId))
                            }
                            // Debug: Show current foods count
                            LaunchedEffect(foodViewModel.state.foods) {
                                Log.d(TAG, "Foods loaded for group $groupId: ${foodViewModel.state.foods.size} items")
                                foodViewModel.state.foods.forEach { food ->
                                    Log.d(TAG, "Food: ${food.name}, GroupId: ${food.groupFoodId}")
                                }
                            }

                            FoodsByGroupScreen(
                                navController = navController,
                                state = foodViewModel.state,
                                onEvent = foodViewModel::onEvent,
                                groupId = groupId
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavScreen.Food,
        BottomNavScreen.Group,
        BottomNavScreen.Recipe
    )
    val facebookBlue = Color(0xFF1877F2)

    NavigationBar {
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title, tint = facebookBlue ) },
                label = { Text(screen.title) },
                selected = isRouteSelected(currentDestination, screen.route),
                onClick = {
                    Log.d("MainActivity", "Bottom nav clicked: ${screen.route}")
                    Log.d("MainActivity", "Current route: ${currentDestination?.route}")

                    // Force clear navigation to ensure clean state
                    navController.navigate(screen.route) {
                        // Clear entire back stack
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }
    }
}


private fun isRouteSelected(currentDestination: NavDestination?, targetRoute: String): Boolean {
    val currentRoute = currentDestination?.route

    return when (targetRoute) {
        BottomNavScreen.Food.route -> {
            currentRoute == BottomNavScreen.Food.route ||
                    currentRoute?.startsWith("foods/group/") == true
        }
        BottomNavScreen.Group.route -> {
            currentRoute == BottomNavScreen.Group.route
        }
        BottomNavScreen.Recipe.route -> {
            currentRoute == BottomNavScreen.Recipe.route
        }
        else -> currentRoute == targetRoute
    }
}

@Composable
fun RequestNotificationPermission() {
    var permissionRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(permissionRequested) {
        if (!permissionRequested && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            permissionRequested = true
        }
    }
}