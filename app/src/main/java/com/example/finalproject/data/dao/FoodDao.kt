package com.example.finalproject.data.dao

import androidx.room.*
import com.example.finalproject.data.entity.Food
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Insert
    suspend fun insert(food: Food): Long

    @Update
    suspend fun update(food: Food)

    @Delete
    suspend fun delete(food: Food)

    @Query("SELECT * FROM food WHERE deletedDate IS NULL")
    fun getAllFood(): Flow<List<Food>>

    @Query("SELECT * FROM food WHERE id = :id AND deletedDate IS NULL")
    suspend fun getFoodById(id: Long): Food?

    @Query("UPDATE food SET deletedDate = :deletedDate, deletedBy = :deletedBy WHERE id = :id")
    suspend fun softDelete(id: Long, deletedDate: Long, deletedBy: Long)

    @Query("SELECT * FROM food WHERE groupFoodId = :groupId AND deletedDate IS NULL")
    fun getFoodByGroupId(groupId: Long): Flow<List<Food>>

    // Add method to get foods count by group (for debugging)
    @Query("SELECT COUNT(*) FROM food WHERE groupFoodId = :groupId AND deletedDate IS NULL")
    suspend fun getFoodCountByGroupId(groupId: Long): Int

}

