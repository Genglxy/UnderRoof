package com.genglxy.underroof.logic

import android.content.Context
import androidx.room.Room
import com.genglxy.underroof.logic.dao.ConversationDatabase
import com.genglxy.underroof.logic.dao.MessageDatabase
import com.genglxy.underroof.logic.model.Conversation
import com.genglxy.underroof.logic.model.Message
import kotlinx.coroutines.flow.Flow
import java.util.UUID

private const val DATABASE_NAME = "message-database"

class MessageRepository private constructor(context: Context) {

    private val database: MessageDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            MessageDatabase::class.java,
            DATABASE_NAME
        )
        .build()

    fun getMessages(id: UUID): Flow<List<Message>> = database.messageDao().getMessages(id)

    fun getAllMessages(): Flow<List<Message>> = database.messageDao().getAllMessages()

    suspend fun addMessage(message: Message) {
        database.messageDao().addMessage(message)
    }

    fun getRMessages(): List<Message> {
        return database.messageDao().getRMessages()
    }

    suspend fun getMessage(id: UUID): Message =
        database.messageDao().getMessage(id)

    suspend fun checkMessage(id: UUID): Message? =
        database.messageDao().checkMessage(id)

    companion object {
        private var INSTANCE: MessageRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = MessageRepository(context)
            }
        }

        fun get(): MessageRepository {
            return INSTANCE ?: throw IllegalStateException("ResultRepository must be initialized")
        }
    }
}