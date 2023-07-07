package com.genglxy.underroof.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.genglxy.underroof.logic.model.Message
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface MessageDao {
    @Query("SELECT * FROM message WHERE conversation=(:id) ORDER BY createTime")
    fun getMessages(id:UUID): Flow<List<Message>>
    @Query("SELECT * FROM message ORDER BY createTime")
    fun getAllMessages(): Flow<List<Message>>

    @Query("SELECT * FROM message ORDER BY createTime")
    fun getRMessages(): List<Message>

    @Query("SELECT * FROM message WHERE id=(:id)")
    suspend fun getMessage(id: UUID): Message

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMessage(message: Message)
}
