package com.brochurecraft.app

import android.app.Application
import com.brochurecraft.app.data.db.AppDatabase
import com.brochurecraft.app.data.prefs.UserPreferences
import com.brochurecraft.app.data.repository.BrandKitRepository
import com.brochurecraft.app.data.repository.DesignRepository
import com.brochurecraft.app.data.repository.TemplateRepository

class BrochureCraftApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var userPreferences: UserPreferences
        private set
    lateinit var designRepository: DesignRepository
        private set
    lateinit var templateRepository: TemplateRepository
        private set
    lateinit var brandKitRepository: BrandKitRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        userPreferences = UserPreferences(this)
        designRepository = DesignRepository(database.designDao())
        templateRepository = TemplateRepository(database.templateDao())
        brandKitRepository = BrandKitRepository(database.brandKitDao())
    }
}
