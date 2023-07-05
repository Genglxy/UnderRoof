package com.genglxy.underroof.logic.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.genglxy.underroof.logic.model.Message
import com.genglxy.underroof.logic.model.User

@Database(entities = [Message::class], version = 1)
@TypeConverters(MessageTypeConverters::class)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}