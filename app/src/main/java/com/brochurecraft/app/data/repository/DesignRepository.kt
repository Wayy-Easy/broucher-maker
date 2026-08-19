package com.brochurecraft.app.data.repository

import com.brochurecraft.app.data.db.dao.DesignDao
import com.brochurecraft.app.data.db.entity.DesignEntity
import kotlinx.coroutines.flow.Flow

class DesignRepository(private val dao: DesignDao) {
    fun observeAll(): Flow<List<DesignEntity>> = dao.observeAll()
    fun observeFavorites(): Flow<List<DesignEntity>> = dao.observeFavorites()
    fun observeRecent(limit: Int = 5): Flow<List<DesignEntity>> = dao.observeRecent(limit)
    fun observeById(id: Long): Flow<DesignEntity?> = dao.observeById(id)

    suspend fun getById(id: Long): DesignEntity? = dao.getById(id)
    suspend fun save(design: DesignEntity): Long = dao.insert(design)
    suspend fun update(design: DesignEntity) =
        dao.update(design.copy(updatedAt = System.currentTimeMillis()))
    suspend fun setFavorite(id: Long, fav: Boolean) = dao.setFavorite(id, fav)
    suspend fun delete(id: Long) = dao.delete(id)
}
