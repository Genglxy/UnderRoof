package com.genglxy.underroof.ui.home

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.genglxy.underroof.R
import com.genglxy.underroof.UnderRoofApplication
import com.genglxy.underroof.databinding.FragmentHomeBinding
import com.genglxy.underroof.logic.ConversationRepository
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.Conversation
import com.genglxy.underroof.logic.model.Message
import com.genglxy.underroof.logic.model.User
import com.genglxy.underroof.service.SocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet6Address
import java.net.InetAddress
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
    private val userRepository = UserRepository.get()
    private val conversationRepository = ConversationRepository.get()

    lateinit var broadcastBinder: SocketService.BroadcastBinder

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            broadcastBinder = service as SocketService.BroadcastBinder
            viewModel.masterLiveData.observe(viewLifecycleOwner) {
                Log.d("final", "send broadcast called")

                broadcastBinder.sendParcelable(
                    getBroadcastAddress(),
                    it.toUserLite(),
                    SocketService.TYPE_USER_QUERY, 0, false
                )
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }

    }

    //private lateinit var adapter: HomeAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d("HF", "onCreateView")
        val temp = listOf<Conversation>(
            Conversation(UUID.randomUUID(), Uri.EMPTY, Uri.EMPTY, "啊测试1", 0, true, true, "", ""),
            Conversation(UUID.randomUUID(), Uri.EMPTY, Uri.EMPTY, "🤔测试2", 0, false, true, "", ""),
            Conversation(UUID.randomUUID(), Uri.EMPTY, Uri.EMPTY, "の测试2", 0, true, false, "", "")
        )

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        activity?.let { WindowCompat.setDecorFitsSystemWindows(it.window, false) }
        val layoutManager = LinearLayoutManager(context)
        binding.homeRecycler.layoutManager = layoutManager
        binding.createMasterBtn.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.openWizardDialog())
        }

        //adapter = HomeAdapter(this, temp)
        //binding.homeRecycler.adapter = adapter
        /*
        val job = Job()
        val scope = CoroutineScope(job)
        scope.launch {
            viewModel.masterUUID.collect {
                if (it.isEmpty()) {
                    findNavController().navigate(R.id.openWizardDialog)
                }
            }
        }

         */

        val job = Job()
        val scope = CoroutineScope(job)

        var flag = false

        val prefs = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE)
        viewModel.masterUUID = prefs.getString("masterUUID", "").toString()
        Log.d("homefragment", "${viewModel.masterUUID}")
        if (viewModel.masterUUID.isNullOrEmpty()) {
            binding.homePlaceholderLayout.visibility = View.VISIBLE
            binding.homeRecycler.visibility = View.GONE
        } else {
            binding.homePlaceholderLayout.visibility = View.GONE
            binding.homeRecycler.visibility = View.VISIBLE
        }

        /*
        scope.launch {
            preferencesRepository.storedMasterUUID.collect {
                Log.d("HomeFragment", "listuuid $it")
                while (!viewModel.onFront) {
                }
                withContext(Dispatchers.Main) {
                    if (viewModel.onFront) {
                        if (it.isEmpty()) {
                            binding.homePlaceholderLayout.visibility = View.VISIBLE
                            binding.homeRecycler.visibility = View.GONE
                        } else {
                            binding.homePlaceholderLayout.visibility = View.GONE
                            binding.homeRecycler.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

         */

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.home_fresh -> {
                        Log.d("homefragment", "1")
                        val message = Message(
                            UUID.randomUUID(),
                            Message.TYPE_SYSTEM,
                            Message.SUBTYPE_GROUP_QUERY,
                            UUID.fromString(viewModel.masterUUID),
                            UUID.fromString(getString(R.string.SYSTEM_MESSAGE_UUID)),
                            "",
                            System.currentTimeMillis()
                        )
                        val freshJob = Job()
                        val freshScope = CoroutineScope(freshJob)
                        freshScope.launch {
                            withContext(Dispatchers.IO) {
                                val users = userRepository.getActiveUsers()
                                for (user in users) {
                                    broadcastBinder.sendParcelable(
                                        InetAddress.getByName(user.ip),
                                        message,
                                        SocketService.TYPE_MESSAGE, 15, true
                                    )
                                }
                            }
                        }
                        // TODO("刷新聊天列表，先刷新活跃用户列表获取可聊天列表")
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
                        HomeAdapter(requireParentFragment(), conversations, {
                            findNavController().navigate(HomeFragmentDirections.openChatActivity(it))
                        }, {id ->
                            val message = Message(
                                UUID.randomUUID(),
                                Message.TYPE_SYSTEM,
                                Message.SUBTYPE_JOIN,
                                UUID.fromString(viewModel.masterUUID),
                                id,
                                "",
                                System.currentTimeMillis()
                            )
                            val freshJob = Job()
                            val freshScope = CoroutineScope(freshJob)
                            freshScope.launch {
                                withContext(Dispatchers.IO) {
                                    val users = userRepository.getActiveUsers()
                                    val conversation = conversationRepository.getConversation(id)
                                    val groupUsers = stringToUsers(conversation.members)
                                    conversation.apply {
                                        val newConversation = Conversation(id, photo, photoThumbnail, name, type, exposed, true, introduction, ("$members,${viewModel.masterUUID}"))
                                        conversationRepository.addConversation(newConversation)
                                    }
                                    for (user in users) {
                                        if (user.id in groupUsers) {
                                            broadcastBinder.sendParcelable(
                                                InetAddress.getByName(user.ip),
                                                message,
                                                SocketService.TYPE_MESSAGE, 15, false
                                            )
                                        }
                                    }
                                }
                            }
                        })
                }
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()
        Log.d("HF", "onresume")
        viewModel.onFront = true
        val prefs = requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE)
        viewModel.masterUUID = prefs.getString("masterUUID", "").toString()
        if (viewModel.masterUUID.isNullOrEmpty()) {
            binding.homePlaceholderLayout.visibility = View.VISIBLE
            binding.homeRecycler.visibility = View.GONE
        } else {
            binding.homePlaceholderLayout.visibility = View.GONE
            binding.homeRecycler.visibility = View.VISIBLE
        }
        if (viewModel.masterUUID != "") {
            val job = Job()
            val scope = CoroutineScope(job)
            scope.launch {
                viewModel.setMaster()
                withContext(Dispatchers.Main) {
                    val intent = Intent(context, SocketService::class.java)
                    activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("HF", "onpause")
        viewModel.onFront = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getBroadcastAddress(): InetAddress {
        var broadcastAddress = InetAddress.getByName("127.0.0.255")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val wifiManager =
                UnderRoofApplication.context.applicationContext.getSystemService(Service.WIFI_SERVICE) as WifiManager
            val dhcpInfo = wifiManager.dhcpInfo
            broadcastAddress =
                InetAddress.getByName(getBroadcastIp(dhcpInfo.ipAddress, dhcpInfo.netmask))
        } else {
            val connectivityManager =
                requireActivity().getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request =
                NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build()
            val networkCallback = object : ConnectivityManager.NetworkCallback(
                FLAG_INCLUDE_LOCATION_INFO
            ) {
                override fun onCapabilitiesChanged(
                    network: Network, networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val prop =
                        connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
                    val addresses = prop!!.linkAddresses

                    for (address in addresses) {
                        if (address.address is Inet6Address) continue
                        val parts = address.toString().split("/")
                        val ip = parts[0]
                        val prefix = if (parts.lastIndex < 1) 0 else parts[1].toInt()
                        val netmask = ((0xffffffff).toInt() ushr (32 - prefix))
                        broadcastAddress =
                            InetAddress.getByName(getBroadcastIp(ipToInt(ip), netmask))
                    }
                }
            }
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
        return broadcastAddress
    }

    private fun getBroadcastIp(ip: Int, netMask: Int): String {
        var net: Int = ip and netMask
        net = net or netMask.inv()
        return intToIp(net)
    }

    private fun intToIp(paramInt: Int): String {
        return ((paramInt and 0xFF).toString() + "." + (0xFF and (paramInt shr 8)) + "." + (0xFF and (paramInt shr 16)) + "." + (0xFF and (paramInt shr 24)))
    }

    private fun ipToInt(ip: String): Int {
        val ipSplit = ip.split(".")
        return (ipSplit[0].toInt() or (ipSplit[1].toInt() shl 8) or (ipSplit[2].toInt() shl 16) or (ipSplit[3].toInt() shl 24))
    }

    fun stringToUsers(member: String): List<UUID> {
        val memberSplit = member.split(",")
        val users = mutableListOf<UUID>()
        for (userId in memberSplit) {
            Log.d("final2", "userId")
            users.add(UUID.fromString(userId))
        }
        return users
    }

    fun usersToString(users: List<UUID>): String {
        val builder = StringBuilder()
        users.forEachIndexed { index, uuid ->
            if (index != 0) {
                builder.append(",")
            }
            builder.append(uuid)
        }
        return builder.toString()
    }
}