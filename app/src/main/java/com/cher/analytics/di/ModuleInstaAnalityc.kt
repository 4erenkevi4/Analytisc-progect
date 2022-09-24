package com.cher.analytics.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.cher.analytics.domain.repository.FollowersRepository
import com.cher.analytics.utils.Constants.Companion.APP_PREFERENCES
import com.cher.analytics.viewModel.ViewModelBase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.experimental.property.inject

val viewModelBase = module {
    single { ViewModelBase(androidContext())}
}

val sharedPreferences = module {
    single { provideSharedPref(androidApplication()) }
}

val followersDAO = module {
    single { FollowersRepository(androidApplication()) }
}

fun provideSharedPref(app: Application): SharedPreferences {
    return app.applicationContext.getSharedPreferences(
        APP_PREFERENCES,
        Context.MODE_PRIVATE
    )
}

