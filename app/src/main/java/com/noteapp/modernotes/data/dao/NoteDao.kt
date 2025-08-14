package com.noteapp.modernotes.data.dao

import androidx.room.*
import com.noteapp.modernotes.data.entity.Note
import kotlinx.coroutines.flow.Flow

/**
 * 笔记数据访问对象
 * 定义所有数据库操作
 */
@Dao
interface NoteDao {
    
    /**
     * 获取所有未删除的笔记，按修改时间倒序排列
     */
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY modifiedAt DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    /**
     * 根据ID获取笔记
     */
    @Query("SELECT * FROM notes WHERE id = :id AND isDeleted = 0")
    suspend fun getNoteById(id: Long): Note?
    
    /**
     * 搜索笔记（按标题和内容）
     */
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY modifiedAt DESC")
    fun searchNotes(query: String): Flow<List<Note>>
    
    /**
     * 按分类获取笔记
     */
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND category = :category ORDER BY modifiedAt DESC")
    fun getNotesByCategory(category: String): Flow<List<Note>>
    
    /**
     * 获取所有分类
     */
    @Query("SELECT DISTINCT category FROM notes WHERE isDeleted = 0")
    fun getAllCategories(): Flow<List<String>>
    
    /**
     * 获取回收站中的笔记
     */
    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY modifiedAt DESC")
    fun getDeletedNotes(): Flow<List<Note>>
    
    /**
     * 插入笔记
     */
    @Insert
    suspend fun insertNote(note: Note): Long
    
    /**
     * 更新笔记
     */
    @Update
    suspend fun updateNote(note: Note)
    
    /**
     * 软删除笔记（移到回收站）
     */
    @Query("UPDATE notes SET isDeleted = 1, modifiedAt = :modifiedAt WHERE id = :id")
    suspend fun softDeleteNote(id: Long, modifiedAt: String)
    
    /**
     * 恢复笔记
     */
    @Query("UPDATE notes SET isDeleted = 0, modifiedAt = :modifiedAt WHERE id = :id")
    suspend fun restoreNote(id: Long, modifiedAt: String)
    
    /**
     * 永久删除笔记
     */
    @Delete
    suspend fun deleteNote(note: Note)
    
    /**
     * 批量删除笔记
     */
    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun deleteNotes(ids: List<Long>)
    
    /**
     * 清空回收站
     */
    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun clearTrash()
}