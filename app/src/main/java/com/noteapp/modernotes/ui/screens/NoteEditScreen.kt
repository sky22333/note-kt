package com.noteapp.modernotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noteapp.modernotes.data.entity.NoteColor
import com.noteapp.modernotes.ui.viewmodel.NoteEditViewModel

/**
 * 笔记编辑屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long,
    onNavigateBack: () -> Unit,
    viewModel: NoteEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showColorPicker by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    
    // 加载笔记数据
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }
    
    // 自动保存逻辑
    LaunchedEffect(uiState.title, uiState.content) {
        if (!uiState.isNewNote && (uiState.title.isNotBlank() || uiState.content.isNotBlank())) {
            viewModel.autoSave()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isNewNote) "新建笔记" else "编辑笔记",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (viewModel.saveNote()) {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 颜色选择按钮
                    IconButton(
                        onClick = { showColorPicker = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(uiState.color)))
                        )
                    }
                    
                    // 分类设置按钮
                    IconButton(
                        onClick = { showCategoryDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = "设置分类"
                        )
                    }
                    
                    // 保存按钮
                    IconButton(
                        onClick = {
                            if (viewModel.saveNote()) {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "保存"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(android.graphics.Color.parseColor(uiState.color)))
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 标题输入框
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "标题",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 内容输入框
                    OutlinedTextField(
                        value = uiState.content,
                        onValueChange = { viewModel.updateContent(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        placeholder = {
                            Text(
                                text = "开始写作...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    
                    // 底部信息栏
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 分类显示
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = uiState.category,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        // 字数统计
                        Text(
                            text = "${uiState.wordCount} 字",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
    
    // 颜色选择器
    if (showColorPicker) {
        ColorPickerDialog(
            selectedColor = uiState.color,
            onColorSelected = { color ->
                viewModel.updateColor(color)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
    
    // 分类设置对话框
    if (showCategoryDialog) {
        CategoryInputDialog(
            currentCategory = uiState.category,
            onCategorySet = { category ->
                viewModel.updateCategory(category)
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
    
    // 错误消息显示
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // 显示错误提示
            viewModel.clearError()
        }
    }
    
    // 处理返回键
    BackHandler {
        if (viewModel.saveNote()) {
            onNavigateBack()
        }
    }
}

/**
 * 颜色选择对话框
 */
@Composable
private fun ColorPickerDialog(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("选择颜色")
        },
        text = {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(NoteColor.entries) { noteColor ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(noteColor.colorCode)))
                            .clickable { onColorSelected(noteColor.colorCode) }
                            .then(
                                if (selectedColor == noteColor.colorCode) {
                                    Modifier.padding(4.dp)
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == noteColor.colorCode) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "已选择",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
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

/**
 * 分类输入对话框
 */
@Composable
private fun CategoryInputDialog(
    currentCategory: String,
    onCategorySet: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var category by remember { mutableStateOf(currentCategory) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("设置分类")
        },
        text = {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("分类名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCategorySet(category.ifBlank { "默认" })
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}