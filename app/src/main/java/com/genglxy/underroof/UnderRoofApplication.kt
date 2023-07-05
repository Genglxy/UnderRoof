package com.genglxy.underroof

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.genglxy.underroof.logic.ConversationRepository
import com.genglxy.underroof.logic.PreferencesRepository
import com.genglxy.underroof.logic.UserRepository

class UnderRoofApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        UserRepository.initialize(this)
        ConversationRepository.initialize(this)
        PreferencesRepository.initialize(this)
    }
}