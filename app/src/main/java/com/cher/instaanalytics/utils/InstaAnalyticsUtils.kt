package com.cher.instaanalytics.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import java.io.Serializable

object InstaAnalyticsUtils {

    fun <T : Serializable?> Bundle?.getFormattedSerializable(activity: Activity, name: String, clazz: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as T
    }
    fun getToInstagramApp(username: String, _context: Context?){
        val context = _context?: return
        var intent = context.packageManager.getLaunchIntentForPackage("com.instagram.android")
        if (intent != null) {
            // start the activity
            intent.data = Uri.parse("${Constants.INSTA_URL}$username/")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            // bring user to the market
            // or let them choose an app?
            intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = Uri.parse("market://details?id=" + "com.instagram.android")
            context.startActivity(intent)
        }
    }
}