package com.genglxy.underroof.logic.dao

import android.net.Uri
import androidx.room.TypeConverter

class ConversationTypeConverters {
    @TypeConverter
    fun fromUri(uri: Uri): String {
        return uri.toString()
    }

    @TypeConverter
    fun toUri(string: String): Uri {
        return Uri.parse(string)
    }
}
