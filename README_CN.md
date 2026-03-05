# 音乐播放器项目

本仓库包含两个功能完整的音乐播放器，分别针对Android和macOS平台开发。

## 📱 Android音乐播放器

**目录**: `app/`  
**平台**: Android 7.0+  
**语言**: Kotlin  
**框架**: Material Design 3, ExoPlayer

### 特点
- 原生Android应用
- 后台播放和通知控制
- 音频焦点管理
- 专业级音质

### 快速开始
```bash
# 使用Android Studio打开项目
# 构建并运行到Android设备
```

详见 [Android版README](app/README.md)

---

## 💻 macOS音乐播放器

**目录**: `mac-music-player/`  
**平台**: macOS 10.14+  
**语言**: Python 3.8+  
**框架**: PyQt6, VLC

### 特点
- 桌面端大屏体验
- VLC专业音频引擎
- 快速开发和部署
- 易于定制扩展

### 快速开始

#### 1. 安装VLC
```bash
brew install --cask vlc
```

#### 2. 安装依赖（已完成✅）
```bash
cd mac-music-player
source venv/bin/activate
```

#### 3. 运行程序
```bash
./run.sh
```

详见 [macOS版README](mac-music-player/README.md)

---

## 🎯 功能对比

| 功能 | Android版 | macOS版 |
|------|-----------|---------|
| 本地音乐导入 | ✅ | ✅ |
| 多种排序方式 | ✅ (8种) | ✅ (8种) |
| 播放模式 | ✅ (3种) | ✅ (3种) |
| 歌词显示 | ✅ | ✅ |
| 歌词同步 | ✅ | ✅ |
| 点击歌词跳转 | ✅ | ✅ |
| 完整歌词查看 | ✅ | ✅ |
| 播放状态缓存 | ✅ | ✅ |
| 专辑封面 | ✅ | ✅ |
| 后台播放 | ✅ | ✅ |
| 通知控制 | ✅ | ❌ |

## 📚 核心功能

### 1. 音乐管理
- 指定目录导入本地音乐
- 支持多种音频格式（MP3, FLAC, WAV等）
- 自动提取元数据
- 专辑封面显示

### 2. 排序功能
- 默认按创建日期排序
- 支持8种排序方式

### 3. 播放模式
- 列表循环（默认）
- 单曲循环
- 随机播放

### 4. 歌词功能
- 自动检测.lrc和.txt歌词文件
- 实时同步显示
- 当前行高亮
- 点击跳转
- 完整歌词对话框

### 5. 状态缓存
- 记住上次播放的歌曲
- 保存播放位置
- 保存播放模式和排序设置

### 6. 音质优化
- Android: ExoPlayer专业级
- macOS: VLC专业级

## 🔧 技术栈对比

### Android版
- **语言**: Kotlin
- **架构**: MVVM
- **UI**: Material Design 3
- **数据库**: Room (SQLite)
- **播放器**: ExoPlayer
- **依赖注入**: Hilt

### macOS版
- **语言**: Python 3.8+
- **架构**: MVC
- **UI**: PyQt5（兼容macOS 12+）
- **数据库**: SQLite
- **播放器**: python-vlc
- **元数据**: mutagen

## 📖 文档

### Android版
- [完整README](app/README.md)
- [构建说明](app/build.gradle)

### macOS版
- [完整README](mac-music-player/README.md)
- [快速入门](mac-music-player/QUICKSTART.md)
- [安装指南](mac-music-player/INSTALL.md)
- [项目总结](mac-music-player/总结.md)

## 🎵 歌词格式

两个版本都支持相同的歌词格式：

### LRC格式（推荐）
```
[00:12.50]第一行歌词
[00:17.20]第二行歌词
[00:21.10]第三行歌词
```

### 纯文本格式
```
第一行歌词
第二行歌词
第三行歌词
```

## 🚀 选择建议

### 选择Android版，如果你：
- 需要移动端音乐播放器
- 要求专业级音频质量
- 需要后台播放和通知控制
- 计划发布到应用商店

### 选择macOS版，如果你：
- 需要桌面端音乐播放器
- 想要快速开发和部署
- 需要易于定制的解决方案
- 在办公环境使用

## 📝 注意事项

### Android版
- 需要Android Studio
- 最低Android 7.0
- 需要存储权限

### macOS版
- 需要Python 3.8+
- **需要安装VLC**（必需）
- macOS 10.14+

## 🎉 项目特点

1. **功能完整** - 实现所有需求功能
2. **双平台支持** - Android + macOS
3. **相同核心逻辑** - 一致的用户体验
4. **独立运行** - 互不影响
5. **易于扩展** - 清晰的代码结构

## 📦 目录结构

```
.
├── app/                      # Android音乐播放器
│   ├── src/
│   ├── build.gradle
│   └── ...
├── mac-music-player/         # macOS音乐播放器
│   ├── src/
│   ├── main.py
│   ├── requirements.txt
│   └── ...
├── PROJECT_COMPARISON.md     # 详细对比文档
└── README_CN.md             # 本文件
```

## 🤝 贡献

欢迎提交问题和改进建议！

## 📄 许可证

本项目仅供学习和个人使用。

---

**享受你的音乐之旅！** 🎵