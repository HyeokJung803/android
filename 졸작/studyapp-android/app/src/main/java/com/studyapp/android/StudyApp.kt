package com.studyapp.android

import android.app.Application
import com.studyapp.android.util.PreferenceManager

class StudyApp : Application() {
    companion object {
        var currentUserId: Long = 0L
        var currentNickname: String = ""

        lateinit var instance: StudyApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this


        currentUserId = PreferenceManager.getUserId(this)
        currentNickname = PreferenceManager.getNickname(this)

        println("🟢 앱 시작 - userId=$currentUserId, nickname=$currentNickname")
    }
}