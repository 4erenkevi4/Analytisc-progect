package com.cher.analytics.fragments

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import coil.compose.rememberAsyncImagePainter
import com.cher.analytics.R
import com.cher.analytics.data.FormattedFollowers
import com.cher.analytics.utils.AutentificationClient
import com.cher.analytics.utils.Constants
import com.cher.analytics.utils.InstaAnalyticsUtils
import com.github.instagram4j.instagram4j.models.user.Profile


class FragmentListFolowers : FragmentCompose() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = context ?: return
        val folowersMode =
            arguments?.getSerializable(Constants.FOLOWERS_MODE_KEY) as? InstaAnalyticsUtils.UsersTypeList
        val client =
            AutentificationClient.getClientFromSerialize(
                Constants.IG_CLIENT_FILE_NAME,
                Constants.COOKIE_FILE_NAME,
                context
            )
        client?.let {
            titleTopBar.text = it.selfProfile.full_name
            if (folowersMode?.type == 1)
                viewModelBase.getFollowers(it, context, true)
            if (folowersMode?.type == 2)
                viewModelBase.searchListUnFollowers(it, context, null)

        }
        buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_global_fragmentAutentification)
        }
    }

    @Composable
    override fun Greeting() {
        val folowersMode = arguments?.getBoolean(Constants.FOLOWERS_MODE_KEY)
        val followersListState = viewModelBase.getFollowers.observeAsState()
        val unFolowersState = viewModelBase.getUnFollowers.observeAsState()
        if (followersListState.value.isNullOrEmpty().not() && folowersMode == true) {
            CreateListFolovers(followersListState.value!!)
        }
        if (unFolowersState.value.isNullOrEmpty().not()) {
            CreateListUnFollowers(unFolowersState.value!!)
        }
    }

    @Composable
    fun CreateListFolovers(mutableList: List<Profile>) {
        LazyColumn {
            itemsIndexed(mutableList) { index, profile ->
                Row(modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable {
                        InstaAnalyticsUtils.getToInstagramApp(
                            profile.username,
                            context
                        )
                    }) {
                    Text(
                        text = (index + 1).toString(),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(30.dp),
                        color = Color.DarkGray
                    )
                    Image(
                        painter = rememberAsyncImagePainter(profile.profile_pic_url),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .padding(horizontal = 8.dp)
                    )
                    Text(text = profile.username)
                }
            }
        }

    }

    @Composable
    fun CreateListUnFollowers(mutableSet: List<FormattedFollowers>?) {
        LazyColumn {
            itemsIndexed(mutableSet as List<FormattedFollowers>) { index, profile ->
                Row(modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable {
                        InstaAnalyticsUtils.getToInstagramApp(
                            profile.username,
                            context
                        )
                    }) {
                    Text(
                        text = (index + 1).toString(),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(30.dp),
                        color = Color.DarkGray
                    )
                    Image(
                        painter = rememberAsyncImagePainter(profile.photoUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .padding(horizontal = 8.dp)
                    )
                    Text(text = profile.username)
                }
            }
        }
    }
}