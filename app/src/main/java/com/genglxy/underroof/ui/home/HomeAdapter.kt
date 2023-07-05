package com.genglxy.underroof.ui.home

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genglxy.underroof.R
import com.genglxy.underroof.databinding.ItemHomeBinding
import com.genglxy.underroof.logic.model.Conversation

class HomeAdapter(private val homeFragment: Fragment, private val list: List<Conversation>) :
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(conversation: Conversation) {
            conversation.photoThumbnail.apply {
                if (this != Uri.EMPTY) {
                    Glide.with(homeFragment).load(this).into(binding.conversationPhoto)
                } else {
                    binding.conversationPhoto.setImageResource(R.drawable.icon_circle_primary)
                    val firstChar = conversation.name[0].toString()
                    binding.conversationPhotoText.text = conversation.name  //TODO("待实现对emoji的支持，https://developer.android.com/develop/ui/views/text-and-emoji/emoji-compat")
                    Log.d("HomeAdapter", "firstChar $firstChar")

                }
            }
            binding.conversationName.text = conversation.name

            val message: String = "" //TODO("待实现数据库，待实现从数据库获取第一条消息的功能")
            if (message.isNotEmpty()) {
                binding.conversationMessage.text = message
            } else {
                binding.conversationMessage.text = homeFragment.getString(R.string.no_message_yet)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = list[position]
        holder.bind(conversation)
    }

    override fun getItemCount() = list.size
}