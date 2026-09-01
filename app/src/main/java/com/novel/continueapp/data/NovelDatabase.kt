package com.novel.continueapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.novel.continueapp.model.Book
import com.novel.continueapp.model.NovelRecord

@Database(entities = [NovelRecord::class, Book::class], version = 2, exportSchema = false)
abstract class NovelDatabase : RoomDatabase() {
    abstract fun novelRecordDao(): NovelRecordDao
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile private var INSTANCE: NovelDatabase? = null

        fun getInstance(context: Context): NovelDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NovelDatabase::class.java,
                    "novel_continue.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}