package com.cher.instaanalytics.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cher.instaanalytics.utils.AutentificationClient
import com.cher.instaanalytics.utils.Constants
import com.cher.instaanalytics.utils.Constants.Companion.SP_LIST_OF_USERS_KEY
import com.github.instagram4j.instagram4j.IGClient
import com.github.instagram4j.instagram4j.models.user.Profile
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsFeedsRequest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.dsl.koinApplication
import org.koin.experimental.property.inject
import java.util.function.Consumer

class ViewModelBase(context: Context) : ViewModel() {

    private val _callError = MutableLiveData<Error>()
    val callError: LiveData<java.lang.Error> = _callError

    private val _selfProfile = MutableLiveData<Profile>()
    val selfProfile: LiveData<Profile> = _selfProfile

    private val _getFollowers = MutableLiveData<List<Profile>>()
    val getFollowers: LiveData<List<Profile>> = _getFollowers

    private val _getUnFollowers = MutableLiveData<List<String>>()
    val getUnFollowers: LiveData<List<String>> = _getUnFollowers

    private val _uploadFollowers = MutableLiveData<Int>()
    val uploadFollowers: LiveData<Int> = _uploadFollowers


    @RequiresApi(Build.VERSION_CODES.N)
    fun initInstagramClient(email: String?, password: String?, _context: Context?) {
        val context = _context ?: return
        var client =
            AutentificationClient.getClientFromSerialize("client.ser", "cookie.ser", context)
        if (client == null && email.isNullOrEmpty().not() && password.isNullOrEmpty().not()) {
            AutentificationClient.serializeLogin(email, password, context)
            client =
                AutentificationClient.getClientFromSerialize("client.ser", "cookie.ser", context)

        }
        //any profile =  client.actions().users().findByUsername("4erenkevi4")
        _selfProfile.value = client?.selfProfile ?: Profile()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getFollowers(client: IGClient, context: Context, ifNeedSave: Boolean) {
        val listFollowers = responceFolowers(client)
        _getFollowers.value = listFollowers
        if (ifNeedSave)
            makeSNapshotFolowers(context, listFollowers)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun responceFolowers(client: IGClient): MutableList<Profile> {
        val listFollowers = mutableListOf<Profile>()
        viewModelScope.launch {
            var response =
                FriendshipsFeedsRequest(
                    client.selfProfile.pk,
                    FriendshipsFeedsRequest.FriendshipsFeeds.FOLLOWERS
                ).execute(client)
                    .join()
            listFollowers.addAll(response.users)
            _uploadFollowers.value = response.users.size
            println("${response.users.size} followers successfully uploaded")

            while (response.isMore_available) {

                response = FriendshipsFeedsRequest(
                    client.selfProfile.pk,
                    FriendshipsFeedsRequest.FriendshipsFeeds.FOLLOWERS, response.next_max_id
                ).execute(client)
                    .join()
                _uploadFollowers.value = response.users.size
                println("${response.users.size} followers successfully uploaded")
                listFollowers.addAll(response.users)
            }
        }
        return listFollowers
    }

    @SuppressLint("CommitPrefEdits")
    fun makeSNapshotFolowers(context: Context, listFollowers: MutableList<Profile>) {
        val set = mutableSetOf<String>()
        listFollowers.forEach { profile ->
            set.add(profile.username)
        }
        getSP(context).apply {
            if (this.contains(SP_LIST_OF_USERS_KEY))
                this.edit().remove(SP_LIST_OF_USERS_KEY)
            this.edit().putStringSet(SP_LIST_OF_USERS_KEY, set).apply()
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun getFollowing(client: IGClient) {
        val listFollowers = mutableStateListOf<Profile>()
        val response =
            FriendshipsFeedsRequest(
                client.selfProfile.pk,
                FriendshipsFeedsRequest.FriendshipsFeeds.FOLLOWING
            ).execute(client)
                .join()
        listFollowers.addAll(response.users)
        println("Users Successfully uploaded")
        _getFollowers.value = listFollowers
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun searchUnFollowers(client: IGClient, context: Context) {
        val sp = getSP(context)
        val newList = mutableListOf<String>()
        val oldList = sp.getStringSet(SP_LIST_OF_USERS_KEY, mutableSetOf())
        val listFollowers = responceFolowers(client)
        oldList?.let {
            it.forEach { username ->
                if (listFollowers.any { it.username == username }.not()) {
                    newList.add(username)
                }

            }
        }
        _getUnFollowers.value = newList
    }

    private fun getSP(context: Context): SharedPreferences = context.getSharedPreferences(
        Constants.APP_PREFERENCES,
        Context.MODE_PRIVATE
    )
}