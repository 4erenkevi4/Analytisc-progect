package com.cher.analytics.domain.repository

import android.app.Application
import com.cher.analytics.domain.FollowersDao
import com.cher.analytics.domain.FollowersDatabase
import com.github.instagram4j.instagram4j.models.user.Profile


class FollowersRepository ( application: Application
) {
    private val followersDao: FollowersDao by lazy {
        val db = FollowersDatabase.getInstance(application)
        db.getFollowersDao()
    }

    suspend fun insertListFollowers(list: List<Profile>) = followersDao.insertListFollowers(list)

    fun getFollowers() = followersDao.getFollowers()  // returns list followers and list followings

    fun getListFollowers() = followersDao.getListFollowers()

    fun getListFollowings() = followersDao.getListFollowings()

    fun getNumberFollowers() = followersDao.getNumberFollowers()

    fun getNumberFollowings() = followersDao.getNumberFollowings()


}