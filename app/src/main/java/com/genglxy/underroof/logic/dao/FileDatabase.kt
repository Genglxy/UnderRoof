package com.genglxy.underroof.logic.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.genglxy.underroof.logic.model.Conversation
import com.genglxy.underroof.logic.model.File

@Database(entities = [File::class], version = 1)
@TypeConverters(FileTypeConverters::class)
abstract class FileDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
}