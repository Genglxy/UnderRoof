package com.genglxy.underroof.logic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Packet(
    val flag: Int,
    val data: ByteArray
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Packet

        if (flag != other.flag) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flag
        result = 31 * result + data.contentHashCode()
        return result
    }
}