package com.brochurecraft.app.data.db.dao

import androidx.room.*
import com.brochurecraft.app.data.db.entity.BrandKitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandKitDao {
    @Query("SELECT * FROM brand_kit WHERE id = 1")
    fun observe(): Flow<BrandKitEntity?>

    @Query("SELECT * FROM brand_kit WHERE id = 1")
    suspend fun get(): BrandKitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(brandKit: BrandKitEntity)
}
