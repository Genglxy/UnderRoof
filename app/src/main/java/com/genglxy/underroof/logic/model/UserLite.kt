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
    val genderPrivate: Boolean,
    val age: Int,
    val agePrivate: Boolean,
    val statusEmoji: String,
    val statusText: String,
    val statusCreate: Long,
    val introduction: String
) : Parcelable {
    fun toUser(ip: String): User {
        this.apply {
            return User(
                id,
                Uri.EMPTY,
                Uri.EMPTY,
                name,
                gender,
                genderPrivate,
                age,
                agePrivate,
                statusEmoji,
                statusText,
                statusCreate,
                introduction,
                true,
                ip
            )
        }
    }
}