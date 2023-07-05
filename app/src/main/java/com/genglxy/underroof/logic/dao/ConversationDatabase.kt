package com.genglxy.underroof.logic.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.genglxy.underroof.logic.model.Conversation
import com.genglxy.underroof.logic.model.Message

@Database(entities = [Conversation::class], version = 1)
@TypeConverters(ConversationTypeConverters::class)
abstract class ConversationDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
}