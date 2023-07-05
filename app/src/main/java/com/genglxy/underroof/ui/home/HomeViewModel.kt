package com.genglxy.underroof.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genglxy.underroof.logic.ConversationRepository
import com.genglxy.underroof.logic.model.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _list = MutableLiveData<List<Conversation>>()
    val list: LiveData<List<Conversation>> = _list

    private val conversationRepository = ConversationRepository.get()

    val conversationList = conversationRepository.getConversations()

    private val _conversations: MutableStateFlow<List<Conversation>> = MutableStateFlow(emptyList())
    val conversations: StateFlow<List<Conversation>>
        get() = _conversations.asStateFlow()

    init {
        viewModelScope.launch {
            conversationRepository.getConversations().collect {
                _conversations.value = it
            }
        }
    }

    suspend fun addConversation(conversation: Conversation) {
        conversationRepository.addConversation(conversation)
    }
}