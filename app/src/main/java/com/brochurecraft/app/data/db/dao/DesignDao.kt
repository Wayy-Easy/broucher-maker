package com.brochurecraft.app.data.db.dao

import androidx.room.*
import com.brochurecraft.app.data.db.entity.DesignEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DesignDao {
    @Query("SELECT * FROM designs ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<DesignEntity>>

    @Query("SELECT * FROM designs WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<DesignEntity>>

    @Query("SELECT * FROM designs ORDER BY updatedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 5): Flow<List<DesignEntity>>

    @Query("SELECT * FROM designs WHERE id = :id")
    suspend fun getById(id: Long): DesignEntity?

    @Query("SELECT * FROM designs WHERE id = :id")
    fun observeById(id: Long): Flow<DesignEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(design: DesignEntity): Long

    @Update
    suspend fun update(design: DesignEntity)

    @Query("UPDATE designs SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("DELETE FROM designs WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM designs")
    suspend fun count(): Int
}
