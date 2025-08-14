package com.noteapp.modernotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noteapp.modernotes.ui.components.NoteCard
import com.noteapp.modernotes.ui.components.SearchBar
import com.noteapp.modernotes.ui.viewmodel.NotesViewModel

/**
 * 笔记列表主屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToEdit: (Long) -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCategoryDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "我的笔记",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // 视图切换按钮
                    IconButton(
                        onClick = { viewModel.toggleViewMode() }
                    ) {
                        Icon(
                            imageVector = if (uiState.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = if (uiState.isGridView) "列表视图" else "网格视图"
                        )
                    }
                    
                    // 分类筛选按钮
                    IconButton(
                        onClick = { showCategoryDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "分类筛选"
                        )
                    }
                    
                    // 选择模式下的操作按钮
                    if (uiState.isSelectionMode) {
                        IconButton(
                            onClick = { viewModel.toggleSelectAll() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "全选"
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.deleteSelectedNotes() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除选中"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode) {
                FloatingActionButton(
                    onClick = { onNavigateToEdit(0L) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "新建笔记"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // 搜索栏
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.searchNotes(it) },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // 当前分类显示
            if (uiState.selectedCategory != "全部") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "分类: ${uiState.selectedCategory}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    TextButton(
                        onClick = { viewModel.selectCategory("全部") }
                    ) {
                        Text("清除筛选")
                    }
                }
            }
            
            // 笔记列表
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.notes.isEmpty() -> {
                    EmptyNotesView(
                        searchQuery = uiState.searchQuery,
                        onCreateNote = { onNavigateToEdit(0L) }
                    )
                }
                
                else -> {
                    if (uiState.isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.notes) { note ->
                                NoteCard(
                                    note = note,
                                    isSelected = note.id in uiState.selectedNotes,
                                    isSelectionMode = uiState.isSelectionMode,
                                    onClick = {
                                        if (uiState.isSelectionMode) {
                                            viewModel.toggleNoteSelection(note.id)
                                        } else {
                                            onNavigateToEdit(note.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (!uiState.isSelectionMode) {
                                            viewModel.enterSelectionMode(note.id)
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.notes) { note ->
                                NoteCard(
                                    note = note,
                                    isSelected = note.id in uiState.selectedNotes,
                                    isSelectionMode = uiState.isSelectionMode,
                                    onClick = {
                                        if (uiState.isSelectionMode) {
                                            viewModel.toggleNoteSelection(note.id)
                                        } else {
                                            onNavigateToEdit(note.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (!uiState.isSelectionMode) {
                                            viewModel.enterSelectionMode(note.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 分类选择对话框
    if (showCategoryDialog) {
        CategorySelectionDialog(
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = { category ->
                viewModel.selectCategory(category)
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
    
    // 错误消息显示
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // 这里可以显示Snackbar或其他错误提示
            viewModel.clearError()
        }
    }
    
    // 处理返回键退出选择模式
    BackHandler(enabled = uiState.isSelectionMode) {
        viewModel.exitSelectionMode()
    }
}

/**
 * 空状态视图
 */
@Composable
private fun EmptyNotesView(
    searchQuery: String,
    onCreateNote: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (searchQuery.isEmpty()) Icons.Default.Note else Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (searchQuery.isEmpty()) "还没有笔记" else "没有找到相关笔记",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (searchQuery.isEmpty()) "点击右下角的 + 按钮创建第一个笔记" else "尝试使用其他关键词搜索",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        if (searchQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onCreateNote
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("创建笔记")
            }
        }
    }
}

/**
 * 分类选择对话框
 */
@Composable
private fun CategorySelectionDialog(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("选择分类")
        },
        text = {
            LazyColumn {
                items(categories) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = category == selectedCategory,
                            onClick = { onCategorySelected(category) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}