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
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import androidx.core.app.NotificationCompat
import com.genglxy.underroof.MainActivity
import com.genglxy.underroof.logic.PreferencesRepository
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.Conversation
import com.genglxy.underroof.logic.model.Message
import com.genglxy.underroof.logic.model.Packet
import com.genglxy.underroof.logic.model.User
import com.genglxy.underroof.logic.model.UserLite
import com.genglxy.underroof.util.ParcelableUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.parcelableCreator
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet6Address
import java.net.InetAddress
import java.util.UUID

class SocketService : Service() {
    companion object {
        const val TYPE_USER = 0
        const val TYPE_USER_QUERY = 1
        const val TYPE_MESSAGE = 2
        const val TYPE_CONVERSATION = 3
    }

    private var running: Boolean = true
    private val userRepository = UserRepository.get()
    private val preferencesRepository = PreferencesRepository.get()
    private var messageQueue = mutableListOf<UUID>()

    val tcpPort = 11697
    val udpPort = 11791
    var udpLock = false
    //lateinit var broadcastAddress: InetAddress

    private val mBinder = BroadcastBinder()

    inner class BroadcastBinder : Binder() {

        fun sendParcelable(address: String, parcelable: Parcelable, TYPE: Int) {
            val job = Job()
            val scope = CoroutineScope(job)
            val inetAddress = InetAddress.getByName(address)
            val client = DatagramSocket()

            scope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val parcelBytes = ParcelableUtil.marshall(parcelable)
                        val packet = Packet(TYPE, parcelBytes)
                        val sendBytes = ParcelableUtil.marshall(packet)
                        val sendPacket =
                            DatagramPacket(sendBytes, sendBytes.size, inetAddress, udpPort)
                        try {
                            client.send(sendPacket)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        client.close()
                    }

                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
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
                    val receiveBytes = ByteArray(2048)
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
                            val packet = ParcelableUtil.unmarshall(
                                receivePacket.data, parcelableCreator<Packet>()
                            )
                            packet.apply {
                                when (flag) {
                                    TYPE_USER_QUERY -> {
                                        val user = ParcelableUtil.unmarshall(
                                            packet.data, parcelableCreator<UserLite>()
                                        ).toUser()
                                        Log.d("service", "receive user $user")
                                        //跳过自己发送的数据
                                        //if (receiveAddress == localIp)
                                        //    continue
                                        //receiveMsg = String(receivePacket.data, 0, receivePacket.length)
                                        //val user = ParcelableUtil.unmarshall(receivePacket.data, parcelableCreator<User>()) //creator.createFromParcel(parcel)
                                        //Log.d("service", "$user")

                                        receiveUser(user)
                                        preferencesRepository.storedQuery.collect {
                                            BroadcastBinder().sendParcelable(receiveAddress, userRepository.getUser(UUID.fromString(it)), TYPE_USER)
                                        }
                                    }

                                    TYPE_MESSAGE -> {
                                        val message = ParcelableUtil.unmarshall(
                                            packet.data, parcelableCreator<Message>()
                                        )
                                        when (message.type) {
                                            Message.TYPE_SYSTEM -> {
                                                when (message.subType) {
                                                    Message.SUBTYPE_MESSAGE_GET -> {
                                                        //TODO("实现从消息队列去除UUID的功能")
                                                    }
                                                }
                                            }
                                            Message.TYPE_MESSAGE -> {
                                                when (message.subType) {
                                                    Message.SUBTYPE_TEXT -> {
                                                        //TODO("文本消息类，直接写入数据库")
                                                    }
                                                    Message.SUBTYPE_IMAGE -> {
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
                                        val conversation = ParcelableUtil.unmarshall(
                                            packet.data, parcelableCreator<Conversation>()
                                        )
                                        //TODO("已返回群聊，添加至数据库")
                                    }
                                }
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
}