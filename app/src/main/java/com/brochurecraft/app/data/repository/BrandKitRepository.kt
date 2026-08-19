package com.brochurecraft.app.data.repository

import com.brochurecraft.app.data.db.dao.BrandKitDao
import com.brochurecraft.app.data.db.entity.BrandKitEntity
import kotlinx.coroutines.flow.Flow

class BrandKitRepository(private val dao: BrandKitDao) {
    fun observe(): Flow<BrandKitEntity?> = dao.observe()
    suspend fun get(): BrandKitEntity? = dao.get()
    suspend fun save(brandKit: BrandKitEntity) = dao.upsert(brandKit)
}
