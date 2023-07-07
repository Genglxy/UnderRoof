package com.genglxy.underroof.ui.contact

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.genglxy.underroof.databinding.FragmentContactBinding
import com.genglxy.underroof.logic.model.User
import com.genglxy.underroof.ui.home.HomeAdapter
import kotlinx.coroutines.launch
import java.sql.Time
import java.util.UUID

class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }
    private val viewModel by lazy { ViewModelProvider(this)[ContactViewModel::class.java] }
    private lateinit var adapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        /*
        val temp = listOf<User>(
            User(
                UUID.randomUUID(),
                Uri.EMPTY,
                Uri.EMPTY,
                "李山水",
                0,
                23,"😀", "开心",
                System.currentTimeMillis(),
                "好无聊",
                true
            ),
            User(
                UUID.randomUUID(),
                Uri.EMPTY,
                Uri.EMPTY,
                "贾富贵",
                1,
                25,
                "🤔", "吃什么呢",
                System.currentTimeMillis(),
                "明天得出一次门",
                true
            ),
            User(
                UUID.randomUUID(),
                Uri.EMPTY,
                Uri.EMPTY,
                "猫",
                2,
                5,
                "🐱", "瞄",
                System.currentTimeMillis(),
                "喵喵喵喵",
                true
            ),
            User(
                UUID.randomUUID(),
                Uri.EMPTY,
                Uri.EMPTY,
                "😀🤖",
                3,
                109,"🫤", "-……-",
                System.currentTimeMillis(),
                "哔",
                true
            )
        )

         */

        _binding = FragmentContactBinding.inflate(inflater, container, false)
        activity?.let { WindowCompat.setDecorFitsSystemWindows(it.window, false) }
        val layoutManager = LinearLayoutManager(context)
        binding.contactRecycler.layoutManager = layoutManager
        //adapter = ContactAdapter(this, temp)
        //binding.contactRecycler.adapter = adapter

        viewModel.text.observe(viewLifecycleOwner) {

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collect { users ->
                    binding.contactRecycler.adapter =
                        ContactAdapter(requireParentFragment(), users)
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}