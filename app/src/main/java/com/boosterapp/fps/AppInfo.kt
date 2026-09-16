package com.boosterapp.fps

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val isGame: Boolean,
    var isProtected: Boolean = false // true = boost ke time ye app kill nahi hogi
)
