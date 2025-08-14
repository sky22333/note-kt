package com.noteapp.modernotes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteapp.modernotes.data.entity.Note
import com.noteapp.modernotes.data.repository.NoteRepository
import com.noteapp.modernotes.ui.state.NoteEditUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 笔记编辑ViewModel
 * 管理笔记编辑的状态和业务逻辑
 */
@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()
    
    // 跟踪最后保存的内容，避免重复保存
    private var lastSavedTitle = ""
    private var lastSavedContent = ""
    private var lastSavedCategory = ""
    private var lastSavedColor = ""
    
    /**
     * 加载笔记（编辑模式）
     */
    fun loadNote(noteId: Long) {
        if (noteId == 0L) {
            // 新建笔记模式
            _uiState.update { 
                it.copy(
                    isNewNote = true,
                    title = "",
                    content = "",
                    category = "默认",
                    color = "#FFFFFF"
                )
            }
            // 重置保存状态
            resetLastSavedState()
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val note = repository.getNoteById(noteId)
                if (note != null) {
                    _uiState.update { 
                        it.copy(
                            note = note,
                            title = note.title,
                            content = note.content,
                            category = note.category,
                            color = note.color,
                            isNewNote = false,
                            isLoading = false,
                            wordCount = note.content.length
                        )
                    }
                    // 更新最后保存的状态
                    updateLastSavedState(note.title, note.content, note.category, note.color)
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "笔记不存在"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载笔记失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 更新标题
     */
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }
    
    /**
     * 更新内容
     */
    fun updateContent(content: String) {
        _uiState.update { 
            it.copy(
                content = content,
                wordCount = content.length
            )
        }
    }
    
    /**
     * 更新分类
     */
    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }
    
    /**
     * 更新颜色
     */
    fun updateColor(color: String) {
        _uiState.update { it.copy(color = color) }
    }
    
    /**
     * 保存笔记
     */
    fun saveNote(): Boolean {
        val state = _uiState.value
        
        // 验证输入
        if (state.title.isBlank() && state.content.isBlank()) {
            _uiState.update { it.copy(errorMessage = "标题和内容不能都为空") }
            return false
        }
        
        // 检查是否有变化，避免重复保存
        val finalTitle = state.title.ifBlank { "无标题" }
        if (!hasChanges(finalTitle, state.content, state.category, state.color)) {
            return true // 没有变化，返回成功
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                if (state.isNewNote) {
                    // 创建新笔记
                    val newNote = repository.createNote(
                        title = finalTitle,
                        content = state.content,
                        category = state.category,
                        color = state.color
                    )
                    
                    // 更新状态，包含新创建的笔记对象
                    _uiState.update { 
                        it.copy(
                            note = newNote,
                            title = newNote?.title ?: finalTitle,
                            isSaving = false,
                            errorMessage = null,
                            isNewNote = false // 保存后标记为非新笔记
                        )
                    }
                    
                    // 更新最后保存的状态
                    newNote?.let { note ->
                        updateLastSavedState(note.title, note.content, note.category, note.color)
                    }
                } else {
                    // 更新现有笔记
                    state.note?.let { note ->
                        val updatedNote = note.copy(
                            title = finalTitle,
                            content = state.content,
                            category = state.category,
                            color = state.color
                        )
                        repository.updateNote(updatedNote)
                        
                        // 更新状态
                        _uiState.update { 
                            it.copy(
                                note = updatedNote,
                                isSaving = false,
                                errorMessage = null
                            )
                        }
                        
                        // 更新最后保存的状态
                        updateLastSavedState(updatedNote.title, updatedNote.content, updatedNote.category, updatedNote.color)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = "保存失败: ${e.message}"
                    )
                }
            }
        }
        
        return true
    }
    
    /**
     * 自动保存 - 静默保存，不更新UI状态
     */
    fun autoSave() {
        val state = _uiState.value
        
        // 检查是否需要保存
        if (state.isSaving || state.isLoading) {
            return // 如果正在保存或加载，跳过
        }
        
        // 只有在有内容变化时才自动保存
        if (state.title.isBlank() && state.content.isBlank()) {
            return // 内容为空，不保存
        }
        
        // 检查是否有变化，避免重复保存
        val finalTitle = state.title.ifBlank { "无标题" }
        if (!hasChanges(finalTitle, state.content, state.category, state.color)) {
            return // 没有变化，跳过保存
        }
        
        viewModelScope.launch {
            try {
                if (state.isNewNote) {
                    // 创建新笔记
                    val newNote = repository.createNote(
                        title = finalTitle,
                        content = state.content,
                        category = state.category,
                        color = state.color
                    )
                    
                    // 标记为非新笔记，避免重复创建，并更新笔记对象
                    _uiState.update { 
                        it.copy(
                            note = newNote,
                            isNewNote = false
                        ) 
                    }
                    
                    // 更新最后保存的状态
                    newNote?.let { note ->
                        updateLastSavedState(note.title, note.content, note.category, note.color)
                    }
                } else {
                    // 更新现有笔记
                    state.note?.let { note ->
                        val updatedNote = note.copy(
                            title = finalTitle,
                            content = state.content,
                            category = state.category,
                            color = state.color
                        )
                        repository.updateNote(updatedNote)
                        
                        // 更新笔记对象
                        _uiState.update { it.copy(note = updatedNote) }
                        
                        // 更新最后保存的状态
                        updateLastSavedState(updatedNote.title, updatedNote.content, updatedNote.category, updatedNote.color)
                    }
                }
            } catch (e: Exception) {
                // 静默处理错误，不影响用户体验
                // 可以记录日志，但不更新UI状态
            }
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * 重置状态
     */
    fun resetState() {
        _uiState.value = NoteEditUiState()
        resetLastSavedState()
    }
    
    /**
     * 检查内容是否有变化
     */
    private fun hasChanges(title: String, content: String, category: String, color: String): Boolean {
        return title != lastSavedTitle || 
               content != lastSavedContent || 
               category != lastSavedCategory || 
               color != lastSavedColor
    }
    
    /**
     * 更新最后保存的状态
     */
    private fun updateLastSavedState(title: String, content: String, category: String, color: String) {
        lastSavedTitle = title
        lastSavedContent = content
        lastSavedCategory = category
        lastSavedColor = color
    }
    
    /**
     * 重置最后保存的状态
     */
    private fun resetLastSavedState() {
        lastSavedTitle = ""
        lastSavedContent = ""
        lastSavedCategory = ""
        lastSavedColor = ""
    }
}