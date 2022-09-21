package com.cher.analytics.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cher.analytics.utils.AutentificationClient
import com.cher.analytics.utils.Constants
import com.cher.analytics.utils.Constants.Companion.SP_LAST_FOLLOWERS_NUMBER
import com.cher.analytics.utils.Constants.Companion.SP_LIST_OF_USERS_KEY
import com.cher.analytics.utils.Constants.Companion.SP_TIME_LAST_SAVED
import com.cher.analytics.utils.Constants.Companion.TIME_TWO_DAYS_IN_MILLIS
import com.github.instagram4j.instagram4j.IGClient
import com.github.instagram4j.instagram4j.models.user.Profile
import com.github.instagram4j.instagram4j.models.user.User
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsFeedsRequest
import com.github.instagram4j.instagram4j.requests.users.UsersInfoRequest
import com.github.instagram4j.instagram4j.responses.IGResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ViewModelBase(context: Context) : ViewModel() {

    private val _callError = MutableLiveData<Error>()
    val callError: LiveData<java.lang.Error> = _callError

    private val _selfProfile = MutableLiveData<User?>()
    val selfProfile: LiveData<User?> = _selfProfile

    private val _getFollowers = MutableLiveData<List<Profile>>()
    val getFollowers: LiveData<List<Profile>> = _getFollowers

    private val _getFollowings = MutableLiveData<List<Profile>>()
    val getFollowings: LiveData<List<Profile>> = _getFollowings

    private val _getUnFollowers = MutableLiveData<List<String>>()
    val getUnFollowers: LiveData<List<String>> = _getUnFollowers

    private val _uploadFollowers = MutableLiveData<Int>()
    val uploadFollowers: LiveData<Int> = _uploadFollowers

    private val _ballanceFollowers = MutableLiveData<Int>()
    val ballanceFollowers: LiveData<Int> = _ballanceFollowers

    private val _listofBots = MutableLiveData<List<User>>()
    val listofBots: LiveData<List<User>> = _listofBots


    @RequiresApi(Build.VERSION_CODES.N)
    fun initInstagramClient(email: String?, password: String?, _context: Context?) {
        val context = _context ?: return
        var client =
            AutentificationClient.getClientFromSerialize("clien.ser", "cooki.ser", context)
        if (client == null && email.isNullOrEmpty().not() && password.isNullOrEmpty().not()) {
            AutentificationClient.serializeLogin(email, password, context)
            client =
                AutentificationClient.getClientFromSerialize("clien.ser", "cooki.ser", context)

        }
        //any profile =  client.actions().users().findByUsername("4erenkevi4")
        if (client?.selfProfile != null) {
            val res = client.sendRequest(UsersInfoRequest(client.selfProfile.pk)).join()
            _selfProfile.value = res.user ?: User()
        } else {
            _selfProfile.value = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getFollowers(
        client: IGClient,
        context: Context,
        ifNeedSave: Boolean,
        ifNeedAllList: Boolean = false,
    ) {
        val listFollowers = responceFolowers(client, ifNeedAllList, ifNeedSave)
        _getFollowers.value = listFollowers
        if (ifNeedSave)
            makeSNapshotFolowers(context, listFollowers)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getFollowings(client: IGClient, ifNeedAllList: Boolean = false) {
        val listFollowings = responceFolowers(client, ifNeedAllList, true)
        _getFollowings.value = listFollowings

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun responceFolowers(
        client: IGClient,
        ifNeedAllList: Boolean = false,
        ifNeedFolowings: Boolean = false
    ): MutableList<Profile> {
        val listFollowers = mutableListOf<Profile>()
        viewModelScope.launch {
            var response =
                FriendshipsFeedsRequest(
                    client.selfProfile.pk,
                    if (ifNeedFolowings) FriendshipsFeedsRequest.FriendshipsFeeds.FOLLOWING else FriendshipsFeedsRequest.FriendshipsFeeds.FOLLOWERS
                ).execute(client)
                    .join()
            listFollowers.addAll(response.users)
            _uploadFollowers.value = response.users.size
            println("${response.users.size} users successfully uploaded")
            if (ifNeedAllList) {
                while (response.isMore_available) {

                    response = FriendshipsFeedsRequest(
                        client.selfProfile.pk,
                        FriendshipsFeedsRequest.FriendshipsFeeds.FOLLOWERS, response.next_max_id
                    ).execute(client)
                        .join()
                    _uploadFollowers.value = response.users.size
                    println("${response.users.size} users successfully uploaded")
                    listFollowers.addAll(response.users)
                }
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
            if (this.contains(SP_LIST_OF_USERS_KEY).not())
            // this.edit().remove(SP_LIST_OF_USERS_KEY)
                this.edit().putStringSet(SP_LIST_OF_USERS_KEY, set).apply()
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun checkListFollowersInSP(context: Context, followers: Int) {
        getSP(context).apply {
            if (this.contains(SP_TIME_LAST_SAVED)) {
                val lastTime = this.getLong(SP_TIME_LAST_SAVED, 0)
                if (System.currentTimeMillis() > lastTime + TIME_TWO_DAYS_IN_MILLIS) {
                    saveCurrentListToSP(this, followers)
                }
            } else {
                saveCurrentListToSP(this, followers)
            }
        }
        val oldFollowers = getSP(context).getInt(SP_LAST_FOLLOWERS_NUMBER, followers)
        _ballanceFollowers.value = followers - oldFollowers
        //if value <0 it means it is unFollowers for the Last 2 days
    }


    @SuppressLint("CommitPrefEdits")
    private fun saveCurrentListToSP(sp: SharedPreferences, followers: Int) {
        sp.edit().putLong(SP_TIME_LAST_SAVED, System.currentTimeMillis())
        sp.edit().putInt(SP_LAST_FOLLOWERS_NUMBER, followers)
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
    fun searchBots(client: IGClient, list: List<Profile>) {
        val listBots = mutableListOf<User>()
        var timeLaps = 10
        list.forEachIndexed { index, profile ->
            var user: User? = null
            viewModelScope.launch(Dispatchers.IO) {
                user = getUser(client, profile.pk)
                delay((100 + timeLaps).toLong())
            }
                .invokeOnCompletion {
                    user?.let {
                        val isintrested =
                            (it.extraProperties["is_interest_account"] ?: false) as Boolean
                        if (it.following_count > 1000 && (it.follower_count < 300 || it.media_count < 11) && isintrested) {
                            listBots.add(it)
                            println("Найдено: ${listBots.size} бот/ботов  ")
                        }
                    }
                    timeLaps += 10
                    if (timeLaps == 50)
                        timeLaps = 10
                }
            println("User $index из ${list.size} обработан")
        }
        _listofBots.value = listBots
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getUser(client: IGClient, pk: Long): User {
        val req2 = UsersInfoRequest(pk)
        return client.sendRequest(req2).join().user
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun searchListUnFollowers(client: IGClient, context: Context, list: List<Profile>?) {
        val sp = getSP(context)
        val newList = mutableListOf<String>()
        val oldList = sp.getStringSet(SP_LIST_OF_USERS_KEY, mutableSetOf())
        val listFollowers = list ?: responceFolowers(client)
        oldList?.let { oldUser ->
            oldUser.forEach { username ->
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

    @RequiresApi(Build.VERSION_CODES.N)
    fun getCurrentProfile(igClient: IGClient) {
        val res: IGResponse = igClient.sendRequest(UsersInfoRequest(igClient.selfProfile.pk)).join()

    }
}