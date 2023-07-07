package com.genglxy.underroof.ui.chat

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.WindowCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.genglxy.underroof.UnderRoofApplication
import com.genglxy.underroof.databinding.FragmentChatBinding
import com.genglxy.underroof.logic.FileRepository
import com.genglxy.underroof.logic.MessageRepository
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.Message
import com.genglxy.underroof.service.SocketService
import com.genglxy.underroof.ui.home.HomeAdapter
import com.genglxy.underroof.ui.home.HomeFragmentDirections
import github.leavesczy.matisse.Matisse
import github.leavesczy.matisse.MatisseContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.Inet6Address
import java.net.InetAddress
import java.util.Date
import java.util.UUID

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }

    private val viewModel: ChatViewModel by lazy { ViewModelProvider(this)[ChatViewModel::class.java] }
    private val userRepository = UserRepository.get()
    private val messageRepository = MessageRepository.get()
    private val fileRepository = FileRepository.get()

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Matisse>

    lateinit var broadcastBinder: SocketService.BroadcastBinder

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            broadcastBinder = service as SocketService.BroadcastBinder
            viewModel.masterLiveData.observe(viewLifecycleOwner) {
                broadcastBinder.sendParcelable(
                    getBroadcastAddress(),
                    it,
                    SocketService.TYPE_USER_QUERY,
                    0,
                    false
                )
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val temp = listOf<Message>(
            Message(
                UUID.randomUUID(),
                Message.TYPE_MESSAGE,
                Message.SUBTYPE_TEXT,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "qwertyufdsshdjfkghyfdhshxdjfklhufdcjghjyfdjtfyguiyktdyrftugiuyfutdjtfyg,uyfmcfgvjh,kbhvjgmcfgmfdf",
                System.currentTimeMillis()
            ),
            Message(
                UUID.randomUUID(),
                Message.TYPE_MESSAGE,
                Message.SUBTYPE_TEXT,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "qwertyufdsdf",
                System.currentTimeMillis()
            ),
            Message(
                UUID.randomUUID(),
                Message.TYPE_MESSAGE,
                Message.SUBTYPE_TEXT,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "qwertyufdsdf",
                System.currentTimeMillis()
            ),
        )

        _binding = FragmentChatBinding.inflate(inflater, container, false)
        activity?.let { WindowCompat.setDecorFitsSystemWindows(it.window, false) }

        viewModel.chatId = ChatActivityArgs.fromBundle(requireActivity().intent.extras!!).id

        //Log.d("chatfragment", "$chatId")

        val layoutManager = LinearLayoutManager(context)
        binding.chatRecycler.layoutManager = layoutManager
        //binding.chatRecycler.adapter = ChatAdapter(requireParentFragment(), temp)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.setConversation()
                viewModel.messages.observe(viewLifecycleOwner) { messages ->
                    binding.chatRecycler.adapter = ChatAdapter(requireParentFragment(), messages)
                }
            }
        }
        binding.masterInput.doOnTextChanged { text, start, before, count ->
            viewModel.writeInputText(text.toString())
            Log.d("chat", "$text")
        }
        binding.sendMessage.setOnClickListener {
            val message = Message(
                UUID.randomUUID(),
                Message.TYPE_MESSAGE,
                Message.SUBTYPE_TEXT,
                viewModel.masterUUID,
                viewModel.chatId!!,
                viewModel.inputText,
                System.currentTimeMillis()
            )
            val job = Job()
            val scope = CoroutineScope(job)

            scope.launch {
                withContext(Dispatchers.IO) {
                    val users = userRepository.getUsers()
                    for (user in users) {
                        if (user.online) {
                            broadcastBinder.sendParcelable(
                                InetAddress.getByName(user.ip),
                                message,
                                SocketService.TYPE_MESSAGE,
                                15,
                                true
                            )
                        }
                    }
                    messageRepository.addMessage(message)
                }
            }
            binding.masterInput.setText("")
            Log.d("final6", "output input${viewModel.inputText}")
        }
        binding.sendPicture.setOnClickListener {
            openImagePicker()

            //TODO("实现打开图片，发送消息，等待请求逻辑")
        }
        imagePickerLauncher = registerForActivityResult(MatisseContract()) {
            if (it.isNotEmpty()) {
                val uri = it[0].uri
                val path = it[0].path
                val size = it[0].size
                val id = UUID.randomUUID()
                var bitmap: Bitmap? = Glide.with(this).asBitmap().load(uri).submit(500, 500).get()
                val bitmapJob = Job()
                val bitmapScope = CoroutineScope(bitmapJob)
                bitmapScope.launch {
                    while (true) {
                        if (bitmap == null) {
                            delay(100)
                        } else {
                            val uri = bitmap2Cache(id, bitmap)
                            val message = Message(
                                UUID.randomUUID(),
                                Message.TYPE_MESSAGE,
                                Message.SUBTYPE_IMAGE,
                                viewModel.masterUUID,
                                viewModel.chatId!!,
                                id.toString(),
                                System.currentTimeMillis()
                            )
                            val users = userRepository.getUsers()
                            for (user in users) {
                                if (user.online) {
                                    broadcastBinder.sendParcelable(
                                        InetAddress.getByName(user.ip),
                                        message,
                                        SocketService.TYPE_MESSAGE,
                                        15,
                                        true
                                    )
                                    broadcastBinder.sendFile(uri, user.ip)
                                }
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.conversationLiveData.observe(viewLifecycleOwner) {
            binding.chatTopAppBar.title = it.name
        }
        val intent = Intent(context, SocketService::class.java)
        activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
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

    private fun openImagePicker() {
        val matisse = Matisse(maxSelectable = 1)
        imagePickerLauncher.launch(matisse)
    }

    private fun bitmap2Cache(id: UUID, bitmap: Bitmap): Uri {
        val format = Bitmap.CompressFormat.PNG
        val extension = "png"
        val fileName = "$id.$extension"
        val pathName = "${requireActivity().externalCacheDir}${File.separator}"
        val path =
            File("$pathName$fileName")
        try {
            val os = FileOutputStream(path)
            bitmap.compress(format, 80, os)
            os.close()
            val job = Job()
            val scope = CoroutineScope(job)
            scope.launch {
                fileRepository.addFile(
                    com.genglxy.underroof.logic.model.File(
                        id,
                        Uri.fromFile(path),
                        com.genglxy.underroof.logic.model.File.TYPE_IMAGE
                    )
                )
            }
            return Uri.fromFile(path)
        } catch (_: Exception) {
        }
        return Uri.EMPTY
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
}