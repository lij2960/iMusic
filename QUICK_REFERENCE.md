# iMusic 快速参考

## 🚀 快速开始

### Android 版本
```bash
cd app
./gradlew assembleRelease
```

### macOS Python 版本
```bash
cd mac-music-player

# 开发
bash run.sh

# 打包
bash create_app_py312.sh
bash create_dmg_py312.sh
```

### macOS Swift 版本
```bash
cd macos-music-player-swift
open MusicPlayer.xcodeproj
```

## 📦 打包产物

| 版本 | 文件 | 大小 | 位置 |
|------|------|------|------|
| Android | APK | ~10MB | `app/build/outputs/apk/` |
| Python | APP | 180MB | `mac-music-player/iMusic_py312.app` |
| Python | DMG | 152MB | `mac-music-player/iMusic-1.0.1-py312.dmg` |
| Swift | APP | ~15MB | `macos-music-player-swift/build/` |

## 📚 核心文档

| 文档 | 说明 |
|------|------|
| [README_CN.md](README_CN.md) | 项目总览 |
| [PROJECT_STATUS.md](PROJECT_STATUS.md) | 项目状态 |
| [CLEANUP_SUMMARY.md](CLEANUP_SUMMARY.md) | 清理总结 |
| [mac-music-player/README.md](mac-music-player/README.md) | Python 版本说明 |
| [mac-music-player/完整打包流程.md](mac-music-player/完整打包流程.md) | 打包流程 |

## 🔧 常用命令

### Python 版本开发
```bash
cd mac-music-player

# 首次设置
bash setup.sh

# 运行应用
bash run.sh

# 清理构建
bash cleanup.sh
```

### Python 版本打包
```bash
cd mac-music-player

# 设置 Python 3.12（首次）
bash setup_python312.sh

# 打包应用
bash create_app_py312.sh

# 测试应用
bash test_final.sh

# 创建 DMG
bash create_dmg_py312.sh
```

### Git 操作
```bash
# 查看状态
git status

# 添加所有文件
git add .

# 提交
git commit -m "你的提交信息"

# 推送
git push
```

## 🎯 项目结构

```
iMusic/
├── app/                        # Android 版本
├── mac-music-player/          # Python 版本
│   ├── src/                   # 源代码
│   ├── iMusic_py312.app/      # 打包的应用
│   └── iMusic-1.0.1-py312.dmg # DMG 安装包
├── macos-music-player-swift/  # Swift 版本
├── README_CN.md               # 项目总览
├── PROJECT_STATUS.md          # 项目状态
└── QUICK_REFERENCE.md         # 本文件
```

## ✨ 功能特性

- 🎵 音乐播放（MP3, WAV, FLAC, AAC, OGG, M4A, WMA）
- 🎤 歌词显示和同步
- 🌐 在线歌词搜索
- 🎨 专辑封面显示
- 🔀 多种播放模式
- 📊 排序功能
- 💾 状态保存

## 📝 版本对比

| 特性 | Android | Python | Swift |
|------|---------|--------|-------|
| 平台 | Android 7.0+ | macOS 10.13+ | macOS 12.0+ |
| 语言 | Kotlin | Python 3.12 | Swift |
| 大小 | ~10MB | 180MB | ~15MB |
| 启动 | 快 | 中等 | 快 |
| 打包 | ✅ | ✅ | ✅ |

## 🐛 故障排除

### Python 版本无法启动
```bash
# 查看错误
./mac-music-player/iMusic_py312.app/Contents/MacOS/iMusic

# 重新打包
cd mac-music-player
bash create_app_py312.sh
```

### VLC 错误
```bash
# 安装 VLC
brew install --cask vlc
```

### 数据库错误
```bash
# 删除数据库
rm ~/Library/Application\ Support/iMusic/music_player.db
```

## 📮 联系方式

- GitHub: [@yourusername](https://github.com/yourusername)
- Email: your@email.com

---

**最后更新**: 2026-03-13
