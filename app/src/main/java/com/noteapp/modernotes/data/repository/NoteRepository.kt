package com.noteapp.modernotes.data.repository

import com.noteapp.modernotes.data.dao.NoteDao
import com.noteapp.modernotes.data.entity.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 笔记仓库类
 * 封装数据访问逻辑，提供统一的数据接口
 */
@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    
    /**
     * 获取所有笔记
     */
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    
    /**
     * 根据ID获取笔记
     */
    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)
    
    /**
     * 搜索笔记
     */
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
    
    /**
     * 按分类获取笔记
     */
    fun getNotesByCategory(category: String): Flow<List<Note>> = 
        noteDao.getNotesByCategory(category)
    
    /**
     * 获取所有分类
     */
    fun getAllCategories(): Flow<List<String>> = noteDao.getAllCategories()
    
    /**
     * 获取回收站笔记
     */
    fun getDeletedNotes(): Flow<List<Note>> = noteDao.getDeletedNotes()
    
    /**
     * 创建新笔记
     */
    suspend fun createNote(title: String, content: String, category: String = "默认", color: String = "#FFFFFF"): Long {
        val currentTime = getCurrentTimeString()
        val note = Note(
            title = title,
            content = content,
            createdAt = currentTime,
            modifiedAt = currentTime,
            category = category,
            color = color
        )
        return noteDao.insertNote(note)
    }
    
    /**
     * 更新笔记
     */
    suspend fun updateNote(note: Note) {
        val updatedNote = note.copy(modifiedAt = getCurrentTimeString())
        noteDao.updateNote(updatedNote)
    }
    
    /**
     * 删除笔记（移到回收站）
     */
    suspend fun deleteNote(id: Long) {
        noteDao.softDeleteNote(id, getCurrentTimeString())
    }
    
    /**
     * 恢复笔记
     */
    suspend fun restoreNote(id: Long) {
        noteDao.restoreNote(id, getCurrentTimeString())
    }
    
    /**
     * 永久删除笔记
     */
    suspend fun permanentlyDeleteNote(note: Note) {
        noteDao.deleteNote(note)
    }
    
    /**
     * 批量删除笔记
     */
    suspend fun deleteNotes(ids: List<Long>) {
        noteDao.deleteNotes(ids)
    }
    
    /**
     * 清空回收站
     */
    suspend fun clearTrash() {
        noteDao.clearTrash()
    }
    
    /**
     * 获取当前时间字符串
     */
    private fun getCurrentTimeString(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
    }
}