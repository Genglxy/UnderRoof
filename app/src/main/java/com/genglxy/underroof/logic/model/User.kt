package com.genglxy.underroof.logic.model

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Entity
@Parcelize
data class User(
    @PrimaryKey val id: UUID,
    val photo: Uri,
    val photoThumbnail: Uri,
    val name: String,
    val gender: Int,
    val age: Int,
    val statusEmoji: String,
    val statusText: String,
    val statusCreate: Long,
    val introduction: String,
    val online: Boolean
) : Parcelable {
    fun toUserLite(): UserLite {
        this.apply {
            return UserLite(
                id,
                name,
                gender,
                age,
                statusEmoji,
                statusText,
                statusCreate,
                introduction,
                online
            )
        }
    }
}
