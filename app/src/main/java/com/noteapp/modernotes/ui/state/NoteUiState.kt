package com.noteapp.modernotes.ui.state

import com.noteapp.modernotes.data.entity.Note

/**
 * 笔记列表UI状态
 */
data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "全部",
    val categories: List<String> = emptyList(),
    val isGridView: Boolean = false,
    val selectedNotes: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 笔记编辑UI状态
 */
data class NoteEditUiState(
    val note: Note? = null,
    val title: String = "",
    val content: String = "",
    val category: String = "默认",
    val color: String = "#FFFFFF",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val wordCount: Int = 0,
    val errorMessage: String? = null,
    val isNewNote: Boolean = true
)

/**
 * 应用主题状态
 */
data class ThemeUiState(
    val isDarkTheme: Boolean = false,
    val useDynamicColor: Boolean = true
)