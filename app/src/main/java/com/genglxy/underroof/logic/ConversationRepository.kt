package com.genglxy.underroof.logic

import android.content.Context
import androidx.room.Room
import com.genglxy.underroof.logic.dao.ConversationDao
import com.genglxy.underroof.logic.dao.ConversationDatabase
import com.genglxy.underroof.logic.dao.UserDatabase
import com.genglxy.underroof.logic.model.Conversation
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

private const val DATABASE_NAME = "conversation-database"

class ConversationRepository private constructor(context: Context){

    private val database: ConversationDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            ConversationDatabase::class.java,
            DATABASE_NAME
        )
        .build()

    fun getConversations(): Flow<List<Conversation>> = database.conversationDao().getConversations()

    suspend fun addConversation(conversation: Conversation) {
        database.conversationDao().addConversation(conversation)
    }

    suspend fun getConversation(id: UUID): Conversation = database.conversationDao().getConversation(id)

    companion object {
        private var INSTANCE: ConversationRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = ConversationRepository(context)
            }
        }

        fun get(): ConversationRepository {
            return INSTANCE ?:
            throw IllegalStateException("ResultRepository must be initialized")
        }
    }
}