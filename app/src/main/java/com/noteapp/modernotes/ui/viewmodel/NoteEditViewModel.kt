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
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                if (state.isNewNote) {
                    // 创建新笔记
                    repository.createNote(
                        title = state.title.ifBlank { "无标题" },
                        content = state.content,
                        category = state.category,
                        color = state.color
                    )
                } else {
                    // 更新现有笔记
                    state.note?.let { note ->
                        val updatedNote = note.copy(
                            title = state.title.ifBlank { "无标题" },
                            content = state.content,
                            category = state.category,
                            color = state.color
                        )
                        repository.updateNote(updatedNote)
                    }
                }
                
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = null
                    )
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
     * 自动保存
     */
    fun autoSave() {
        val state = _uiState.value
        
        // 只有在有内容变化时才自动保存
        if (state.title.isNotBlank() || state.content.isNotBlank()) {
            saveNote()
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
    }
}