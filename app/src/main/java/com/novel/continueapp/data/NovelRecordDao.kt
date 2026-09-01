package com.novel.continueapp.data

import androidx.room.*
import com.novel.continueapp.model.NovelRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface NovelRecordDao {
    @Query("SELECT * FROM novel_records ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<NovelRecord>>

    @Query("SELECT * FROM novel_records ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatest(): NovelRecord?

    @Insert
    suspend fun insert(record: NovelRecord): Long

    @Update
    suspend fun update(record: NovelRecord)

    @Delete
    suspend fun delete(record: NovelRecord)

    @Query("DELETE FROM novel_records")
    suspend fun deleteAll()
}