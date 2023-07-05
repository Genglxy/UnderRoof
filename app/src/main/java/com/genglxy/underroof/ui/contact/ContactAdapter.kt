package com.genglxy.underroof.ui.contact

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genglxy.underroof.R
import com.genglxy.underroof.databinding.ItemContactBinding
import com.genglxy.underroof.logic.model.User

class ContactAdapter(val contactFragment: Fragment, private val list: List<User>): RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemContactBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: User) {
            contact.photoThumbnail.apply {
                if (this != Uri.EMPTY) {
                    Glide.with(contactFragment).load(this).into(binding.contactPhoto)
                } else {
                    binding.contactPhoto.setImageResource(R.drawable.icon_circle_primary)
                    val firstChar = contact.name[0].toString()
                    binding.contactPhotoText.text = contact.name
                }
            }
            binding.contactName.text = contact.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = list[position]
        holder.bind(contact)
    }

    override fun getItemCount() = list.size
}