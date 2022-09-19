package com.cher.instaanalytics

import android.app.Application
import com.cher.instaanalytics.di.sharedPreferences
import com.cher.instaanalytics.di.viewModelBase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(viewModelBase, sharedPreferences))
        }
    }
}