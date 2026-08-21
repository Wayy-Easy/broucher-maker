package com.brochurecraft.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.brochurecraft.app.data.db.dao.BrandKitDao
import com.brochurecraft.app.data.db.dao.DesignDao
import com.brochurecraft.app.data.db.dao.TemplateDao
import com.brochurecraft.app.data.db.entity.BrandKitEntity
import com.brochurecraft.app.data.db.entity.DesignEntity
import com.brochurecraft.app.data.db.entity.TemplateEntity
import com.brochurecraft.app.data.model.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DesignEntity::class, TemplateEntity::class, BrandKitEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun designDao(): DesignDao
    abstract fun templateDao(): TemplateDao
    abstract fun brandKitDao(): BrandKitDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "brochurecraft_html_v1.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Trigger seeding when the database is first created
                    }

                    override fun onDestructiveMigration(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                    }
                }).build().also { 
                    INSTANCE = it
                    // Check if we need to seed
                    seedIfEmpty(it)
                }
            }
        }

        private fun seedIfEmpty(db: AppDatabase) {
            CoroutineScope(Dispatchers.IO).launch {
                if (db.templateDao().count() == 0) {
                    db.templateDao().insertAll(SeedData.templates())
                    db.brandKitDao().upsert(BrandKitEntity())
                    SeedData.sampleDesigns().forEach { db.designDao().insert(it) }
                }
            }
        }
    }
}
