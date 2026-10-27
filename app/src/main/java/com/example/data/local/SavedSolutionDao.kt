package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSolutionDao {
    @Query("SELECT * FROM saved_solutions ORDER BY timestamp DESC")
    fun getAllSolutions(): Flow<List<SavedSolutionEntity>>

    @Query("SELECT * FROM saved_solutions WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteSolutions(): Flow<List<SavedSolutionEntity>>

    @Query("SELECT * FROM saved_solutions WHERE id = :id LIMIT 1")
    suspend fun getSolutionById(id: Long): SavedSolutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(solution: SavedSolutionEntity): Long

    @Query("UPDATE saved_solutions SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Delete
    suspend fun delete(solution: SavedSolutionEntity)

    @Query("DELETE FROM saved_solutions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_solutions")
    suspend fun clearAll()
}
