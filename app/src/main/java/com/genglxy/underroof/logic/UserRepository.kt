package com.genglxy.underroof.logic

import android.content.Context
import androidx.room.Room
import com.genglxy.underroof.logic.dao.UserDatabase
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

private const val DATABASE_NAME = "user-database"

class UserRepository private constructor(context: Context){

    private val database: UserDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            UserDatabase::class.java,
            DATABASE_NAME
        )
        .build()

    fun getUsersFlow(): Flow<List<User>> = database.userDao().getUsersFlow()

    suspend fun getActiveUsers() = database.userDao().getActiveUsers(true)

    suspend fun getUsers() = database.userDao().getUsers()

    suspend fun addUser(user: User) {
        database.userDao().addUser(user)
    }

    suspend fun getUser(id: UUID): User? = database.userDao().getUser(id)

    suspend fun getUser(ip: String): User? = database.userDao().getUser(ip)

    companion object {
        private var INSTANCE: UserRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = UserRepository(context)
            }
        }

        fun get(): UserRepository {
            return INSTANCE ?:
            throw IllegalStateException("ResultRepository must be initialized")
        }
    }
}