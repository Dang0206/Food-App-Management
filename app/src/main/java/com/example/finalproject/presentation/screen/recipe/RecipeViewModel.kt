package com.example.finalproject.presentation.screen.recipe

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.AppDatabase
import com.example.finalproject.data.dao.FoodDao
import com.example.finalproject.data.entity.Food
import com.example.finalproject.data.entity.Recipe
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class RecipeViewModel(
    private val foodDao: FoodDao,
    private val context: Context
) : ViewModel() {

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _currentBatch = MutableLiveData<Int>()
    val currentBatch: LiveData<Int> = _currentBatch

    private val _debugResponse = MutableLiveData<String?>()
    val debugResponse: LiveData<String?> = _debugResponse

    fun generateSelectedRecipes(apiKey: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _debugResponse.value = null
                _currentBatch.value = 0

                val allFoods: List<Food> = foodDao.getAllFood().first()
                Log.d("RecipeViewModel", "Found foods: ${allFoods.size}")

                if (allFoods.isEmpty()) {
                    _recipes.value = emptyList()
                    return@launch
                }

                val selectedCombinations = if (allFoods.size == 1) {
                    listOf(allFoods)
                } else {
                    generateFoodCombinations(allFoods)
                }

                Log.d("RecipeViewModel", "Generated ${selectedCombinations.size} combinations")

                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.0-flash",
                    apiKey = apiKey
                )

                val allRecipes = mutableListOf<Recipe>()

                for ((index, combination) in selectedCombinations.withIndex()) {
                    val recipe = generateEnhancedRecipeForCombination(generativeModel, combination)
                    recipe?.let { allRecipes.add(it) }
                    _currentBatch.postValue(index + 1)
                    delay(1500)
                }

                _recipes.value = allRecipes
            } catch (e: Exception) {
                _error.value = "Error generating recipes: ${e.message}"
                Log.e("RecipeViewModel", "Exception: ", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateFoodCombinations(foods: List<Food>): List<List<Food>> {
        val result = mutableSetOf<List<Food>>()
        val random = java.util.Random()
        val maxRecipes = minOf(foods.size, 5)

        while (result.size < maxRecipes) {
            val size = 1 + random.nextInt(foods.size - 1)
            val combo = foods.shuffled().take(size).sortedBy { it.name }
            result.add(combo)
        }

        return result.toList()
    }

    // Create a unique recipe for each food combination using Gemini
    private suspend fun generateEnhancedRecipeForCombination(
        generativeModel: GenerativeModel,
        foodCombination: List<Food>
    ): Recipe? {
        val foodNames = foodCombination.joinToString(", ") { it.name }

        val prompt = """
            Using the following ingredients: $foodNames,
            create a unique and practical recipe.
            Add common spices or oil if needed.
            Return ONLY a valid JSON object in this format:
            {
                "title": "Dish Name",
                "description": "2-3 sentence description (appearance, taste, experience)",
                "cookingTime": "30 minutes",
                "ingredients": ["${foodCombination.joinToString("\", \"") { it.name }}", "salt", "oil"],
                "steps": ["Step 1", "Step 2", "Step 3", "Final step"]
            }
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            val rawResponse = response.text?.trim()

            Log.d("RecipeViewModel", "Response for $foodNames: ${rawResponse?.take(150)}")

            if (rawResponse != null) {
                val jsonString = extractJsonFromText(rawResponse)
                val recipeData = Gson().fromJson(jsonString, Map::class.java) as Map<String, Any>

                Recipe(
                    title = recipeData["title"] as? String ?: "Delicious Recipe",
                    description = recipeData["description"] as? String
                        ?: "A wonderful dish made with fresh ingredients.",
                    cookingTime = recipeData["cookingTime"] as? String ?: "30 minutes",
                    ingredients = (recipeData["ingredients"] as? List<*>)?.map { it.toString() }
                        ?: listOf("ingredients"),
                    steps = (recipeData["steps"] as? List<*>)?.map { it.toString() }
                        ?: listOf("cooking steps")
                )
            } else null
        } catch (e: Exception) {
            Log.e("RecipeViewModel", "Failed to generate recipe for: $foodNames", e)
            null
        }
    }

    // Extract JSON from the raw response text
    private fun extractJsonFromText(text: String): String {
        Log.i("RecipeViewModel", "Raw response: $text")
        val cleanText = text.replace("```json", "").replace("```", "").trim()

        val jsonStart = cleanText.indexOf("{")
        val jsonEnd = cleanText.lastIndexOf("}") + 1

        return if (jsonStart != -1 && jsonEnd > jsonStart) {
            cleanText.substring(jsonStart, jsonEnd)
        } else {
            createFallbackJson()
        }
    }

    private fun createFallbackJson(): String {
        return """
        {
            "title": "Simple Recipe",
            "description": "A simple dish using available ingredients.",
            "cookingTime": "30 minutes",
            "ingredients": ["Ingredient 1", "Salt", "Oil"],
            "steps": ["Prepare ingredients", "Cook", "Serve"]
        }
        """.trimIndent()
    }
}

class RecipeViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(db.foodDao(), application.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}