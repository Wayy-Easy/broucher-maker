package com.brochurecraft.app.data.repository

import com.brochurecraft.app.data.db.dao.TemplateDao
import com.brochurecraft.app.data.db.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

class TemplateRepository(private val dao: TemplateDao) {
    fun observeAll(): Flow<List<TemplateEntity>> = dao.observeAll()
    fun observeByCategory(category: String): Flow<List<TemplateEntity>> = dao.observeByCategory(category)
    fun search(query: String): Flow<List<TemplateEntity>> = dao.search(query)
    fun observeFeatured(limit: Int = 4): Flow<List<TemplateEntity>> = dao.observeFeatured(limit)
    suspend fun getById(id: Long): TemplateEntity? = dao.getById(id)
}
