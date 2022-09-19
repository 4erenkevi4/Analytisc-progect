package com.cher.instaanalytics.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.cher.instaanalytics.R
import com.cher.instaanalytics.utils.Constants.Companion.PROFILE_KEY

class FragmentAutentification : FragmentCompose() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModelBase.initInstagramClient(null, null, context)
        viewModelBase.selfProfile.observe(viewLifecycleOwner) { profile ->
            if (profile.username.isNullOrEmpty())
                view.findViewById<ComposeView>(R.id.compose_view).setContent {
                    Greeting()
                }
            else {
                val bundle = bundleOf(PROFILE_KEY to profile)
                findNavController().navigate(
                    R.id.action_fragmentAutentification_to_fragmentSelfProfile,
                    bundle
                )
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun Greeting() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text(text = "email") },
                label = { Text(text = "email") })
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(text = "Password") },
                label = { Text(text = "Passsword") })
            Button(onClick = {
                if (email.isNotEmpty() && password.isNotEmpty())
                    viewModelBase.initInstagramClient(email, password, context)
            }) {
                Text(
                    text = "Авторизоваться",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
