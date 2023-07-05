package com.genglxy.underroof.logic.model

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Entity
@Parcelize
data class ConversationLite(
    @PrimaryKey val id: UUID,
    val name: String,
    val type: Int,
    val exposed: Boolean,
    val introduction: String,
    val members: String
) : Parcelable {
    fun toConversation(joined: Boolean): Conversation {
        this.apply {
            return Conversation(
                id, Uri.EMPTY, Uri.EMPTY, name, type, exposed, joined,introduction, members
            )
        }
    }
}