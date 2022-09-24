package com.cher.analytics.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.instagram4j.instagram4j.models.user.Profile
import com.github.instagram4j.instagram4j.models.user.User


@Entity(tableName = "followers_table")
data class Followers(
    val listFollowers: List<Profile>,
    val followersNumbers: Int,
    val listFollowings: List<Profile>,
    val followingsNumbers: Int,
    val saveTime: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}