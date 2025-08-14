package com.noteapp.modernotes

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用程序入口类
 * 启用Hilt依赖注入
 */
@HiltAndroidApp
class NoteApplication : Application()