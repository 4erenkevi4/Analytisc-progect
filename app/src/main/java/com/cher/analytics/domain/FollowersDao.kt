package com.cher.analytics.domain

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cher.analytics.data.Followers
import com.cher.analytics.data.FormattedFollowers
import com.github.instagram4j.instagram4j.models.user.Profile
import com.github.instagram4j.instagram4j.models.user.User

@Dao
interface FollowersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListFollowers(followers: Followers)

    @Delete
    suspend fun deleteListFollowers(list: List<Profile>)

    @Query("SELECT * FROM followers_table ORDER BY saveTime ASC")
    fun getFollowers(): List<Followers>

    @Query("SELECT listFollowers  FROM followers_table ORDER BY saveTime  DESC LIMIT 1")
    fun getListFollowers(): List<FormattedFollowers>

    @Query("SELECT listFollowings  FROM followers_table ORDER BY saveTime  DESC LIMIT 1")
    fun getListFollowings(): List<FormattedFollowers>

    @Query("SELECT followersNumbers FROM followers_table   ORDER BY saveTime ASC")
    fun getNumberFollowers(): Int

    @Query("SELECT followingsNumbers FROM followers_table   ORDER BY saveTime ASC")
    fun getNumberFollowings(): Int
}