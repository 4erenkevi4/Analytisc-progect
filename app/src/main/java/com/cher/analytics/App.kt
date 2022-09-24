package com.cher.analytics

import android.app.Application
import com.cher.analytics.di.followersDAO
import com.cher.analytics.di.sharedPreferences
import com.cher.analytics.di.viewModelBase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(viewModelBase, sharedPreferences, followersDAO))
        }
    }
}