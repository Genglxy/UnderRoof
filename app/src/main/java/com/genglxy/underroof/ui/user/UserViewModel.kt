package com.genglxy.underroof.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class UserViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text

    var masterUUID : UUID? = null
    var master : User? = null
}