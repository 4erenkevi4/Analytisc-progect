package com.cher.analytics.fragments

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.cher.analytics.R
import com.cher.analytics.domain.FollowersDao
import com.cher.analytics.domain.repository.FollowersRepository
import com.cher.analytics.viewModel.ViewModelBase
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.experimental.property.inject

abstract class FragmentCompose : Fragment() {

    val viewModelBase: ViewModelBase by viewModel()
    val repository : FollowersRepository by inject()
    val sp : SharedPreferences by inject()

    lateinit var topBar: LinearLayout
    lateinit var buttonBack: ImageView
    lateinit var titleTopBar: TextView

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_compose, container, false)
        buttonBack = view.findViewById(R.id.button_back)
        titleTopBar = view.findViewById(R.id.toolbar_title)
        topBar = view.findViewById(R.id.topBar)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        view.findViewById<ComposeView>(R.id.compose_view).setContent {
            Greeting()
        }
    }

    @Composable
    abstract fun Greeting()
}