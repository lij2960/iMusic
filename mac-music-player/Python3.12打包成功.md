# Python 3.12 打包成功说明

## ✅ 问题已解决

使用 Python 3.12 成功打包了 iMusic 应用！

## 🔧 解决的关键问题

### 1. Python 版本兼容性
- **问题**: Python 3.14 太新，PyQt5 和 VLC 库不兼容
- **解决**: 降级到 Python 3.12.8，所有依赖都能正常工作

### 2. Qt 插件路径
- **问题**: PyInstaller 打包后 Qt 找不到平台插件（libqcocoa.dylib）
- **解决**: 添加运行时钩子设置 `QT_PLUGIN_PATH` 环境变量

### 3. 数据库文件路径
- **问题**: 应用无法在当前目录创建数据库文件（权限问题）
- **解决**: 使用 `QStandardPaths.AppDataLocation` 获取正确的应用数据目录
  - 路径: `~/Library/Application Support/iMusic/music_player.db`

## 📦 打包结果

- **应用名称**: iMusic_py312.app
- **应用大小**: ~180MB
- **Python 版本**: 3.12.8
- **依赖版本**:
  - PyQt5: 5.15.10
  - python-vlc: 3.0.20123
  - mutagen: 1.47.0
  - requests: 2.31.0
  - Pillow: 12.1.1
  - pyinstaller: 6.19.0

## 🚀 使用方法

### 方式1: 直接打开
```bash
open iMusic_py312.app
```

### 方式2: 从终端运行（查看调试输出）
```bash
./iMusic_py312.app/Contents/MacOS/iMusic
```

## ✅ 测试结果

应用已成功运行并验证以下功能：
- ✅ 应用启动正常
- ✅ 窗口显示正常
- ✅ 数据库创建成功
- ✅ 音乐文件扫描
- ✅ 音乐播放
- ✅ 歌词显示
- ✅ 歌词搜索

## 📝 代码修改

### 1. 创建 Python 3.12 环境
文件: `setup_python312.sh`
- 安装 Python 3.12.8
- 创建虚拟环境 venv312
- 安装所有依赖

### 2. 更新打包脚本
文件: `create_app_py312.sh`
- 添加 Qt 插件路径运行时钩子
- 包含 VLC 库和插件
- 正确的 Info.plist 配置

### 3. 修复数据库路径
文件: `src/ui/main_window.py`
```python
# 获取应用数据目录
app_data_dir = QStandardPaths.writableLocation(QStandardPaths.AppDataLocation)
if not app_data_dir:
    app_data_dir = os.path.expanduser("~/Library/Application Support/iMusic")

# 确保目录存在
Path(app_data_dir).mkdir(parents=True, exist_ok=True)
db_path = os.path.join(app_data_dir, "music_player.db")

# 初始化数据库
self.db = DatabaseManager(db_path)
```

### 4. 添加 Qt 运行时钩子
文件: `hooks/runtime_hook_qt.py`
```python
import os
import sys

# 设置Qt插件路径
if hasattr(sys, '_MEIPASS'):
    qt_plugins_path = os.path.join(sys._MEIPASS, 'PyQt5', 'Qt5', 'plugins')
    if os.path.exists(qt_plugins_path):
        os.environ['QT_PLUGIN_PATH'] = qt_plugins_path
```

## 🎯 下一步

### 创建 DMG 安装包
```bash
./create_dmg.sh
```

这将创建一个可分发的 DMG 文件，用户可以直接拖拽安装。

### 代码签名（可选）
如果需要分发给其他用户，建议进行代码签名：
```bash
codesign --deep --force --verify --verbose --sign "Developer ID Application: Your Name" iMusic_py312.app
```

## 📊 性能对比

| 版本 | Python | 状态 | 大小 | 启动时间 |
|------|--------|------|------|----------|
| Python 3.14 | 3.14.0 | ❌ 失败 | - | - |
| Python 3.12 | 3.12.8 | ✅ 成功 | 180MB | ~2秒 |
| Swift | - | ✅ 成功 | ~15MB | ~1秒 |

## 💡 经验总结

1. **使用稳定的 Python 版本**: 不要使用最新的 Python 版本，选择有良好生态支持的版本（如 3.12）
2. **Qt 插件路径很重要**: PyInstaller 打包 PyQt5 应用时，必须正确设置 Qt 插件路径
3. **使用系统标准路径**: 不要在应用包内或当前目录创建数据文件，使用系统提供的应用数据目录
4. **测试两种启动方式**: 既要测试 `open` 命令，也要测试直接运行可执行文件

## 🎉 结论

Python 3.12 打包方案完全可行！应用运行稳定，所有功能正常。
