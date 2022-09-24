package com.cher.analytics.domain

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cher.analytics.data.Followers

@Database(
    entities = [Followers::class,],
    version = 1, exportSchema = false
)

abstract class FollowersDatabase : RoomDatabase() {

    abstract fun getFollowersDao(): FollowersDao
    companion object {

        @Volatile
        private var INSTANCE: FollowersDatabase? = null

        fun getInstance(context: Context): FollowersDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FollowersDatabase::class.java,
                    "followers_table"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}