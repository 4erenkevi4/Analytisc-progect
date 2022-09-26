package com.cher.analytics.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.instagram4j.instagram4j.models.user.Profile
import com.github.instagram4j.instagram4j.models.user.User


@Entity(tableName = "followers_table")
@TypeConverters(Converter::class)
data class Followers(
    var listFollowers: ListFollowers? = null,
    val followersNumbers: Int? = null,
    var listFollowings: ListFollowers? = null,
    val followingsNumbers: Int? = null,
    val saveTime: Long? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

@TypeConverters(Converter::class)
data class ListFollowers(val listFollowers: List<FormattedFollowers> = listOf())

data class FormattedFollowers(
    val username: String = "",
    val fullName: String = "",
    val photoUrl: String = "",
    val pk: Long = 0,
)

