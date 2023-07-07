package com.genglxy.underroof.ui.home

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.genglxy.underroof.R
import com.genglxy.underroof.databinding.DialogCreateGroupBinding
import com.genglxy.underroof.logic.ConversationRepository
import com.genglxy.underroof.logic.model.Conversation
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID


class CreateGroupDialog : DialogFragment() {

    private var _binding: DialogCreateGroupBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }

    private val conversationRepository = ConversationRepository.get()

    var name = ""
    var introduction = ""
    var exposed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogStyle)
        _binding = DialogCreateGroupBinding.inflate(inflater, container, false)
        val temp = isSavable()
        val prefs = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE)
        val masterUUID = prefs.getString("masterUUID", "").toString()
        Log.d("CreateDialog", "$temp")
        binding.groupNameEdittext.editText?.setText(name)
        binding.groupIntroductionEdittext.editText?.setText(introduction)
        binding.groupNameEdittext.editText?.doOnTextChanged { text, start, before, count ->
            name = text.toString()
            isSavable()
        }
        binding.groupIntroductionEdittext.editText?.doOnTextChanged { text, start, before, count ->
            introduction = text.toString()
            isSavable()
        }
        binding.exposed.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked != exposed) exposed = isChecked
        }
        binding.createGroup.setOnClickListener {
            if (isSavable()) {
                val conversation = Conversation(
                    id = UUID.randomUUID(),
                    photo = Uri.EMPTY,
                    photoThumbnail = Uri.EMPTY,
                    name = name,
                    type = Conversation.TYPE_GROUP,
                    exposed = exposed,
                    joined = true,
                    introduction = introduction,
                    members = masterUUID
                )
                Log.d("CreateDialog", "$conversation")
                val job = Job()
                val scope = CoroutineScope(job)
                scope.launch {
                    conversationRepository.addConversation(conversation)
                }
                findNavController().popBackStack()
            } else {
                Snackbar.make(binding.root, "Too many or too few characters.", Snackbar.LENGTH_SHORT).show()
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

    private fun isSavable(): Boolean {
        val temp = (name.length <= 20) and (introduction.length <= 40) and name.isNotEmpty()
        //binding.createGroup.isClickable = temp
        return temp
    }
}