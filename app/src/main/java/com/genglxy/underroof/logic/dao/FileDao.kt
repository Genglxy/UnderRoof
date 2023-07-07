package com.genglxy.underroof.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.genglxy.underroof.logic.model.Conversation
import com.genglxy.underroof.logic.model.File
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface FileDao {

    @Query("SELECT * FROM file WHERE id=(:id)")
    suspend fun getFile(id: UUID): File

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFile(file: File)
}
