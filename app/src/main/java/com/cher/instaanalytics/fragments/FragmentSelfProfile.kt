package com.cher.instaanalytics.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import coil.compose.rememberAsyncImagePainter
import com.cher.instaanalytics.R
import com.cher.instaanalytics.utils.Constants.Companion.FOLOWERS_MODE_KEY
import com.cher.instaanalytics.utils.Constants.Companion.PROFILE_KEY
import com.github.instagram4j.instagram4j.models.user.Profile


class FragmentSelfProfile : FragmentCompose() {

    private var selfProfile: Profile? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val profile = arguments?.getSerializable(PROFILE_KEY) as Profile?
        selfProfile = profile
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun Greeting() {
        selfProfile?.let { selfProfile ->
            if (selfProfile.username.isNullOrEmpty().not()) {
                titleTopBar.text = selfProfile.username
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(selfProfile.profile_pic_url),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .padding(vertical = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(text = "${selfProfile.full_name} - вход выполнен.")
                    Button(modifier = Modifier
                        .padding(top = 16.dp)
                        .background(Color.White), onClick = {
                        findNavController().navigate(
                            R.id.action_fragmentSelfProfile_to_fragmentListFolowers, bundleOf(FOLOWERS_MODE_KEY to true)
                        )
                    }) {
                        Text(
                            text = "Список подписчиков",
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(modifier = Modifier
                        .padding(top = 16.dp)
                        .background(Color.White), onClick = {
                        findNavController().navigate(
                            R.id.action_fragmentSelfProfile_to_fragmentListFolowers, bundleOf(FOLOWERS_MODE_KEY to false)
                        )
                    }) {
                        Text(
                            text = "Кто отписался?",
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }



    @Composable
    private fun ProgressBar(responseState: MutableState<Boolean>) {
        if (responseState.value)
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    Modifier.padding(16.dp),
                    color = Color.Magenta,
                    strokeWidth = Dp(value = 4F)
                )
                val uploadUsers = viewModelBase.uploadFollowers.observeAsState()
                Text(text = "Загружено ${uploadUsers.value} подписчиков")
            }
    }

}

