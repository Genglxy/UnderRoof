package com.genglxy.underroof.logic

import android.content.Context
import androidx.room.Room
import com.genglxy.underroof.logic.dao.FileDatabase
import com.genglxy.underroof.logic.model.File
import kotlinx.coroutines.flow.Flow
import java.util.UUID

private const val DATABASE_NAME = "file-database"

class FileRepository private constructor(context: Context) {

    private val database: FileDatabase = Room.databaseBuilder(
        context.applicationContext,
        FileDatabase::class.java,
        DATABASE_NAME
    )
        .build()

    suspend fun addFile(file: File) {
        database.fileDao().addFile(file)
    }

    suspend fun getFile(id: UUID): File =
        database.fileDao().getFile(id)

    companion object {
        private var INSTANCE: FileRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = FileRepository(context)
            }
        }

        fun get(): FileRepository {
            return INSTANCE ?: throw IllegalStateException("ResultRepository must be initialized")
        }
    }
}