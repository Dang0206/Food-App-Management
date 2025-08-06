package com.example.finalproject.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.finalproject.data.dao.FoodDao
import com.example.finalproject.data.dao.FoodNotificationDao
import com.example.finalproject.data.dao.GroupFoodDao
import com.example.finalproject.data.entity.Food
import com.example.finalproject.data.entity.FoodNotification
import com.example.finalproject.data.entity.GroupFood

@Database(entities = [Food::class, GroupFood::class, FoodNotification::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun groupFoodDao(): GroupFoodDao
    abstract fun foodNotificationDao(): FoodNotificationDao

    companion object {
        private const val TAG = "AppDatabase"
        @Volatile private var INSTANCE: AppDatabase? = null

        // Callback to create database with  availble database
        private val roomCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d(TAG, "Create database - onCreate called")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            Log.d(TAG, " Instance of database")
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Create new instance ")

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_app_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build()


                INSTANCE = instance
                Log.d(TAG, "Creste Instance successfully")
                instance
            }
        }
    }
}