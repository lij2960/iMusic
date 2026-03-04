# 图标和封面处理指南

## 概述

本文档说明macOS音乐播放器如何处理应用图标和专辑封面，确保与Android版本保持一致。

## 1. 应用图标

### 设计规范（与Android Material Design一致）

- **背景**: 紫色圆形 (#6200EE / RGB: 98, 0, 238)
- **图标**: 白色音符符号
- **尺寸**: 默认128x128，可自定义
- **风格**: Material Design风格，简洁现代

### 实现位置

- 文件: `src/resources/app_icon.py`
- 方法: `AppIcon.create_app_icon(size=128)`
- 应用位置: `src/ui/main_window.py` 的 `__init__` 方法

### 代码示例

```python
# 在MainWindow.__init__中设置
self.setWindowIcon(AppIcon.create_app_icon())
```

### 验证方法

运行测试脚本查看图标效果：
```bash
python3 mac-music-player/test_icons.py
```

## 2. 默认专辑封面

### 设计规范（与Android版本一致）

- **背景**: 浅灰色 (#F0F0F0 / RGB: 240, 240, 240)
- **图标**: 深灰色音符 (#C8C8C8 / RGB: 200, 200, 200)
- **尺寸**: 默认200x200，可自定义
- **用途**: 当歌曲没有专辑封面时显示

### 实现位置

- 文件: `src/resources/app_icon.py`
- 方法: `AppIcon.create_default_album_art(size=200)`
- 应用位置: `src/ui/main_window.py` 的 `__init__` 方法

### 代码示例

```python
# 在MainWindow.__init__中创建
self.default_album_art = AppIcon.create_default_album_art()

# 在on_song_changed中使用
if not cover_loaded:
    self.album_art_label.setPixmap(self.default_album_art)
```

## 3. 专辑封面搜索逻辑

### 搜索优先级（与Android版本完全一致）

扫描音乐文件时，按以下顺序查找专辑封面：

1. **与音乐文件同名的封面**（优先级最高）
   - `{歌曲名}.jpg`
   - `{歌曲名}.jpeg`
   - `{歌曲名}.png`

2. **通用封面名称**
   - `cover.jpg`
   - `cover.jpeg`
   - `cover.png`
   - `folder.jpg`
   - `folder.jpeg`
   - `folder.png`
   - `album.jpg`
   - `album.jpeg`
   - `album.png`

### 实现位置

- 文件: `src/utils/music_scanner.py`
- 方法: `MusicScanner._get_album_art_path(music_path)`

### 代码逻辑

```python
@staticmethod
def _get_album_art_path(music_path: Path) -> str:
    """获取专辑封面路径（与Android版本逻辑完全一致）"""
    directory = music_path.parent
    file_name = music_path.stem
    
    # 按照Android版本的优先级顺序查找封面
    art_files = [
        # 1. 与音乐文件同名的封面（优先级最高）
        f"{file_name}.jpg",
        f"{file_name}.jpeg", 
        f"{file_name}.png",
        # 2. 通用封面名称（按Android版本顺序）
        "cover.jpg",
        "cover.jpeg",
        "cover.png",
        "folder.jpg",
        "folder.jpeg",
        "folder.png",
        "album.jpg",
        "album.jpeg",
        "album.png"
    ]
    
    for art_file in art_files:
        art_path = directory / art_file
        if art_path.exists() and art_path.is_file():
            print(f"✅ Found album art: {art_path}")
            return str(art_path)
    
    print(f"ℹ️  No album art found for: {music_path.name}")
    return None
```

## 4. 封面加载流程

### 播放歌曲时的封面加载

在 `MainWindow.on_song_changed()` 方法中：

1. **检查歌曲是否有封面路径**
   ```python
   if song.album_art:
   ```

2. **尝试加载封面图片**
   ```python
   pixmap = QPixmap(song.album_art)
   if not pixmap.isNull():
       scaled_pixmap = pixmap.scaled(200, 200, Qt.KeepAspectRatio, Qt.SmoothTransformation)
       self.album_art_label.setPixmap(scaled_pixmap)
       cover_loaded = True
   ```

3. **加载失败或无封面时显示默认封面**
   ```python
   if not cover_loaded:
       self.album_art_label.setPixmap(self.default_album_art)
   ```

### 调试日志

程序运行时会输出以下日志：

- `✅ Found album art: /path/to/cover.jpg` - 找到封面
- `ℹ️  No album art found for: song.mp3` - 未找到封面
- `✅ Loaded album art from: /path/to/cover.jpg` - 成功加载封面
- `❌ Error loading album art: error message` - 加载失败
- `ℹ️  Using default album art for: 歌曲名` - 使用默认封面

## 5. 与Android版本的对比

### 相同点

✅ 应用图标设计（紫色圆形，白色音符）
✅ 默认封面设计（灰色背景，灰色音符）
✅ 封面搜索优先级顺序
✅ 支持的图片格式（jpg, jpeg, png）
✅ 封面文件名匹配逻辑

### 差异点

⚠️ **存储位置**:
- Android: 可能使用内部存储目录 (`context.filesDir/album_art`)
- macOS: 直接从音乐文件目录读取

⚠️ **在线下载**:
- Android: 支持从网易云音乐API下载封面
- macOS: 当前版本未实现（可以后续添加）

## 6. 测试方法

### 测试应用图标

1. 运行程序：
   ```bash
   ./run.sh
   ```

2. 检查窗口标题栏的图标是否为紫色圆形音符

3. 运行图标测试脚本：
   ```bash
   python3 mac-music-player/test_icons.py
   ```

### 测试专辑封面

1. **准备测试文件**：
   ```
   /music/
   ├── song1.mp3
   ├── song1.jpg          # 与歌曲同名的封面
   ├── song2.mp3
   ├── cover.jpg          # 通用封面
   └── song3.mp3          # 无封面，应显示默认封面
   ```

2. **添加音乐文件夹**：
   - 点击"添加音乐文件夹"
   - 选择测试目录

3. **验证封面加载**：
   - 播放 song1.mp3 → 应显示 song1.jpg
   - 播放 song2.mp3 → 应显示 cover.jpg
   - 播放 song3.mp3 → 应显示默认灰色封面

4. **检查控制台日志**：
   ```
   ✅ Found album art: /music/song1.jpg
   ✅ Loaded album art from: /music/song1.jpg
   ℹ️  No album art found for: song3.mp3
   ℹ️  Using default album art for: song3
   ```

## 7. 常见问题

### Q: 应用图标没有显示？

A: 检查以下几点：
1. 确认 `AppIcon.create_app_icon()` 在 `__init__` 中被调用
2. 确认 PyQt5 正确安装
3. 在macOS上，Dock图标可能需要重启应用才能更新

### Q: 专辑封面没有显示？

A: 检查以下几点：
1. 确认封面文件存在且格式正确（jpg/jpeg/png）
2. 确认封面文件名符合搜索规则
3. 查看控制台日志确认搜索过程
4. 确认封面文件有读取权限

### Q: 封面显示模糊？

A: 封面会自动缩放到200x200，使用 `Qt.SmoothTransformation` 保证质量。如果原图太小，可能会模糊。建议使用至少200x200的封面图片。

### Q: 如何添加在线封面下载功能？

A: 可以参考Android版本的 `downloadAlbumArt` 方法，使用网易云音乐API下载封面。需要：
1. 在 `src/api/` 中添加封面搜索API
2. 在 UI 中添加"搜索封面"按钮
3. 下载后保存到音乐文件目录

## 8. 总结

macOS版本的图标和封面处理逻辑已经与Android版本保持一致：

✅ 应用图标：紫色圆形，白色音符（Material Design风格）
✅ 默认封面：灰色背景，灰色音符
✅ 封面搜索：12种文件名模式，优先级顺序一致
✅ 封面加载：自动缩放，失败时显示默认封面
✅ 调试日志：详细的加载过程日志

如果遇到问题，请查看控制台日志进行调试。
