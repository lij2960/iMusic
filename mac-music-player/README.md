# iMusic - macOS 音乐播放器

一个功能完整的 macOS 音乐播放器，使用 Python + PyQt5 + VLC 开发。

## ✨ 功能特性

- 🎵 **音乐播放** - 支持 MP3, WAV, FLAC, AAC, OGG, M4A, WMA
- 🎤 **歌词显示** - 自动加载本地歌词，在线搜索歌词，实时同步
- 🎨 **专辑封面** - 自动提取和显示专辑封面
- 🔀 **播放模式** - 列表循环、单曲循环、随机播放
- 📊 **排序功能** - 8种排序方式（日期、标题、艺术家、时长）
- 💾 **状态保存** - 记住播放位置、播放模式、排序设置

## 🚀 快速开始

### 开发模式

```bash
# 1. 设置开发环境
bash setup.sh

# 2. 运行应用
bash run.sh
```

### 打包应用

```bash
# 1. 设置 Python 3.12 环境（首次）
bash setup_python312.sh

# 2. 打包应用
bash create_app_py312.sh

# 3. 测试应用
bash test_final.sh

# 4. 创建 DMG 安装包
bash create_dmg_py312.sh
```

## 📦 打包结果

- **应用**: `iMusic_py312.app` (180MB) - 需要自己构建
- **安装包**: `iMusic-1.0.1-py312.dmg` (152MB) - 需要自己构建
- **状态**: ✅ 完全可用，包含所有依赖

**注意**: 由于文件过大，.app 和 .dmg 文件不包含在 Git 仓库中。请按照上述步骤自己构建。

## 📚 文档

- [完整打包流程.md](完整打包流程.md) - 从源代码到 DMG 的完整流程
- [Python3.12打包成功.md](Python3.12打包成功.md) - 技术细节和解决方案
- [DMG安装包说明.md](DMG安装包说明.md) - DMG 分发和安装说明
- [快速使用指南.md](快速使用指南.md) - 用户使用指南

## 🛠️ 技术栈

- **Python** 3.12.8
- **PyQt5** 5.15.10 - GUI 框架
- **python-vlc** 3.0.20123 - 音频播放引擎
- **mutagen** 1.47.0 - 音频元数据提取
- **PyInstaller** 6.19.0 - 应用打包

## 📋 系统要求

- macOS 10.13 或更高版本
- 开发需要 Python 3.12+
- 打包的应用无需安装 Python

## 🎯 项目结构

```
mac-music-player/
├── src/                    # 源代码
│   ├── api/               # API 接口（歌词搜索）
│   ├── database/          # 数据库管理
│   ├── models/            # 数据模型
│   ├── player/            # 音乐播放器
│   ├── resources/         # 资源文件
│   ├── ui/                # 用户界面
│   └── utils/             # 工具函数
├── main.py                # 应用入口
├── requirements.txt       # Python 依赖
├── setup.sh              # 开发环境设置
├── run.sh                # 运行应用
├── setup_python312.sh    # Python 3.12 环境设置
├── create_app_py312.sh   # 打包应用
├── create_dmg_py312.sh   # 创建 DMG
├── test_final.sh         # 测试应用
└── README.md             # 本文件
```

## 🔧 开发

### 安装依赖

```bash
# 创建虚拟环境
python3 -m venv venv
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

### 运行应用

```bash
# 开发模式
python3 main.py

# 或使用脚本
bash run.sh
```

### 代码结构

- `src/ui/main_window.py` - 主窗口界面
- `src/player/music_player.py` - 音乐播放器核心
- `src/api/lyrics_api.py` - 歌词搜索 API
- `src/database/db_manager.py` - 数据库管理
- `src/utils/music_scanner.py` - 音乐文件扫描
- `src/utils/lyrics_parser.py` - 歌词解析

## 📝 使用说明

### 首次使用

1. 启动应用
2. 点击"扫描音乐"按钮
3. 选择音乐文件夹
4. 等待扫描完成

### 播放音乐

- 双击歌曲开始播放
- 使用底部控制按钮（播放/暂停/上一首/下一首）
- 拖动进度条跳转

### 查看歌词

- 如果有本地歌词文件（.lrc 或 .txt）会自动加载
- 点击"搜索歌词"在线搜索
- 点击歌词行跳转到对应时间

### 数据位置

- **开发模式**: `./music_player.db`
- **打包应用**: `~/Library/Application Support/iMusic/music_player.db`

## 🐛 故障排除

### 应用无法启动

```bash
# 查看错误信息
./iMusic_py312.app/Contents/MacOS/iMusic
```

### VLC 错误

确保已安装 VLC：
```bash
brew install --cask vlc
```

### 数据库错误

删除数据库重新扫描：
```bash
rm ~/Library/Application\ Support/iMusic/music_player.db
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

Copyright © 2026 iJackey. All rights reserved.

## 🔗 相关项目

- [Android 版本](../app) - Android 音乐播放器
- [Swift 版本](../macos-music-player-swift) - Swift 原生 macOS 应用

## 📮 联系方式

- GitHub: [@yourusername](https://github.com/yourusername)
- Email: your@email.com

---

**注意**: 首次运行打包的应用需要右键点击 → 打开，之后可以直接双击运行。
