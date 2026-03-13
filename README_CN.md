# iMusic - 跨平台音乐播放器

本仓库包含三个版本的音乐播放器，分别针对 Android、macOS (Python) 和 macOS (Swift) 平台开发。

## 📱 Android 版本

**目录**: `app/`  
**平台**: Android 7.0+  
**语言**: Kotlin  
**框架**: Material Design 3, ExoPlayer

### 特点
- ✅ 原生 Android 应用
- ✅ 后台播放和通知控制
- ✅ 音频焦点管理
- ✅ 专业级音质
- ✅ Material Design 3 界面

### 快速开始
```bash
# 使用 Android Studio 打开项目
# 构建并运行到 Android 设备
./gradlew assembleRelease
```

详见 [Android 版 README](app/README.md)

---

## 💻 macOS 版本 (Python)

**目录**: `mac-music-player/`  
**平台**: macOS 10.13+  
**语言**: Python 3.12  
**框架**: PyQt5, VLC

### 特点
- ✅ 桌面端大屏体验
- ✅ VLC 专业音频引擎
- ✅ 在线歌词搜索
- ✅ 完整打包方案
- ✅ DMG 安装包

### 快速开始

#### 开发模式
```bash
cd mac-music-player

# 设置环境
bash setup.sh

# 运行应用
bash run.sh
```

#### 打包应用
```bash
# 设置 Python 3.12 环境
bash setup_python312.sh

# 打包应用
bash create_app_py312.sh

# 创建 DMG 安装包
bash create_dmg_py312.sh
```

### 打包结果
- **应用**: `iMusic_py312.app` (180MB)
- **安装包**: `iMusic-1.0.1-py312.dmg` (152MB)
- **状态**: ✅ 完全可用，包含所有依赖

详见 [Python 版 README](mac-music-player/README.md)

---

## 🍎 macOS 版本 (Swift)

**目录**: `macos-music-player-swift/`  
**平台**: macOS 12.0+  
**语言**: Swift  
**框架**: SwiftUI, AVFoundation

### 特点
- ✅ 原生 macOS 应用
- ✅ SwiftUI 现代界面
- ✅ 体积小（~15MB）
- ✅ 启动快（~1秒）
- ✅ 低内存占用

### 快速开始
```bash
cd macos-music-player-swift

# 使用 Xcode 打开项目
open MusicPlayer.xcodeproj

# 或使用脚本构建
bash build_release.sh
```

详见 [Swift 版 README](macos-music-player-swift/README.md)

---

## 🎯 功能对比

| 功能 | Android | macOS (Python) | macOS (Swift) |
|------|---------|----------------|---------------|
| 音乐播放 | ✅ | ✅ | ✅ |
| 歌词显示 | ✅ | ✅ | ✅ |
| 在线歌词搜索 | ✅ | ✅ | ✅ |
| 专辑封面 | ✅ | ✅ | ✅ |
| 播放模式 | ✅ | ✅ | ✅ |
| 后台播放 | ✅ | ✅ | ✅ |
| 通知控制 | ✅ | ❌ | ❌ |
| 应用大小 | ~10MB | ~180MB | ~15MB |
| 启动速度 | 快 | 中等 | 快 |

## 📊 技术栈对比

### Android
- **语言**: Kotlin
- **UI**: Jetpack Compose / XML
- **音频**: ExoPlayer
- **数据库**: Room
- **架构**: MVVM

### macOS (Python)
- **语言**: Python 3.12
- **UI**: PyQt5
- **音频**: VLC
- **数据库**: SQLite
- **打包**: PyInstaller

### macOS (Swift)
- **语言**: Swift
- **UI**: SwiftUI
- **音频**: AVFoundation
- **数据库**: SQLite
- **架构**: MVVM

## 🚀 推荐使用

- **Android 用户**: 使用 Android 版本
- **macOS 用户（追求性能）**: 使用 Swift 版本
- **macOS 用户（需要快速开发）**: 使用 Python 版本
- **跨平台开发**: 参考三个版本的实现

## 📝 共同特性

所有版本都支持：

- 🎵 多种音频格式（MP3, WAV, FLAC, AAC, OGG, M4A, WMA）
- 🎤 本地歌词文件自动加载
- 🌐 在线歌词搜索
- 🎨 专辑封面显示
- 🔀 多种播放模式（列表循环、单曲循环、随机）
- 📊 多种排序方式
- 💾 播放状态保存

## 🛠️ 开发环境

### Android
- Android Studio Arctic Fox+
- JDK 11+
- Android SDK 24+

### macOS (Python)
- Python 3.12+
- VLC Media Player
- macOS 10.13+

### macOS (Swift)
- Xcode 14+
- macOS 12.0+

## 📚 文档

- [Android 版文档](app/README.md)
- [Python 版文档](mac-music-player/README.md)
- [Swift 版文档](macos-music-player-swift/README.md)
- [Python 打包流程](mac-music-player/完整打包流程.md)
- [DMG 安装包说明](mac-music-player/DMG安装包说明.md)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

Copyright © 2026 iJackey. All rights reserved.

## 📮 联系方式

- GitHub: [@yourusername](https://github.com/yourusername)
- Email: your@email.com

---

**注意**: 
- Android 版本需要 Android 7.0+ 设备
- macOS Python 版本需要 macOS 10.13+
- macOS Swift 版本需要 macOS 12.0+
