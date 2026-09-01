package com.novel.continueapp

import android.app.Application
import com.novel.continueapp.data.NovelRepository
import com.novel.continueapp.data.SettingsRepository

class NovelApp : Application() {

    lateinit var repository: NovelRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsRepository = SettingsRepository(this)
        repository = NovelRepository(this)
    }

    companion object {
        @Volatile lateinit var instance: NovelApp
            private set
    }
}