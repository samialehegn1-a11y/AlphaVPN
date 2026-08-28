package com.example.vpn.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val icon: Drawable? = null
)
