package com.genglxy.underroof.ui.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genglxy.underroof.R
import com.genglxy.underroof.databinding.ItemChatBinding
import com.genglxy.underroof.logic.UserRepository
import com.genglxy.underroof.logic.model.Message
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatAdapter(val chatFragment: Fragment, val list: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    companion object {
        const val FROM_SYSTEM = 0
        const val FROM_MASTER = 1
        const val FROM_FRIEND = 2
    }

    private val userRepository = UserRepository.get()
    var masterUUID: UUID? = null

    inner class ViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, user: User, master: User) {
            Log.d("final3", "$message, $user, $master")
            val masterId = master.id //TODO("待实现从数据库读取用户数据的过程")
            val from = when (message.from) {
                UUID.fromString(chatFragment.getString(R.string.SYSTEM_MESSAGE_UUID)) -> FROM_SYSTEM  //TODO 替换成固定的UUID
                masterId -> FROM_MASTER
                else -> FROM_FRIEND
            }
            showMessageLayout(from, message.subType)
            when (from) {
                FROM_SYSTEM -> {
                    when (message.subType) {
                        Message.SUBTYPE_JOIN -> {
                            //val userUuid = UUID.fromString(message.content)
                            val userName = user.name
                            //if (true) "李山水" else "Unknown User" //TODO("待实现从数据库根据UUID获取名称“）
                            binding.chatSystemLayout.text =
                                chatFragment.getString(R.string.join_chat, userName)
                        }

                        Message.SUBTYPE_QUIT -> {
                            //val userUuid = UUID.fromString(message.content)
                            val userName = user.name
                            //if (true) "李山水" else "Unknown User" //TODO("待实现从数据库根据UUID获取名称“）
                            binding.chatSystemLayout.text =
                                chatFragment.getString(R.string.quit_chat, userName)
                        }

                        Message.SUBTYPE_RECEIVED -> {}

                        Message.SUBTYPE_ONLINE -> {}

                        Message.SUBTYPE_OFFLINE -> {}

                        else -> {
                            binding.chatSystemLayout.text =
                                chatFragment.getString(R.string.unknown_system_msg)
                        }
                    }
                }

                FROM_MASTER -> {
                    master!!.apply {
                        Log.d("final5", "$this")
                        if (photoThumbnail != Uri.EMPTY) {
                            Glide.with(chatFragment).load(photoThumbnail)
                                .into(binding.chatThisPhoto)
                        } else {
                            binding.chatThisPhoto.setImageResource(R.drawable.icon_circle_primary)
                            //val firstChar = conversation.name[0].toString()
                            binding.chatThisPhotoText.text =
                                name  //TODO("待实现对emoji的支持，https://developer.android.com/develop/ui/views/text-and-emoji/emoji-compat")
                            //Log.d("HomeAdapter", "firstChar $firstChar")

                        }
                    }

                    when (message.type) {
                        Message.TYPE_MESSAGE -> {
                            binding.chatThisText.text = message.content
                        }

                        Message.TYPE_FILE -> {
                            when (message.subType) {
                                Message.SUBTYPE_TEXT -> {
                                    //TODO("读取文字流，放入textview")
                                }

                                Message.SUBTYPE_IMAGE -> {
                                    //binding.chatThisPic.  TODO("此处插入图片")
                                    //TODO("读取图片放入")
                                }

                                Message.SUBTYPE_FILE -> {
                                    //TODO("读取文件名与信息暂不实现")
                                }
                            }
                        }
                    }
                }

                FROM_FRIEND -> {
                    user.apply {
                        Log.d("final5", "${photoThumbnail != Uri.EMPTY}, $this")
                        if (photoThumbnail != Uri.EMPTY) {
                            Glide.with(chatFragment).load(photoThumbnail)
                                .into(binding.chatThisPhoto)
                        } else {
                            binding.chatThatPhoto.setImageResource(R.drawable.icon_circle_tertiary)
                            //val firstChar = conversation.name[0].toString()
                            Log.d("final5", "$name")
                            binding.chatThatPhotoText.text =
                                name  //TODO("待实现对emoji的支持，https://developer.android.com/develop/ui/views/text-and-emoji/emoji-compat")
                            //Log.d("HomeAdapter", "firstChar $firstChar")

                        }
                    }
                    when (message.type) {
                        Message.TYPE_MESSAGE -> {
                            Log.d("final5", "$this")
                            binding.chatThatText.text = message.content
                        }

                        Message.TYPE_FILE -> {
                            when (message.subType) {
                                Message.SUBTYPE_TEXT -> {
                                    //TODO("读取文字流，放入textview")
                                }

                                Message.SUBTYPE_IMAGE -> {
                                    //binding.chatThisPic.  TODO("此处插入图片")
                                    //TODO("读取图片放入")
                                }

                                Message.SUBTYPE_FILE -> {
                                    //TODO("读取文件名与信息暂不实现")
                                }
                            }
                        }
                    }
                }
            }
            /*
            val job = Job()
            val scope = CoroutineScope(job)
            scope.launch {

                val user = userRepository.getUser(message.from)

                val prefs = chatFragment.requireActivity()
                    .getSharedPreferences("data", Context.MODE_PRIVATE)

                masterUUID = UUID.fromString(prefs.getString("masterUUID", ""))
                var master: User?
                withContext(Dispatchers.IO) { master = userRepository.getUser(masterUUID!!) }

                withContext(Dispatchers.Main) {

                }
            }

             */
        }

        init {

        }

        private fun showMessageLayout(from: Int, type: Int) {
            when (from) {
                FROM_SYSTEM -> {
                    binding.chatThisLayout.visibility = View.GONE
                    binding.chatThatLayout.visibility = View.GONE
                    when (type) {
                        Message.SUBTYPE_JOIN, Message.SUBTYPE_QUIT -> {
                            binding.chatSystemLayout.visibility = View.VISIBLE
                        }

                        else -> {
                            binding.chatSystemLayout.visibility = View.GONE
                        }
                    }
                }

                FROM_MASTER -> {
                    binding.chatSystemLayout.visibility = View.GONE
                    binding.chatThisLayout.visibility = View.VISIBLE
                    binding.chatThatLayout.visibility = View.GONE
                    when (type) {
                        Message.SUBTYPE_TEXT -> {
                            binding.chatThisText.visibility = View.VISIBLE
                            binding.chatThisPicContainer.visibility = View.GONE
                        }

                        Message.SUBTYPE_IMAGE -> {
                            binding.chatThisText.visibility = View.GONE
                            binding.chatThisPicContainer.visibility = View.VISIBLE
                        }

                        Message.SUBTYPE_FILE -> {
                            //TODO("暂不实现")
                        }
                    }
                }

                FROM_FRIEND -> {
                    binding.chatSystemLayout.visibility = View.GONE
                    binding.chatThisLayout.visibility = View.GONE
                    binding.chatThatLayout.visibility = View.VISIBLE
                    when (type) {
                        Message.SUBTYPE_TEXT -> {
                            binding.chatThatText.visibility = View.VISIBLE
                            binding.chatThatPicContainer.visibility = View.GONE
                        }

                        Message.SUBTYPE_IMAGE -> {
                            binding.chatThatText.visibility = View.GONE
                            binding.chatThatPicContainer.visibility = View.VISIBLE
                        }

                        Message.SUBTYPE_FILE -> {
                            //TODO("暂不实现")
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = list[position]
        val prefs =
            chatFragment.requireActivity().getSharedPreferences("data", Context.MODE_PRIVATE)
        masterUUID = UUID.fromString(prefs.getString("masterUUID", ""))
        var user: User? = User(
            UUID.randomUUID(),
            Uri.EMPTY,
            Uri.EMPTY,
            "啊测试1",
            0,
            false,
            15,
            false,
            "😀",
            "开心",
            0,
            "",
            true,
            ""
        )
        var master:User? = User(
            masterUUID!!,
            Uri.EMPTY,
            Uri.EMPTY,
            "啊测试1",
            0,
            false,
            15,
            false,
            "😀",
            "开心",
            0,
            "",
            true,
            ""
        )
        val job = Job()
        val scope = CoroutineScope(job)
        Log.d("chatAdapterInfo", "1")
        scope.launch {
            Log.d("chatAdapterInfo", "2")
            Log.d("chatAdapterInfo", "3, ${message.from}")

            user = userRepository.getUser(message.from)
            if (user == null)
                user = User(
                    masterUUID!!,
                    Uri.EMPTY,
                    Uri.EMPTY,
                    "? Unknown",
                    0,
                    false,
                    15,
                    false,
                    "😀",
                    "开心",
                    0,
                    "Unknown",
                    true,
                    ""
                )


        Log.d("chatAdapterInfo", "4")
        Log.d("chatAdapterInfo", "5")
        master = userRepository.getUser(masterUUID!!)
        if (master == null)
            master = User(
                masterUUID!!,
                Uri.EMPTY,
                Uri.EMPTY,
                "Unknown",
                0,
                false,
                15,
                false,
                "😀",
                "开心",
                0,
                "Unknown",
                true,
                ""
            )
        Log.d("chatAdapterInfo", "6")
        withContext(Dispatchers.Main) {
            Log.d("chatAdapterInfo", "7")
            holder.bind(message, user!!, master!!)
            Log.d("chatAdapterInfo", "8")
        }
    }
}

override fun getItemCount() = list.size


}