package com.genglxy.underroof.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.genglxy.underroof.logic.model.Conversation
import com.genglxy.underroof.logic.model.Message
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation ORDER BY name")
    fun getConversationsFlow(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversation WHERE exposed=(:exposed) ORDER BY name")
    fun getExposedConversations(exposed: Boolean): List<Conversation>

    @Query("SELECT * FROM conversation WHERE id=(:id)")
    suspend fun getConversation(id: UUID): Conversation

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addConversation(conversation: Conversation)
}
