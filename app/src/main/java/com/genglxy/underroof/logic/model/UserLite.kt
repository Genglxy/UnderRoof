package com.genglxy.underroof.logic.model

import android.net.Uri
import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class UserLite(
    @PrimaryKey val id: UUID,
    val name: String,
    val gender: Int,
    val age: Int,
    val statusEmoji: String,
    val statusText: String,
    val statusCreate: Long,
    val introduction: String,
    val online: Boolean
) : Parcelable {
    fun toUser(): User {
        this.apply {
            return User(
                id,
                Uri.EMPTY,
                Uri.EMPTY,
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