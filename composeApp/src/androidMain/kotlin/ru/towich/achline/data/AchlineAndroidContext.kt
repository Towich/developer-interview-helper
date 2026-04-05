package ru.towich.achline.data

import android.content.Context

object AchlineAndroidContext {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun require(): Context = appContext ?: error("AchlineAndroidContext.init not called")
}
