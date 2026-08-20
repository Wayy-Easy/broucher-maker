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
    version = 3,
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
                    "brochurecraft.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        seedDatabase()
                    }

                    override fun onDestructiveMigration(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        seedDatabase()
                    }

                    private fun seedDatabase() {
                        CoroutineScope(Dispatchers.IO).launch {
                            val instance = INSTANCE ?: return@launch
                            instance.templateDao().insertAll(SeedData.templates())
                            instance.brandKitDao().upsert(BrandKitEntity())
                            SeedData.sampleDesigns().forEach { instance.designDao().insert(it) }
                        }
                    }
                }).build().also { INSTANCE = it }
            }
        }
    }
}
