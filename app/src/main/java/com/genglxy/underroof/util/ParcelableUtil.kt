package com.genglxy.underroof.util

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator


/**
 * How to use<a></a>>
 * - Make a simple object (POJO)
 * - Create a parcelable in ONE CLICK! http://devk.it/proj/parcelabler/
 * - Convert to pojo <-> byte[]
 *
 * Example
 * MyParcelable happy = new MyParcelable();
 * byte[] toByte = ParcelableUtil.marshall(happy);
 * // Save to DB? Send via socket?
 * // ...
 * // Restore from DB?
 * byte[] fromByte = cursor.getBlob(c);
 * MyParcelable happy = ParcelableUtil.unmarshall(fromByte, MyParcelable.CREATOR);
 */
object ParcelableUtil {
    fun marshall(parcelable: Parcelable): ByteArray {
        val parcel = Parcel.obtain()
        parcelable.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle() // not sure if needed or a good idea
        return bytes
    }

    fun <T : Parcelable?> unmarshall(bytes: ByteArray, creator: Creator<T>): T {
        val parcel = unmarshall(bytes)
        return creator.createFromParcel(parcel)
    }

    fun unmarshall(bytes: ByteArray): Parcel {
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0) // this is extremely important!
        return parcel
    }
}