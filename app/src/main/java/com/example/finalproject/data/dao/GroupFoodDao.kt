package com.example.finalproject.data.dao

import androidx.room.*
import com.example.finalproject.data.entity.GroupFood
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupFoodDao {

    @Insert
    suspend fun insert(groupFood: GroupFood): Long

    @Update
    suspend fun update(groupFood: GroupFood)

    @Delete
    suspend fun delete(groupFood: GroupFood)

    @Query("SELECT * FROM groupFood WHERE deletedDate IS NULL")
    fun getAllGroupFood(): Flow<List<GroupFood>>

    @Query("SELECT * FROM groupFood WHERE id = :id AND deletedDate IS NULL")
    suspend fun getGroupFoodById(id: Long): GroupFood?

    @Query("UPDATE groupFood SET deletedDate = :deletedDate, deletedBy = :deletedBy WHERE id = :id")
    suspend fun softDelete(id: Long, deletedDate: Long, deletedBy: Long)
}



