# 现代笔记APP

一个使用现代Android开发技术栈构建的记事本应用，采用Material Design 3设计规范，提供流畅的用户体验。

## 功能特性

### 核心功能
- ✅ 创建、编辑、删除笔记
- ✅ 自动保存机制
- ✅ 实时搜索功能
- ✅ 分类标签系统
- ✅ 笔记颜色标记
- ✅ 网格/列表视图切换
- ✅ 批量操作支持

### 用户体验
- ✅ Material Design 3 设计规范
- ✅ 深色/浅色主题自适应
- ✅ 流畅的动画效果
- ✅ 响应式布局设计

## 技术架构

### 核心技术栈
- **开发语言**: Kotlin
- **架构模式**: MVVM + Repository Pattern
- **UI框架**: Jetpack Compose
- **数据库**: Room + SQLite
- **依赖注入**: Hilt/Dagger
- **异步处理**: Kotlin Coroutines + Flow

### 主要依赖
- Navigation Compose (导航管理)
- ViewModel + StateFlow (状态管理)
- kotlinx-datetime (日期时间处理)
- Compose Animation API (动画效果)
- Material 3 Components (UI组件)

## 项目结构

```
app/src/main/java/com/noteapp/modernotes/
├── data/                          # 数据层
│   ├── entity/                    # 数据实体
│   │   └── Note.kt               # 笔记实体类
│   ├── dao/                      # 数据访问对象
│   │   └── NoteDao.kt            # 笔记DAO接口
│   ├── database/                 # 数据库配置
│   │   └── NoteDatabase.kt       # Room数据库
│   └── repository/               # 仓库层
│       └── NoteRepository.kt     # 笔记仓库
├── di/                           # 依赖注入
│   └── DatabaseModule.kt         # 数据库模块
├── ui/                           # UI层
│   ├── components/               # 可复用组件
│   │   ├── NoteCard.kt          # 笔记卡片
│   │   └── SearchBar.kt         # 搜索栏
│   ├── screens/                  # 屏幕页面
│   │   ├── NotesScreen.kt       # 笔记列表页面
│   │   └── NoteEditScreen.kt    # 笔记编辑页面
│   ├── state/                    # UI状态
│   │   └── NoteUiState.kt       # 状态定义
│   ├── viewmodel/                # 视图模型
│   │   ├── NotesViewModel.kt    # 笔记列表VM
│   │   └── NoteEditViewModel.kt # 笔记编辑VM
│   └── theme/                    # 主题样式
│       ├── Color.kt             # 颜色定义
│       ├── Theme.kt             # 主题配置
│       └── Type.kt              # 字体样式
├── navigation/                   # 导航
│   └── NoteNavigation.kt        # 导航配置
├── MainActivity.kt               # 主Activity
└── NoteApplication.kt           # 应用入口
```

## 编译和运行

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 8 或更高版本
- Android SDK API 24-34
- Kotlin 1.9.22

### 编译步骤
1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击运行按钮或使用快捷键 Ctrl+R (Windows/Linux) 或 Cmd+R (Mac)

### 构建命令
```bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease

# 安装到设备
./gradlew installDebug
```

## 应用说明

### 主要界面
- 笔记列表页面：展示所有笔记，支持搜索和筛选
- 笔记编辑页面：创建和编辑笔记，支持颜色和分类设置
- 网格视图：紧凑的卡片布局
- 列表视图：详细的列表布局

## 开发说明

### 代码规范
- 遵循Kotlin官方编码规范
- 使用中文注释说明关键逻辑
- 采用MVVM架构模式
- 合理使用Compose状态管理

### 性能优化
- 使用LazyColumn/LazyVerticalGrid优化列表性能
- 合理使用remember和derivedStateOf减少重组
- 数据库操作使用协程避免阻塞UI线程
- 图片和资源合理压缩

## 贡献指南

欢迎提交Issue和Pull Request来改进这个项目！
