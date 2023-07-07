package com.genglxy.underroof.ui.user

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.genglxy.underroof.R
import com.genglxy.underroof.databinding.FragmentUserBinding
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.User
import com.genglxy.underroof.ui.home.HomeFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }
    private val viewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private val userRepository = UserRepository.get()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserBinding.inflate(inflater, container, false)
        activity?.let { WindowCompat.setDecorFitsSystemWindows(it.window, false) }
        val prefs = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE)
        viewModel.masterUUID = UUID.fromString(prefs.getString("masterUUID", ""))
        val job = Job()
        CoroutineScope(job).launch {
            viewModel.master = userRepository.getUser(viewModel.masterUUID!!)
            withContext(Dispatchers.Main) {
                viewModel.master!!.apply {
                    if (photoThumbnail != Uri.EMPTY) {
                        Glide.with(requireParentFragment()).load(photoThumbnail)
                            .into(binding.userPhoto)
                    } else {
                        binding.userPhoto.setImageResource(R.drawable.icon_circle_primary)
                        //val firstChar = conversation.name[0].toString()
                        binding.userPhotoText.text =
                            name  //TODO("待实现对emoji的支持，https://developer.android.com/develop/ui/views/text-and-emoji/emoji-compat")
                        //Log.d("HomeAdapter", "firstChar $firstChar")

                    }
                    binding.userName.text = name
                    binding.userAge.text = "$age 岁"
                    binding.userGender.text =
                        when (gender) {
                            User.GENDER_FEMALE -> {
                                "♀"
                            }

                            User.GENDER_MALE -> {
                                "♂"
                            }

                            else -> {
                                "?"
                            }
                        }
                    binding.userStatus.text = "$statusEmoji $statusText"
                    binding.userIntroduction.text = introduction
                    if (statusEmoji == "" && statusText == "") {
                        binding.userStatus.text = getString(R.string.no_status)
                    } else {
                        binding.userStatus.text = "$statusEmoji $statusText"
                    }
                    binding.userStatus.setOnClickListener {
                        findNavController().navigate(UserFragmentDirections.changeStatus(viewModel.master!!))
                    }
                    /*
                    if ((System.currentTimeMillis() - statusCreate) > 86400000) {
                        binding.userStatus.background =
                    }

                     */
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val job = Job()
        val scope = CoroutineScope(job)
        scope.launch {
            viewModel.master = userRepository.getUser(viewModel.masterUUID!!)
            withContext(Dispatchers.Main) {
                if (viewModel.master!!.statusEmoji == "" && viewModel.master!!.statusText == "") {
                    binding.userStatus.text = getString(R.string.no_status)
                } else {
                    binding.userStatus.text = "${viewModel.master!!.statusEmoji} ${viewModel.master!!.statusText}"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}