package com.genglxy.underroof

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.genglxy.underroof.logic.ConversationRepository
import com.genglxy.underroof.logic.FileRepository
import com.genglxy.underroof.logic.MessageRepository
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UnderRoofApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        UserRepository.initialize(this)
        MessageRepository.initialize(this)
        ConversationRepository.initialize(this)
        FileRepository.initialize(this)
        val userRepository = UserRepository.get()
        val job = Job()
        val scope = CoroutineScope(job)
        scope.launch {
            val users = userRepository.getUsers()
            for (user in users) {
                user.apply {
                    val newUser = User(
                        id,
                        photo,
                        photoThumbnail,
                        name,
                        gender,
                        genderPrivate,
                        age,
                        agePrivate,
                        statusEmoji,
                        statusText,
                        statusCreate,
                        introduction,
                        false,
                        ip
                    )
                    userRepository.addUser(newUser)
                }
            }
        }
    }
}