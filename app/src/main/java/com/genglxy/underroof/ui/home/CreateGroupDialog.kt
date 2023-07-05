package com.genglxy.underroof.ui.home

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.genglxy.underroof.R
import com.genglxy.underroof.databinding.DialogCreateGroupBinding
import com.genglxy.underroof.databinding.FragmentHomeBinding
import com.genglxy.underroof.logic.ConversationRepository
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.Conversation
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
        _binding = DialogCreateGroupBinding.inflate(inflater, container, false)
        binding.groupNameEdittext.editText?.doOnTextChanged { text, start, before, count ->
            name = text.toString()
        }
        binding.groupIntroductionEdittext.editText?.doOnTextChanged { text, start, before, count ->
            introduction = text.toString()
        }
        binding.exposed.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked != exposed) exposed = isChecked
        }
        binding.createGroup.setOnClickListener {
            val conversation = Conversation(
                id = UUID.randomUUID(),
                photo = Uri.EMPTY,
                photoThumbnail = Uri.EMPTY,
                name = name,
                type = Conversation.TYPE_GROUP,
                exposed = exposed,
                joined = true,
                introduction = introduction,
                members = ""
            )
            val job = Job()
            val scope = CoroutineScope(job)
            scope.launch {
                conversationRepository.addConversation(conversation)
            }
            findNavController().popBackStack()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}