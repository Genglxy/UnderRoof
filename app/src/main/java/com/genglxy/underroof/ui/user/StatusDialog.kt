package com.genglxy.underroof.ui.user

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.genglxy.underroof.R
import com.genglxy.underroof.databinding.DialogStatusBinding
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatusDialog: DialogFragment() {
    private val args: StatusDialogArgs by navArgs()

    private var _binding: DialogStatusBinding? = null

    var newEmoji = ""
    var newStatusText = ""

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }

    private val userRepository = UserRepository.get()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogStyle)
        _binding = DialogStatusBinding.inflate(inflater, container, false)
        val master = args.master
        binding.statusEmojiEdittext.editText?.setText(master.statusEmoji)
        binding.statusTextEdittext.editText?.setText(master.statusText)
        binding.statusEmojiEdittext.editText?.doOnTextChanged { text, start, before, count ->
            newEmoji = text.toString()
        }
        binding.statusTextEdittext.editText?.doOnTextChanged { text, start, before, count ->
            newStatusText = text.toString()
        }
        binding.submitStatus.setOnClickListener {
            master.apply {
                val newMaster = User(id, photo, photoThumbnail, name, gender, genderPrivate, age, agePrivate, newEmoji, newStatusText, System.currentTimeMillis(), introduction, online, ip)
                val job = Job()
                val scope = CoroutineScope(job)
                scope.launch {
                    userRepository.addUser(newMaster)
                    withContext(Dispatchers.Main) {
                        findNavController().popBackStack()
                    }
                }
            }
        }

        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun dismiss() {
        try {
            val fragmentActivity = activity
            //Log.i(TAG, "dialog dismiss, resume activity " + fragmentActivity!!.localClassName)
            // 触发背景 activity 重新 onresume
            val intent = Intent(activity, fragmentActivity!!.javaClass)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        } catch (e: NullPointerException) {
            //Log.i(TAG, "dialog dismiss, resume activity failed, null pointer exception occurs")
        }
        super.dismiss()
    }
}