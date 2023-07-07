package com.genglxy.underroof.ui.contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private val userRepository = UserRepository.get()

    private val _users: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
    val users: StateFlow<List<User>>
        get() = _users.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getUsersFlow().collect {
                _users.value = it
            }
        }
    }

    suspend fun addUser(user: User) {
        userRepository.addUser(user)
    }
}