# iMusic - Android 音乐播放器 v1.0.0

一个功能完整的Android音乐播放器，支持本地音乐播放、歌词显示、多种播放模式、在线资源搜索等功能。

## 📱 应用特色

### 🎵 核心功能
- **本地音乐管理** - 指定目录导入或全盘扫描音乐文件
- **高质量播放** - 使用ExoPlayer引擎，支持多种音频格式
- **智能歌词** - 自动加载LRC歌词，支持同步显示和点击跳转
- **专辑封面** - 自动显示专辑封面，支持在线搜索下载
- **播放模式** - 顺序播放、随机播放、单曲循环
- **音频均衡器** - 专业音频调节，多种预设模式

### 🌐 在线功能
- **歌词搜索** - 网易云音乐API，丰富的中文歌词资源
- **封面搜索** - 在线搜索和下载高质量专辑封面
- **放屁网音乐** - 集成放屁网音乐搜索和下载

### 🎨 用户体验
- **Material Design 3** - 现代化界面设计
- **自定义进度条** - 流畅拖动，无白色边框
- **状态缓存** - 记住播放位置、模式设置
- **搜索功能** - 按歌名、艺术家、文件名搜索

## 🚀 技术架构

### 核心技术
- **MVVM架构** - 清晰的代码结构
- **Jetpack Compose** - 现代UI框架
- **Hilt** - 依赖注入
- **Room** - 本地数据库
- **ExoPlayer** - 高质量音频播放
- **Coroutines + Flow** - 异步编程

### 网络服务
- **Retrofit** - 网络请求
- **Coil** - 图片加载
- **网易云音乐API** - 歌词和音乐搜索
- **OkHttp** - HTTP客户端

## 📋 系统要求

- **Android版本**: 7.0 (API 24) 及以上
- **存储权限**: 访问本地音乐文件
- **网络权限**: 在线功能使用
- **推荐内存**: 2GB以上

## 🎯 主要界面

### 1. 音乐库
- 显示所有导入的音乐
- 支持多种排序方式（创建日期、标题、艺术家、时长）
- 实时搜索功能
- 歌曲删除确认

### 2. 播放器
- 专辑封面显示
- 实时歌词显示
- 自定义进度条（支持流畅拖动）
- 播放控制按钮
- 播放模式切换

### 3. 歌词界面
- 全屏歌词显示
- 当前行高亮
- 点击跳转功能
- 渐变背景效果

### 4. 均衡器
- 5频段调节
- 多种预设模式
- 实时音效调节
- 自定义设置保存

### 5. 在线搜索
- 放屁网音乐搜索
- 自动浏览器跳转
- 下载进度显示

## 🔧 安装使用

### 安装方法
```bash
# 方法1: 直接安装APK
adb install app-debug.apk

# 方法2: 从源码构建
./gradlew assembleDebug
```

### 首次使用
1. 授予存储权限和网络权限
2. 点击"导入"选择音乐目录或全盘扫描
3. 等待扫描完成
4. 在音乐库中选择歌曲播放

### 歌词使用
1. 将.lrc歌词文件放在音乐文件同目录
2. 确保歌词文件名与音乐文件名相同
3. 播放时自动加载歌词
4. 点击"更新歌词"按钮在线搜索

### 专辑封面
1. 将封面图片放在音乐文件同目录
2. 支持cover.jpg、folder.jpg、album.jpg等文件名
3. 点击"更新封面"按钮在线搜索

## 🌐 API服务

### 网易云音乐API
- **基础URL**: https://music-api.heheda.top/
- **歌词搜索**: `/lyric?keywords={关键词}`
- **音乐搜索**: `/search?keywords={关键词}&type=1&limit=20`
- **特点**: 国内优化，访问速度快，中文资源丰富

### 放屁网API
- **音乐搜索**: 集成放屁网搜索功能
- **下载方式**: 自动跳转浏览器到Quark网盘
- **支持格式**: 多种音频格式

## 📦 支持格式

### 音频格式
- MP3, WAV, FLAC, AAC, OGG, M4A, WMA, OPUS

### 歌词格式
- LRC (带时间戳)
- TXT (纯文本)

### 封面格式
- JPG, PNG, WEBP, BMP

## 🛠️ 开发环境

### 必需工具
- **Android Studio** Hedgehog 2023.1.1+
- **JDK** 11+
- **Kotlin** 1.9.0+
- **Gradle** 8.0+

### 主要依赖
```kotlin
// UI框架
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// 音频播放
implementation("androidx.media3:media3-exoplayer")
implementation("androidx.media3:media3-session")

// 数据库
implementation("androidx.room:room-runtime")
implementation("androidx.room:room-ktx")

// 依赖注入
implementation("com.google.dagger:hilt-android")

// 网络请求
implementation("com.squareup.retrofit2:retrofit")
implementation("com.squareup.retrofit2:converter-gson")

// 图片加载
implementation("io.coil-kt:coil-compose")
```

## 🔍 故障排除

### 常见问题
- **无法播放音乐**: 检查存储权限和文件格式
- **歌词不显示**: 确认歌词文件编码为UTF-8
- **在线功能异常**: 检查网络连接和API状态
- **封面不显示**: 检查图片文件格式和权限

### 调试命令
```bash
# 查看应用日志
adb logcat | grep iMusic

# 测试API连接
curl "https://music-api.heheda.top/search?keywords=测试&type=1&limit=5"
```

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交GitHub Issue
- 发送邮件反馈

---

**iMusic v1.0.0** - 让音乐更美好 🎵