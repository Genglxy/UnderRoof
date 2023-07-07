package com.genglxy.underroof.ui.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import com.genglxy.underroof.R
import com.genglxy.underroof.databinding.ActivityChatBinding
import com.genglxy.underroof.databinding.DialogWizardBinding
import com.genglxy.underroof.util.InsetsWithKeyboardCallback
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityChatBinding
    //lateinit var id : UUID
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_chat)
        val binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val id = ChatActivityArgs.fromBundle(intent.extras!!).id
        //Log.d("ChatActivity", "id = $id")
        val insetsWithKeyboardCallback = InsetsWithKeyboardCallback(window)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, insetsWithKeyboardCallback)
    }
}