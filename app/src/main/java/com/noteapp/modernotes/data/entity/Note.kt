package com.noteapp.modernotes.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

/**
 * 笔记实体类
 * 包含笔记的所有基本信息
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: String, // 使用String存储ISO格式的日期时间
    val modifiedAt: String,
    val color: String = "#FFFFFF", // 笔记颜色，默认白色
    val category: String = "默认", // 分类标签
    val isDeleted: Boolean = false // 软删除标记
)

/**
 * 笔记颜色枚举
 */
enum class NoteColor(val colorCode: String, val displayName: String) {
    WHITE("#FFFFFF", "白色"),
    YELLOW("#FFF9C4", "黄色"),
    ORANGE("#FFE0B2", "橙色"),
    RED("#FFCDD2", "红色"),
    PURPLE("#E1BEE7", "紫色"),
    BLUE("#BBDEFB", "蓝色"),
    GREEN("#C8E6C9", "绿色"),
    GRAY("#F5F5F5", "灰色")
}