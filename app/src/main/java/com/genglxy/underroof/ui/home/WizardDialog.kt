package com.genglxy.underroof.ui.home

import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import com.genglxy.underroof.databinding.DialogWizardBinding
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.User
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


class WizardDialog : DialogFragment() {

    private var _binding: DialogWizardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }

    private val userRepository = UserRepository.get()

    var uuid: UUID? = null
    var name = ""
    var introduction = ""
    var age = ""
    var agePrivate = false
    var gender: Int? = null
    var genderPrivate = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogStyle)
        _binding = DialogWizardBinding.inflate(inflater, container, false)

        val getMasterJob = Job()
        val getMasterScope = CoroutineScope(getMasterJob)
        getMasterScope.launch {
            /*
            preferencesRepository.storedMasterUUID.collect {
                Log.d("Wiz", "collect called")
                if (it.isNotEmpty() and (uuid == null)) {

                    Log.d("Wiz", "collect2 called")
                    uuid = UUID.fromString(it)
                    val user = userRepository.getUser(uuid!!)
                    name = user.name
                    introduction = user.introduction
                    age = user.age.toString()
                    agePrivate = user.agePrivate
                    gender = user.gender
                    genderPrivate = user.genderPrivate

                    Log.d("Wiz", "collect3 called")
                }
                Log.d("Wiz", "collect6 called")

                withContext(Dispatchers.Main){
                    binding.masterNameEdittext.editText?.setText(name)
                    binding.masterIntroductionEdittext.editText?.setText(introduction)
                    binding.masterAgeEdittext.editText?.setText(age)
                    binding.agePrivateCheckbox.isChecked = agePrivate
                    when (gender) {
                        User.GENDER_MALE -> binding.genderGroup.check(R.id.male)
                        User.GENDER_FEMALE -> binding.genderGroup.check(R.id.female)
                        else -> {}
                    }
                    binding.genderPrivateCheckbox.isChecked = genderPrivate
                }
            }

             */
        }

        val temp = isSavable()
        Log.d("CreateDialog", "$temp")
        binding.masterNameEdittext.editText?.setText(name)
        binding.masterIntroductionEdittext.editText?.setText(introduction)
        binding.masterNameEdittext.editText?.doOnTextChanged { text, start, before, count ->
            name = text.toString()
            isSavable()
        }
        binding.masterIntroductionEdittext.editText?.doOnTextChanged { text, start, before, count ->
            introduction = text.toString()
            isSavable()
        }
        binding.masterAgeEdittext.editText?.doOnTextChanged { text, start, before, count ->
            age = text.toString()
            isSavable()
        }
        binding.agePrivateCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            agePrivate = isChecked
        }
        binding.genderGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.male -> gender = User.GENDER_MALE
                R.id.female -> gender = User.GENDER_FEMALE
            }
        }
        binding.genderPrivateCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            genderPrivate = isChecked
        }

        binding.createGroup.setOnClickListener {
            if (isSavable()) {
                val user = User(
                    id = UUID.randomUUID(),
                    photo = Uri.EMPTY,
                    photoThumbnail = Uri.EMPTY,
                    name = name,
                    gender = gender!!,
                    genderPrivate = genderPrivate,
                    age = age.toInt(),
                    agePrivate = agePrivate,
                    statusEmoji = "",
                    statusText = "",
                    statusCreate = (0).toLong(),
                    introduction = introduction,
                    online = true,
                    ""
                )
                Log.d("CreateDialog", "$user")
                val job = Job()
                val scope = CoroutineScope(job)
                val editor = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE).edit()
                editor.putString("masterUUID", user.id.toString())
                editor.apply()

                val prefs = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE)
                val masterUUID = UUID.fromString(prefs.getString("masterUUID", ""))
                Log.d("wizard", "$masterUUID")
                scope.launch {
                    userRepository.addUser(user)

                    withContext(Dispatchers.Main) {
                        Log.d("Wiz", "collect7 called")
                        findNavController().popBackStack()
                    }
                }
                Log.d("Wiz", "collect7 called")
            } else {
                Snackbar.make(
                    binding.root,
                    "Too many or too few characters.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
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


    private fun isSavable(): Boolean {
        try {
            age.toInt()
        } catch (e: Exception) {
            return false
        }
        val temp =
            (name.length <= 20) and (introduction.length <= 40) and name.isNotEmpty() and (gender != null) and age.isNotEmpty()
        //binding.createGroup.isClickable = temp
        return temp
    }
}