package com.genglxy.underroof.logic.model

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.net.InetAddress
import java.util.UUID

@Entity
@Parcelize
data class User(
    @PrimaryKey val id: UUID,
    val photo: Uri,
    val photoThumbnail: Uri,
    val name: String,
    val gender: Int,
    val genderPrivate: Boolean,
    val age: Int,
    val agePrivate: Boolean,
    val statusEmoji: String,
    val statusText: String,
    val statusCreate: Long,
    val introduction: String,
    val online: Boolean,
    val ip: String
) : Parcelable {

    companion object {
        const val GENDER_MALE = 0
        const val GENDER_FEMALE = 1
    }
    fun toUserLite(): UserLite {
        this.apply {
            return UserLite(
                id,
                name,
                gender,
                genderPrivate,
                age,
                agePrivate,
                statusEmoji,
                statusText,
                statusCreate,
                introduction
            )
        }
    }
}
