package com.genglxy.underroof.logic.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Entity
@Parcelize
data class Message(
    @PrimaryKey val id: UUID,
    val type: Int,
    val subType: Int,
    val from: UUID,
    val conversation: UUID,
    val content: String,
    val createTime: Long
) : Parcelable {
    companion object {
        const val TYPE_SYSTEM = 0
        const val TYPE_MESSAGE = 1
        const val TYPE_FILE = 2

        //系统消息子类
        const val SUBTYPE_JOIN = 0
        const val SUBTYPE_QUIT = 1
        const val SUBTYPE_RECEIVED = 2 //收到消息回执
        const val SUBTYPE_ONLINE = 3
        const val SUBTYPE_OFFLINE = 4
        const val SUBTYPE_GROUP_QUERY = 5


        //文件消息子类
        const val SUBTYPE_TEXT = 0
        const val SUBTYPE_IMAGE = 1
        const val SUBTYPE_FILE = 2
    }
}
