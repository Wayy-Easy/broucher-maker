package com.brochurecraft.app.data.db.dao

import androidx.room.*
import com.brochurecraft.app.data.db.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY id ASC")
    fun observeAll(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE category = :category ORDER BY id ASC")
    fun observeByCategory(category: String): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE name LIKE '%' || :query || '%' OR subtitle LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getById(id: Long): TemplateEntity?

    @Query("SELECT * FROM templates LIMIT :limit")
    fun observeFeatured(limit: Int = 4): Flow<List<TemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<TemplateEntity>)

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun count(): Int
}
