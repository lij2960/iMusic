# 更新说明

## ✅ 已修复 - macOS 12兼容性问题

### 问题
原版本使用PyQt6，需要macOS 13.0+，但你的系统是macOS 12.7.6。

### 解决方案
已将GUI框架从PyQt6降级到PyQt5，完美兼容macOS 12。

## 🎉 当前状态

### ✅ 已完成
1. **PyQt5安装成功** - 兼容macOS 12.7.6
2. **python-vlc安装成功** - 音频播放引擎
3. **mutagen安装成功** - 元数据提取
4. **VLC检测成功** - 播放引擎已就绪
5. **所有代码已更新** - 从PyQt6迁移到PyQt5

### 📦 已安装的依赖
```
PyQt5==5.15.11          ✅
python-vlc==3.0.21203   ✅
mutagen==1.47.0         ✅
```

### 🖥️ 系统信息
- macOS版本: 12.7.6 ✅
- Python版本: 3.14.2 ✅
- VLC: 已安装 ✅

## 🚀 立即运行

### 方法1: 使用运行脚本
```bash
./run.sh
```

### 方法2: 手动运行
```bash
source venv/bin/activate
python main.py
```

### 方法3: 测试安装
```bash
source venv/bin/activate
python test_install.py
```

## 📝 主要变更

### 代码变更
1. `requirements.txt` - PyQt6 → PyQt5
2. `main.py` - 更新导入和API调用
3. `src/ui/main_window.py` - 更新所有Qt API调用
   - `Qt.AlignmentFlag.AlignCenter` → `Qt.AlignCenter`
   - `Qt.Orientation.Horizontal` → `Qt.Horizontal`
   - `Qt.ItemDataRole.UserRole` → `Qt.UserRole`
   - `Qt.GlobalColor.blue` → `Qt.blue`
   - `app.exec()` → `app.exec_()`

### 文档更新
- README.md - 更新技术栈说明
- 总结.md - 更新框架信息
- README_CN.md - 更新对比信息

## 🎯 功能验证

所有核心功能保持不变：
- ✅ 音乐导入和扫描
- ✅ 8种排序方式
- ✅ 3种播放模式
- ✅ 歌词解析和同步
- ✅ 播放状态缓存
- ✅ VLC音频播放

## 💡 使用提示

### 首次运行
1. 启动程序
2. 点击"添加音乐文件夹"
3. 选择音乐目录
4. 双击歌曲播放

### 添加歌词
1. 创建与音乐文件同名的.lrc文件
2. 放在同一目录
3. 格式: `[00:12.50]歌词内容`

### 支持格式
- 音频: MP3, FLAC, WAV, AAC, OGG, M4A, WMA
- 歌词: .lrc (带时间戳), .txt (纯文本)

## 🐛 故障排除

### 如果程序无法启动
```bash
# 重新激活虚拟环境
source venv/bin/activate

# 测试安装
python test_install.py

# 运行程序
python main.py
```

### 如果提示找不到模块
```bash
# 重新安装依赖
pip install -r requirements.txt
```

### 如果音乐无法播放
- 确认VLC已安装
- 检查音频文件格式
- 尝试使用MP3格式

## 📚 相关文档

- [README.md](README.md) - 完整功能说明
- [QUICKSTART.md](QUICKSTART.md) - 快速入门
- [INSTALL.md](INSTALL.md) - 安装指南
- [总结.md](总结.md) - 项目总结

## ✨ 下一步

程序已经可以运行了！

```bash
./run.sh
```

享受你的音乐播放器！🎵