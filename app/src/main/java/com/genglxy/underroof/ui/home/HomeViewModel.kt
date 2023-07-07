package com.genglxy.underroof.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genglxy.underroof.UnderRoofApplication
import com.genglxy.underroof.logic.ConversationRepository
import com.genglxy.underroof.logic.MessageRepository
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.Conversation
import com.genglxy.underroof.logic.model.Message
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class HomeViewModel : ViewModel() {

    private val userRepository = UserRepository.get()
    private val _list = MutableLiveData<List<Conversation>>()
    val list: LiveData<List<Conversation>> = _list
    var onFront = false

    private val conversationRepository = ConversationRepository.get()
    private val messageRepository = MessageRepository.get()
    val conversationList = conversationRepository.getConversations()

    private val _conversations: MutableStateFlow<List<Conversation>> = MutableStateFlow(emptyList())
    val conversations: StateFlow<List<Conversation>>
        get() = _conversations.asStateFlow()
    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())
    val messages: StateFlow<List<Message>>
        get() = _messages.asStateFlow()
    var masterUUID = ""

    //private var _masterUUID: UUID? = null
    //val masterUUID: UUID
    //    get() = _masterUUID!!
    val masterLiveData = MutableLiveData<User>()
    /*
    private val _masterUUID: MutableStateFlow<List<UUID>> = MutableStateFlow(emptyList())
    val masterUUID: StateFlow<List<UUID>>
        get() = _masterUUID.asStateFlow()

     */

    init {
        //val prefs = UnderRoofApplication.context.getSharedPreferences("data", Context.MODE_PRIVATE)
        Log.d("chat", "4ViewModel instance created")
        //try{
        //    _masterUUID = UUID.fromString(prefs.getString("masterUUID", ""))
        //}catch (_:Exception) {

        //}
        viewModelScope.launch {
            conversationRepository.getConversations().collect {
                _conversations.value = it
            }
            messageRepository.getAllMessages().collect {
                _messages.value = it
            }/*
                        preferencesRepository.storedMasterUUID.collect {
                            try {
                                _masterUUID.value = listOf(UUID.fromString(it))
                            } catch (e: Exception) {
                                Log.e("HomeViewModel", "Failed to get masterUUID", e)
                            }
                        }

             */
        }
    }

    suspend fun setMaster() {
        Log.d("chat", "getConversationStart")
        val user = userRepository.getUser(UUID.fromString(masterUUID))
        withContext(Dispatchers.Main) {
            masterLiveData.value = user
        }
        //Log.d("chat", "$conversation")
    }

    suspend fun addConversation(conversation: Conversation) {
        conversationRepository.addConversation(conversation)
    }
}