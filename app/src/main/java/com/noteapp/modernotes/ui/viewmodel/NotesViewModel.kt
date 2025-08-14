package com.noteapp.modernotes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteapp.modernotes.data.entity.Note
import com.noteapp.modernotes.data.repository.NoteRepository
import com.noteapp.modernotes.ui.state.NotesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

/**
 * 笔记列表ViewModel
 * 管理笔记列表的状态和业务逻辑
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()
    
    init {
        loadNotes()
        loadCategories()
    }
    
    private var notesJob: Job? = null
    
    /**
     * 加载笔记列表
     */
    private fun loadNotes() {
        // 取消之前的任务
        notesJob?.cancel()
        
        notesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val notesFlow = when {
                    _uiState.value.searchQuery.isNotEmpty() -> {
                        repository.searchNotes(_uiState.value.searchQuery)
                    }
                    _uiState.value.selectedCategory != "全部" -> {
                        repository.getNotesByCategory(_uiState.value.selectedCategory)
                    }
                    else -> {
                        repository.getAllNotes()
                    }
                }
                
                notesFlow.collect { notes ->
                    _uiState.update { 
                        it.copy(
                            notes = notes,
                            isLoading = false,
                            errorMessage = null
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
     * 加载分类列表
     */
    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _uiState.update { 
                    it.copy(categories = listOf("全部") + categories)
                }
            }
        }
    }
    
    /**
     * 搜索笔记
     */
    fun searchNotes(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadNotes()
    }
    
    /**
     * 选择分类
     */
    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadNotes()
    }
    
    /**
     * 切换视图模式
     */
    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }
    
    /**
     * 删除笔记
     */
    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteNote(noteId)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "删除笔记失败: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 批量删除笔记
     */
    fun deleteSelectedNotes() {
        viewModelScope.launch {
            try {
                repository.deleteNotes(_uiState.value.selectedNotes.toList())
                exitSelectionMode()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "批量删除失败: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 进入选择模式
     */
    fun enterSelectionMode(noteId: Long) {
        _uiState.update { 
            it.copy(
                isSelectionMode = true,
                selectedNotes = setOf(noteId)
            )
        }
    }
    
    /**
     * 退出选择模式
     */
    fun exitSelectionMode() {
        _uiState.update { 
            it.copy(
                isSelectionMode = false,
                selectedNotes = emptySet()
            )
        }
    }
    
    /**
     * 切换笔记选择状态
     */
    fun toggleNoteSelection(noteId: Long) {
        _uiState.update { state ->
            val selectedNotes = if (noteId in state.selectedNotes) {
                state.selectedNotes - noteId
            } else {
                state.selectedNotes + noteId
            }
            
            state.copy(
                selectedNotes = selectedNotes,
                isSelectionMode = selectedNotes.isNotEmpty()
            )
        }
    }
    
    /**
     * 全选/取消全选
     */
    fun toggleSelectAll() {
        _uiState.update { state ->
            val allSelected = state.selectedNotes.size == state.notes.size
            val selectedNotes = if (allSelected) {
                emptySet()
            } else {
                state.notes.map { it.id }.toSet()
            }
            
            state.copy(
                selectedNotes = selectedNotes,
                isSelectionMode = selectedNotes.isNotEmpty()
            )
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}