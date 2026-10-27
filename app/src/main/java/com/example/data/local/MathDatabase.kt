package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedSolutionEntity::class], version = 1, exportSchema = false)
abstract class MathDatabase : RoomDatabase() {
    abstract fun savedSolutionDao(): SavedSolutionDao

    companion object {
        @Volatile
        private var INSTANCE: MathDatabase? = null

        fun getDatabase(context: Context): MathDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MathDatabase::class.java,
                    "sani_math_guide_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
