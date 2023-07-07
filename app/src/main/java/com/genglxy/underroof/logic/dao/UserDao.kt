package com.genglxy.underroof.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface UserDao {
    @Query("SELECT * FROM user ORDER BY name")
    fun getUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE online=(:online) ORDER BY name")
    suspend fun getActiveUsers(online: Boolean): List<User>

    @Query("SELECT * FROM user ORDER BY name")
    suspend fun getUsers(): List<User>

    @Query("SELECT * FROM user WHERE id=(:id)")
    suspend fun getUser(id: UUID): User?

    @Query("SELECT * FROM user WHERE ip=(:ip)")
    suspend fun getUser(ip: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: User)
}
