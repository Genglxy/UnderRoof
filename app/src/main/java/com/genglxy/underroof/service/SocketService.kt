package com.genglxy.underroof.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import androidx.core.app.NotificationCompat
import com.genglxy.underroof.MainActivity
import com.genglxy.underroof.R
import com.genglxy.underroof.UnderRoofApplication
import com.genglxy.underroof.logic.ConversationRepository
import com.genglxy.underroof.logic.MessageRepository
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.Conversation
import com.genglxy.underroof.logic.model.ConversationLite
import com.genglxy.underroof.logic.model.Message
import com.genglxy.underroof.logic.model.Packet
import com.genglxy.underroof.logic.model.User
import com.genglxy.underroof.logic.model.UserLite
import com.genglxy.underroof.util.ParcelableUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.parcelableCreator
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet6Address
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID

class SocketService : Service() {
    companion object {
        const val TYPE_USER = 0
        const val TYPE_USER_QUERY = 1 //不需要等待回执
        const val TYPE_MESSAGE = 2
        const val TYPE_CONVERSATION = 3
        const val TYPE_CONVERSATION_QUERY = 4
    }

    private var running: Boolean = true
    private val userRepository = UserRepository.get()
    private val messageRepository = MessageRepository.get()
    private val conversationRepository = ConversationRepository.get()
    private var messageQueue = mutableListOf<UUID>()
    private lateinit var masterUUID: UUID
    private lateinit var localIp: String

    val tcpPort = 11697
    val udpPort = 8888
    var udpLock = true
    //lateinit var broadcastAddress: InetAddress

    private val mBinder = BroadcastBinder()

    inner class BroadcastBinder : Binder() {

        fun sendParcelable(
            inetAddress: InetAddress,
            parcelable: Parcelable,
            TYPE: Int,
            cycle: Int,
            needReply: Boolean
        ) {
            val replyId = UUID.randomUUID()
            if (needReply) {
                messageQueue.add(replyId)
            }
            Log.d("final", "${inetAddress.address}, $TYPE, $parcelable")
            val job = Job()
            val scope = CoroutineScope(job)
            //val inetAddress = InetAddress.getByName(address)
            val client = DatagramSocket()
            //Log.d("final", "service 1 send")
            scope.launch {
                //Log.d("final", "service 2 send")
                withContext(Dispatchers.IO) {
                    try {
                        //Log.d("final", "service 3 send")
                        val parcelBytes = ParcelableUtil.marshall(parcelable)
                        val packet = Packet(TYPE, parcelBytes)
                        val sendBytes = ParcelableUtil.marshall(packet)
                        //Log.d("final", "service 4 send")
                        val sendPacket =
                            DatagramPacket(sendBytes, sendBytes.size, inetAddress, udpPort)
                        try {
                            if (cycle != 0) {
                                for (i in 1..cycle) {
                                    //Log.d("final", "service 5 send")
                                    client.send(sendPacket)
                                    delay((10000 / cycle).toLong())
                                    if (replyId !in messageQueue && needReply) break
                                    if (i == cycle && needReply) {
                                        val user: User? = userRepository.getUser(
                                            inetAddress.address.toString().drop(1)
                                        )
                                        while (true) {
                                            delay(100)
                                            user?.apply {
                                                val newUser = User(
                                                    id,
                                                    photo,
                                                    photoThumbnail,
                                                    name,
                                                    gender,
                                                    genderPrivate,
                                                    age,
                                                    agePrivate,
                                                    statusEmoji,
                                                    statusText,
                                                    statusCreate,
                                                    introduction,
                                                    false,
                                                    ""
                                                )
                                                userRepository.addUser(newUser)
                                            }
                                        }
                                    }
                                }
                            } else {
                                client.send(sendPacket)
                                delay(3000)
                            }
                            //Log.d("final", "service 6 send")
                        } catch (e: Exception) {
                            //Log.d("final", "service 7 send")
                            e.printStackTrace()
                        }
                    } catch (e: Exception) {
                        //Log.d("final", "service 8 send")
                        e.printStackTrace()
                    } finally {
                        //Log.d("final", "service 9 send")
                        client.close()
                    }

                }
            }
        }

        fun sendFile(uri: Uri, address: String) {

            val job = Job()
            val scope = CoroutineScope(job)
            val client = Socket(address, tcpPort)
            scope.launch {
                try {
                    val out: OutputStream = client.getOutputStream()
                    val cr = applicationContext.contentResolver
                    val inputStream: InputStream? = cr.openInputStream(uri)
//            val fileInputStream = FileInputStream(File(url))
                    var len = 0
                    //窗口值大小
                    val buffer = ByteArray(10240)
                    var total: Long = 0

                    //发送数据
                    while (withContext(Dispatchers.IO) {
                            inputStream!!.read(buffer)
                        }.also { len = it } != -1) {
                        withContext(Dispatchers.IO) {
                            out.write(buffer, 0, len)
                        }
                        total += len
                        //updateProgress(total, length)
                    }
                    withContext(Dispatchers.IO) {
                        out.close()
                    }
                    withContext(Dispatchers.IO) {
                        if (inputStream != null) {
                            inputStream.close()
                        }
                    }
                    withContext(Dispatchers.IO) {
                        client.takeIf { it.isConnected }?.close()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                } finally {
                    //sendFinish()
                }
            }
        }

        fun receiveFile(id: UUID) {
            val serverSocket = ServerSocket(tcpPort)
            val connect = serverSocket.accept()
            val job = Job()
            val scope = CoroutineScope(job)
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        serverSocket.soTimeout = 3000
                        //连接socket

                        Log.e("TcpServerService", "已成功启动TcpServer")
                        //Log.e("TcpServerService", "路径为$uri")

                        val extension = "png"
                        val fileName = "$id.$extension"
                        val pathName = "${application.externalCacheDir}${File.separator}"
                        val path =
                            File("$pathName$fileName")
                        val uri = Uri.fromFile(path)
                        //打开文件
                        val f = File(uri.path!!)
                        val dirs = File(f.parent!!)
                        Log.e("TcpServerService", "正在接收的文件名为${f.name}")
                        if (!dirs.exists()) {
                            dirs.mkdirs()
                            Log.e("TcpServerService", "创建Download目录")
                        }
                        //创建文件
                        if (f.createNewFile()) Log.e("TcpServerService", "成功创建文件 $uri")
                        else Log.e("TcpServerService", "已存在")

                        //获取输入流
                        val inStream: InputStream = connect.getInputStream()
                        val fileOutputStream = FileOutputStream(f)
                        val buffer = ByteArray(10240)
                        var len: Int = 0
                        var total: Long = 0
                        while (inStream.read(buffer).also { len = it } != -1) {
                            fileOutputStream.write(buffer, 0, len)
                            total += len
                            //updateProgress(total, fileSize)
                        }
                        fileOutputStream.close()
                        inStream.close()
                        serverSocket.close()
                        //receiveFinish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    Log.e("TcpServerService", "已结束")
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()

        val prefs = UnderRoofApplication.context.getSharedPreferences("data", Context.MODE_PRIVATE)
        masterUUID = UUID.fromString(prefs.getString("masterUUID", ""))
        //启动前台服务
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "my_service", "前台Service通知", NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification =
            NotificationCompat.Builder(this, "my_service").setContentTitle("This is content title")
                .setContentText("This is content text")
                //.setSmallIcon(R.drawable.small_icon)
                //.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.large_icon))
                .setContentIntent(pi).build()
        startForeground(1, notification)

        val broadcastAddress = getBroadcastAddress()

        val udpService = DatagramSocket(udpPort)

        val udpJob = Job()
        val udpScope = CoroutineScope(udpJob)
        udpScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val receiveBytes = ByteArray(10240)
                    //创建接受信息的包对象
                    val receivePacket = DatagramPacket(receiveBytes, receiveBytes.size)

                    Log.e("ServerService", "成功启动server")
                    //开启一个死循环，不断接受数据
                    while (running) {
                        try {
                            Log.d("service", "start wait")
                            //接收数据，程序会阻塞到这一步，直到收到一个数据包为止
                            udpService.receive(receivePacket)

                            Log.d("service", "info received")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (receivePacket.address != null) {
                            Log.e(
                                "ServerService",
                                "成功接收信息, ${receivePacket.address}, ${receivePacket.data}, size ${receivePacket.data.size}"
                            )
                            //解析收到的数据
                            val receiveAddress = receivePacket.address.toString().drop(1)
                            Log.e("ServerService", "该数据包来自:$receiveAddress")
                            if (receiveAddress == localIp) continue
                            try {
                                val packet = ParcelableUtil.unmarshall(
                                    receivePacket.data, parcelableCreator<Packet>()
                                )
                                packet.apply {
                                    when (flag) {
                                        TYPE_USER_QUERY -> {
                                            val user = ParcelableUtil.unmarshall(
                                                packet.data, parcelableCreator<UserLite>()
                                            ).toUser(receiveAddress)
                                            Log.d("message", "TYPE_USER_QUERY receive user $user")
                                            //跳过自己发送的数据
                                            //if (receiveAddress == localIp)
                                            //    continue
                                            //receiveMsg = String(receivePacket.data, 0, receivePacket.length)
                                            //val user = ParcelableUtil.unmarshall(receivePacket.data, parcelableCreator<User>()) //creator.createFromParcel(parcel)
                                            //Log.d("service", "$user")

                                            receiveUser(user)
                                            userRepository.getUser(masterUUID)?.let {
                                                BroadcastBinder().sendParcelable(
                                                    InetAddress.getByName(
                                                        receiveAddress
                                                    ),
                                                    it.toUserLite(),
                                                    TYPE_USER,
                                                    15,
                                                    true
                                                )
                                            }/*
                                        while (messageQueue.contains(thisUUID)) {
                                            BroadcastBinder().sendParcelable(
                                                InetAddress.getByName(
                                                    receiveAddress
                                                ),
                                                userRepository.getUser(masterUUID).toUserLite(),
                                                TYPE_USER
                                            )
                                            delay(1000)
                                        }

                                         */
                                        }

                                        TYPE_USER -> {
                                            val user = ParcelableUtil.unmarshall(
                                                packet.data, parcelableCreator<UserLite>()
                                            ).toUser(receiveAddress)
                                            Log.d("message", "TYPE_USER receive user $user")
                                            val thisUUID = UUID.randomUUID()
                                            messageQueue.add(thisUUID)
                                            val message = Message(
                                                thisUUID,
                                                Message.TYPE_SYSTEM,
                                                Message.SUBTYPE_RECEIVED,
                                                masterUUID,
                                                UUID.fromString(
                                                    getString(
                                                        R.string.SYSTEM_MESSAGE_UUID
                                                    )
                                                ),
                                                thisUUID.toString(),
                                                System.currentTimeMillis()
                                            )
                                            BroadcastBinder().sendParcelable(
                                                InetAddress.getByName(
                                                    receiveAddress
                                                ), message, TYPE_MESSAGE, 15, true
                                            )
                                            //跳过自己发送的数据
                                            //if (receiveAddress == localIp)
                                            //    continue
                                            //receiveMsg = String(receivePacket.data, 0, receivePacket.length)
                                            //val user = ParcelableUtil.unmarshall(receivePacket.data, parcelableCreator<User>()) //creator.createFromParcel(parcel)
                                            //Log.d("service", "$user")

                                            receiveUser(user)/*
                                        while (messageQueue.contains(thisUUID)) {
                                            BroadcastBinder().sendParcelable(
                                                InetAddress.getByName(
                                                    receiveAddress
                                                ), message, TYPE_MESSAGE
                                            )
                                            delay(1000)
                                        }

                                         */
                                        }

                                        TYPE_MESSAGE -> {
                                            val message = ParcelableUtil.unmarshall(
                                                packet.data, parcelableCreator<Message>()
                                            )

                                            Log.d(
                                                "message", "TYPE_MESSAGE receive message $message"
                                            )
                                            when (message.type) {
                                                Message.TYPE_SYSTEM -> {
                                                    Log.d(
                                                        "message",
                                                        "TYPE_SYSTEM receive message $message"
                                                    )
                                                    when (message.subType) {
                                                        Message.SUBTYPE_RECEIVED -> {
                                                            Log.d(
                                                                "message",
                                                                "SUBTYPE_RECEIVED receive message $message"
                                                            )
                                                            messageQueue.remove(
                                                                UUID.fromString(
                                                                    message.content
                                                                )
                                                            )
                                                        }

                                                        Message.SUBTYPE_GROUP_QUERY -> {
                                                            Log.d(
                                                                "message",
                                                                "SUBTYPE_GROUP_QUERY receive message $message"
                                                            )
                                                            val groups =
                                                                conversationRepository.getExposedConversations()
                                                            for (group in groups) {
                                                                BroadcastBinder().sendParcelable(
                                                                    InetAddress.getByName(
                                                                        receiveAddress
                                                                    ),
                                                                    group.toConversationLite(),
                                                                    TYPE_CONVERSATION,
                                                                    15, true
                                                                )
                                                            }
                                                        }

                                                        Message.SUBTYPE_JOIN -> {
                                                            Log.d(
                                                                "message",
                                                                "SUBTYPE_JOIN receive message $message"
                                                            )
                                                            val conversation =
                                                                conversationRepository.getConversation(
                                                                    message.conversation
                                                                )
                                                            val newMembers =
                                                                "${conversation.members},${message.from}"
                                                            conversation.apply {
                                                                val newGroup = Conversation(
                                                                    id,
                                                                    photo,
                                                                    photoThumbnail,
                                                                    name,
                                                                    type,
                                                                    exposed,
                                                                    joined,
                                                                    introduction,
                                                                    newMembers
                                                                )
                                                                conversationRepository.addConversation(
                                                                    newGroup
                                                                )
                                                                Log.d("qazxsw", "message added")
                                                                messageRepository.addMessage(message)/*
                                                                val messages : MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())
                                                                    messageRepository.getMessages().collect {
                                                                    messages.value = it
                                                                        Log.d("qazxsw", "$it")
                                                                }

                                                                 */
                                                            }
                                                        }
                                                    }
                                                }

                                                Message.TYPE_MESSAGE -> {
                                                    Log.d(
                                                        "message",
                                                        "TYPE_MESSAGE receive message $message"
                                                    )
                                                    when (message.subType) {
                                                        Message.SUBTYPE_TEXT -> {
                                                            Log.d(
                                                                "message",
                                                                "SUBTYPE_TEXT receive message $message"
                                                            )
                                                            replyMessage(message.id, receiveAddress)
                                                            messageRepository.addMessage(message)
                                                            //TODO("文本消息类，直接写入数据库")
                                                        }
                                                    }
                                                }

                                                Message.TYPE_FILE -> {
                                                    Log.d(
                                                        "message",
                                                        "TYPE_MESSAGE receive message $message"
                                                    )
                                                    when (message.subType) {
                                                        Message.SUBTYPE_TEXT -> {

                                                            //TODO("文本消息类，直接写入数据库")
                                                        }

                                                        Message.SUBTYPE_IMAGE -> {
                                                            Log.d(
                                                                "message",
                                                                "SUBTYPE_IMAGE receive message $message"
                                                            )
                                                            val id =
                                                                UUID.fromString(message.content)
                                                            BroadcastBinder().receiveFile(id)
                                                            //TODO("图片类，写入数据库，调用TCP请求图片，请求完成显示")
                                                        }

                                                        Message.SUBTYPE_FILE -> {
                                                            //TODO("文件类，暂不实现")
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        TYPE_CONVERSATION -> {
                                            val job = Job()
                                            val scope = CoroutineScope(job)
                                            scope.launch {
                                                val conversation = ParcelableUtil.unmarshall(
                                                    packet.data,
                                                    parcelableCreator<ConversationLite>()
                                                )
                                                val oldConversation: Conversation? =
                                                    conversationRepository.getConversation(
                                                        conversation.id
                                                    )
                                                val joined = oldConversation?.joined ?: false
                                                conversationRepository.addConversation(
                                                    conversation.toConversation(
                                                        joined
                                                    )
                                                )
                                            }
                                            //TODO("已返回群聊，添加至数据库")
                                        }
                                    }
                                }
                            } catch (_: Exception) {
                            }

                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    //关闭DatagramSocket对象
                    udpService.takeIf { udpService.isConnected }?.close()
                }
            }
        }
    }

    private suspend fun replyMessage(
        uuid: UUID, receiveAddress: String
    ) {
        val replyMessage = Message(
            UUID.randomUUID(),
            Message.TYPE_SYSTEM,
            Message.SUBTYPE_RECEIVED,
            masterUUID,
            UUID.fromString(getString(R.string.SYSTEM_MESSAGE_UUID)),
            uuid.toString(),
            System.currentTimeMillis()
        )
        BroadcastBinder().sendParcelable(
            InetAddress.getByName(
                receiveAddress
            ), replyMessage, TYPE_MESSAGE, 15, false
        )
    }

    private fun receiveUser(user: User) {  //此函数用于向数据库写入数据
        val job = Job()
        val scope = CoroutineScope(job)
        scope.launch {
            userRepository.addUser(user)
        }

        /*
        Log.e("ServerService", "收到信息：$receiveMsg")
        val intent = Intent(RECEIVE_MSG)
        intent.putExtra("msg", receiveMsg)
        intent.putExtra(FROM_ADDRESS, address)
        mLocalBroadcastManager.sendBroadcast(intent)
         */
    }

    private fun getBroadcastAddress(): InetAddress {
        var broadcastAddress = InetAddress.getByName("127.0.0.255")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val dhcpInfo = wifiManager.dhcpInfo
            localIp = intToIp(dhcpInfo.ipAddress)
            broadcastAddress =
                InetAddress.getByName(getBroadcastIp(dhcpInfo.ipAddress, dhcpInfo.netmask))
        } else {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
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
                        Log.d("final", "${address.address}")
                        if (address.address is Inet6Address) continue
                        val parts = address.toString().split("/")
                        Log.d("final", "$parts")
                        localIp = parts[0]
                        val prefix = if (parts.lastIndex < 1) 0 else parts[1].toInt()
                        val netmask = ((0xffffffff).toInt() ushr (32 - prefix))
                        Log.d("final", "prefix = $prefix, netmask = $netmask")
                        broadcastAddress =
                            InetAddress.getByName(getBroadcastIp(ipToInt(localIp), netmask))
                        Log.d("final", "broadcastaddress $broadcastAddress")
                        break
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
}