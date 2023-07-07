package com.genglxy.underroof.ui.chat

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.genglxy.underroof.R
import com.genglxy.underroof.databinding.DialogCreateGroupBinding
import com.genglxy.underroof.databinding.DialogImageBinding
import com.genglxy.underroof.logic.FileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageDialog: DialogFragment() {

    private val args: ImageDialogArgs by navArgs()

    private var _binding: DialogImageBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }

    private val fileRepository = FileRepository.get()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogStyle)
        _binding = DialogImageBinding.inflate(inflater, container, false)
        val id = args.id
        val job = Job()
        val scope = CoroutineScope(job)
        scope.launch {
            val file = fileRepository.getFile(id)
            withContext(Dispatchers.Main) {
                Glide.with(requireParentFragment()).load(file.uri)
                    .into(binding.fullscreenImage)
            }
        }
        binding.fullscreenImage.setOnClickListener {
            findNavController().popBackStack()
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
}