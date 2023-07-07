package com.genglxy.underroof.logic.model

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Entity
@Parcelize
data class File(
    @PrimaryKey val id: UUID,
    val uri: Uri,
    val type: Int
    ): Parcelable {
        companion object {
            const val TYPE_IMAGE = 0
        }
    }