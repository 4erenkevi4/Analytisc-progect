package com.cher.instaanalytics.fragments

import android.content.SharedPreferences
import android.icu.text.CaseMap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.cher.instaanalytics.R
import com.cher.instaanalytics.viewModel.ViewModelBase
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class FragmentCompose : Fragment() {

    val sharedPreferences: SharedPreferences by inject()
    val viewModelBase: ViewModelBase by viewModel()
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