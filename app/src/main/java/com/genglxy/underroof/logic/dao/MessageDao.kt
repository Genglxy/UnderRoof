package com.genglxy.underroof.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.genglxy.underroof.logic.model.Message
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface MessageDao {
    @Query("SELECT * FROM message")
    fun getMessages(): Flow<List<Message>>

    @Query("SELECT * FROM message WHERE id=(:id)")
    suspend fun getMessage(id: UUID): Message

    @Insert
    suspend fun addMessage(message: Message)
}
