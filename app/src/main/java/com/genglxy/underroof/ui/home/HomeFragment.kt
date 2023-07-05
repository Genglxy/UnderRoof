package com.genglxy.underroof.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.genglxy.underroof.R
import com.genglxy.underroof.databinding.FragmentHomeBinding
import com.genglxy.underroof.logic.model.Conversation
import kotlinx.coroutines.launch
import java.util.UUID

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }
    private val viewModel by lazy { ViewModelProvider(this)[HomeViewModel::class.java] }
    //private lateinit var adapter: HomeAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val temp = listOf<Conversation>(
            Conversation(UUID.randomUUID(), Uri.EMPTY, Uri.EMPTY, "啊测试1", 0, true, true, "", ""),
            Conversation(UUID.randomUUID(), Uri.EMPTY, Uri.EMPTY, "🤔测试2", 0, false, true, "", ""),
            Conversation(UUID.randomUUID(), Uri.EMPTY, Uri.EMPTY, "の测试2", 0, true, false, "", "")
        )

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        activity?.let { WindowCompat.setDecorFitsSystemWindows(it.window, false) }
        val layoutManager = LinearLayoutManager(context)
        binding.homeRecycler.layoutManager = layoutManager
        //adapter = HomeAdapter(this, temp)
        //binding.homeRecycler.adapter = adapter

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.home_fresh -> {
                        //TODO("刷新聊天列表，先刷新活跃用户列表获取可聊天列表")
                        true
                    }

                    R.id.home_create_group -> {
                        findNavController().navigate(HomeFragmentDirections.openCreateGroupDialog())
                        true
                    }

                    else -> {
                        false
                    }
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        //viewModel.list.observe(viewLifecycleOwner) {
        //    adapter.notifyDataSetChanged() //这里后面要更改为具体实现，在某几条更新后只更新对应条目以获得更好性能
        //}

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.conversations.collect { conversations ->
                    binding.homeRecycler.adapter =
                        HomeAdapter(requireParentFragment(), conversations)
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