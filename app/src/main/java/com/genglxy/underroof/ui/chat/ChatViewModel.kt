package com.genglxy.underroof.ui.chat

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val conversationRepository = ConversationRepository.get()
    private val messageRepository = MessageRepository.get()
    private val userRepository = UserRepository.get()

    lateinit var users: List<User>
    //var messageList = messageRepository.getMessages()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>>
        get() = _messages

    private var _inputText = ""

    val inputText
        get() = _inputText

    fun writeInputText(text: String) {
        _inputText = text
    }

    private var _masterUUID: UUID? = null
    val masterUUID: UUID
        get() = _masterUUID!!

    var chatId: UUID? = null

    private var _conversation: Conversation? = null
    val conversation: Conversation
        get() = _conversation!!

    val conversationLiveData = MutableLiveData<Conversation>()

    val masterLiveData = MutableLiveData<User>()

    init {
        Log.d("chat", "ViewModel instance created")
        val prefs = UnderRoofApplication.context.getSharedPreferences("data", Context.MODE_PRIVATE)
        Log.d("chat", "44ViewModel instance created")
        _masterUUID = UUID.fromString(prefs.getString("masterUUID", ""))
        Log.d("chat", "uuid $masterUUID")
        viewModelScope.launch {
            /*
            messageRepository.getAllMessages().collect {
                _messages.value = it
            }
             */
            while (true) {
                Log.d("final7", "try start")
                delay(150)
                Log.d("final7", "delay over")
                if (chatId != null) {
                    Log.d("final7", "chatId $chatId")
                    messageRepository.getMessages(chatId!!).collect {
                        Log.d("final7", "message collected $it")
                        _messages.value = it
                    }
                    break
                }
            }
            /*
            while (true) {
                delay(100)
                val temp = messageRepository.getRMessages()
                withContext(Dispatchers.Main) { _messages.value = temp }
            }

             */
        /*
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

    suspend fun setConversation() {

        Log.d("chat", "getConversationStart")
        conversationLiveData.value = conversationRepository.getConversation(chatId!!)
        //Log.d("chat", "$conversation")
    }

    suspend fun addMessage(message: Message) {
        messageRepository.addMessage(message)
    }

    suspend fun getUsers(): List<User> = userRepository.getUsers()
}


