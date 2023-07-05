package com.genglxy.underroof.ui.chat

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.genglxy.underroof.R
import com.genglxy.underroof.databinding.ItemChatBinding
import com.genglxy.underroof.logic.model.Message
import java.util.UUID

class ChatAdapter(val chatFragment: Fragment, val list: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            val masterName = UUID.randomUUID() //TODO("待实现从数据库读取用户数据的过程")
            val from = when (message.from) {
                UUID.fromString("") -> 0
                masterName -> 1
                else -> 2
            }

            when (from) {
                0 -> {
                    binding.chatSystemLayout.visibility = View.VISIBLE
                    binding.chatThisLayout.visibility = View.GONE
                    binding.chatThatLayout.visibility = View.GONE
                    when (message.subType) {
                        1 -> {
                            val userUuid = UUID.fromString(message.content)
                            val userName =
                                if (true) "李山水" else "Unknown User" //TODO("待实现从数据库根据UUID获取名称“）
                            binding.chatSystemLayout.text =
                                chatFragment.getString(R.string.join_chat, userName)
                        }

                        2 -> {
                            val userUuid = UUID.fromString(message.content)
                            val userName =
                                if (true) "李山水" else "Unknown User" //TODO("待实现从数据库根据UUID获取名称“）
                            binding.chatSystemLayout.text =
                                chatFragment.getString(R.string.quit_chat, userName)
                        }

                        else -> {
                            binding.chatSystemLayout.text =
                                chatFragment.getString(R.string.unknown_system_msg)

                        }
                    }
                }

                1 -> {
                    binding.chatSystemLayout.visibility = View.GONE
                    binding.chatThisLayout.visibility = View.VISIBLE
                    binding.chatThatLayout.visibility = View.GONE
                    if (message.type == 1) {
                        binding.chatThisText.visibility = View.VISIBLE
                        binding.chatThisPicContainer.visibility = View.GONE
                        binding.chatThisText.text = message.content
                    } else {
                        binding.chatThisText.visibility = View.VISIBLE
                        binding.chatThisPicContainer.visibility = View.GONE
                        //binding.chatThisPic.  TODO("此处插入图片")
                    }
                }

                2 -> {
                    binding.chatSystemLayout.visibility = View.GONE
                    binding.chatThisLayout.visibility = View.GONE
                    binding.chatThatLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}