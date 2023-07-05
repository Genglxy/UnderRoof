package com.genglxy.underroof.logic.model

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Entity
@Parcelize
data class Conversation(
    @PrimaryKey val id: UUID,
    val photo: Uri,
    val photoThumbnail: Uri,
    val name: String,
    val type: Int,
    val exposed: Boolean,
    val joined: Boolean,
    val introduction: String,
    val members: String
) : Parcelable {
    companion object {
        const val TYPE_GROUP = 0
        const val TYPE_PERSON = 1
    }
}