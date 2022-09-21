package com.cher.analytics.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.cher.analytics.utils.Constants.Companion.APP_PREFERENCES
import com.cher.analytics.viewModel.ViewModelBase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val viewModelBase = module {
    single { ViewModelBase(androidContext()) }
}

val sharedPreferences = module {
    single { provideSharedPref(androidApplication()) }
}

fun provideSharedPref(app: Application): SharedPreferences {
    return app.applicationContext.getSharedPreferences(
        APP_PREFERENCES,
        Context.MODE_PRIVATE
    )
}

