package com.cher.analytics.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cher.analytics.data.Followers
import com.cher.analytics.data.FormattedFollowers
import com.cher.analytics.data.ListFollowers
import com.cher.analytics.domain.repository.FollowersRepository
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


    val repository = FollowersRepository(Application())

    private val _callError = MutableLiveData<Error>()
    val callError: LiveData<java.lang.Error> = _callError

    private val _selfProfile = MutableLiveData<User?>()
    val selfProfile: LiveData<User?> = _selfProfile

    private val _getFollowers = MutableLiveData<List<FormattedFollowers>>()
    val getFollowers: LiveData<List<FormattedFollowers>> = _getFollowers

    private val _getFollowings = MutableLiveData<List<FormattedFollowers>>()
    val getFollowings: LiveData<List<FormattedFollowers>> = _getFollowings

    private val _getUnFollowers = MutableLiveData<List<FormattedFollowers>>()
    val getUnFollowers: LiveData<List<FormattedFollowers>> = _getUnFollowers

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
        val followersNumber = repository.getNumberFollowers()
        val lastNamderofFollowers = getSP(context).getInt(SP_LAST_FOLLOWERS_NUMBER, 0)
        val isEqualsNumbers = followersNumber == lastNamderofFollowers
        _getFollowers.value =
            if (isEqualsNumbers) repository.getListFollowers() else responceFolowers(
                client,
                ifNeedAllList,
                ifNeedSave
            )

        if (ifNeedSave && isEqualsNumbers && _getFollowers.value != null)  //TODO возможно надо удалить флаг ifNeedSave
            makeSNapshotFolowers(context, _getFollowers.value!!)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun searchListUnFollowers(client: IGClient, context: Context, list: List<Profile>?) {
        val newList = mutableListOf<FormattedFollowers>()
        val followersNumber = repository.getNumberFollowers()
        val lastNumberOfFollowers = getSP(context).getInt(SP_LAST_FOLLOWERS_NUMBER, 0)
        if (followersNumber == lastNumberOfFollowers) {
            _getUnFollowers.value = listOf()
            return
        } else {
            val lastListofFollowers = repository.getListFollowers()
            val listFollowers = responceFolowers(
                client,
                ifNeedAllList = true,
                ifNeedFolowings = false
            )
            lastListofFollowers.forEach() { oldFollower ->
                if (listFollowers.any { it.username == oldFollower.username }.not()) {
                    newList.add(oldFollower)
                }
            }
            _getUnFollowers.value = newList

        }
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
    ): MutableList<FormattedFollowers> {
        val listFollowers = mutableListOf<FormattedFollowers>()
        viewModelScope.launch {
            var response =
                FriendshipsFeedsRequest(
                    client.selfProfile.pk,
                    if (ifNeedFolowings) FriendshipsFeedsRequest.FriendshipsFeeds.FOLLOWING else FriendshipsFeedsRequest.FriendshipsFeeds.FOLLOWERS
                ).execute(client)
                    .join()
            response.users.forEach() { profile ->
                listFollowers.add(
                    FormattedFollowers(
                        username = profile.username,
                        fullName = profile.full_name,
                        photoUrl = profile.profile_pic_url,
                        pk = profile.pk
                    )
                )
            }
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
                    response.users.forEach() { profile ->
                        listFollowers.add(
                            FormattedFollowers(
                                username = profile.username,
                                fullName = profile.full_name,
                                photoUrl = profile.profile_pic_url,
                                pk = profile.pk
                            )
                        )
                    }
                }
            }
        }
        return listFollowers
    }

    @SuppressLint("CommitPrefEdits")
    fun makeSNapshotFolowers(context: Context, listFollow: List<FormattedFollowers>) {
        val setUsernames = mutableSetOf<String>()
        listFollow.forEachIndexed { index, profile ->
            setUsernames.add(profile.username)
        }
        val listusers = repository.getFollowers().last().listFollowers?.listFollowers
        if (listusers.isNullOrEmpty()) {
            viewModelScope.launch {
                repository.insertListFollowers(
                    Followers(
                        listFollowers = ListFollowers(listFollow),
                        listFollow.size,
                        saveTime = System.currentTimeMillis()
                    )
                )
            }
        }

        /*  getSP(context).apply {
              if (this.contains(SP_LIST_OF_USERS_KEY).not())
              // this.edit().remove(SP_LIST_OF_USERS_KEY)
                  this.edit().putStringSet(SP_LIST_OF_USERS_KEY, setUsernames).apply()
          }*/
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
        sp.edit().putLong(SP_TIME_LAST_SAVED, System.currentTimeMillis()).apply()
        sp.edit().putInt(SP_LAST_FOLLOWERS_NUMBER, followers).apply()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun getFollowing(client: IGClient) {
        val listFollowers = mutableStateListOf<FormattedFollowers>()
        val response =
            FriendshipsFeedsRequest(
                client.selfProfile.pk,
                FriendshipsFeedsRequest.FriendshipsFeeds.FOLLOWING
            ).execute(client)
                .join()
        response.users.forEach { profile ->
            listFollowers.add(
                FormattedFollowers(
                    username = profile.username,
                    fullName = profile.full_name,
                    photoUrl = profile.profile_pic_url,
                    pk = profile.pk
                )
            )
        }
        println("followings Successfully uploaded")
        _getFollowings.value = listFollowers
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


    private fun getSP(context: Context): SharedPreferences = context.getSharedPreferences(
        Constants.APP_PREFERENCES,
        Context.MODE_PRIVATE
    )

    @RequiresApi(Build.VERSION_CODES.N)
    fun getCurrentProfile(igClient: IGClient) {
        val res: IGResponse = igClient.sendRequest(UsersInfoRequest(igClient.selfProfile.pk)).join()

    }
}