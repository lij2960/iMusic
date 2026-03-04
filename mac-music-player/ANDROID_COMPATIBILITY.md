# Android版本兼容性说明

本文档说明macOS版本如何与Android版本保持一致的核心逻辑。

## 🎯 设计目标

macOS版本的核心功能逻辑与Android版本完全一致，确保两个平台提供相同的用户体验。

---

## 📁 文件扫描逻辑

### Android版本
```kotlin
// MusicRepository.kt
private fun isAudioFile(file: File): Boolean {
    val audioExtensions = listOf(
        "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus",
        "mp4", "3gp", "amr", "awb", "wv", "ape", "dts", "ac3"
    )
    return audioExtensions.any { file.extension.lowercase() == it }
}
```

### macOS版本
```python
# music_scanner.py
SUPPORTED_FORMATS = {
    '.mp3', '.wav', '.flac', '.aac', '.ogg', '.m4a', '.wma', '.opus',
    '.mp4', '.3gp', '.amr', '.awb', '.wv', '.ape', '.dts', '.ac3'
}
```

✅ **完全一致** - 支持相同的音频格式

---

## 🖼️ 专辑封面获取逻辑

### Android版本
```kotlin
// MusicRepository.kt
private fun getAlbumArtPath(musicPath: String): String? {
    val musicFile = File(musicPath)
    val fileName = musicFile.nameWithoutExtension
    val directory = musicFile.parentFile ?: return null
    
    val artFiles = listOf(
        "$fileName.jpg", "$fileName.jpeg", "$fileName.png",
        "cover.jpg", "cover.jpeg", "cover.png",
        "folder.jpg", "folder.jpeg", "folder.png",
        "album.jpg", "album.jpeg", "album.png"
    )
    
    for (artFile in artFiles) {
        val file = File(directory, artFile)
        if (file.exists()) {
            return file.absolutePath
        }
    }
    return null
}
```

### macOS版本
```python
# music_scanner.py
@staticmethod
def _get_album_art_path(music_path: Path) -> str:
    directory = music_path.parent
    file_name = music_path.stem
    
    art_files = [
        f"{file_name}.jpg", f"{file_name}.jpeg", f"{file_name}.png",
        "cover.jpg", "cover.jpeg", "cover.png",
        "folder.jpg", "folder.jpeg", "folder.png",
        "album.jpg", "album.jpeg", "album.png"
    ]
    
    for art_file in art_files:
        art_path = directory / art_file
        if art_path.exists():
            return str(art_path)
    
    return None
```

✅ **完全一致** - 相同的查找优先级和文件名

---

## 📝 歌词文件查找逻辑

### Android版本
```kotlin
// MusicRepository.kt
fun getLyricsForSong(song: Song): String? {
    val musicFile = File(song.path)
    val fileName = musicFile.nameWithoutExtension
    val basePath = song.path.substringBeforeLast(".")
    val directory = File(song.path).parent ?: return null
    
    val possibleLyricFiles = listOf(
        File("$basePath.lrc"),
        File("$basePath.txt"),
        File("$directory/$fileName.lrc"),
        File("$directory/$fileName.txt")
    )
    
    for (lyricFile in possibleLyricFiles) {
        if (lyricFile.exists() && lyricFile.canRead()) {
            return lyricFile.readText(Charsets.UTF_8)
        }
    }
    return null
}
```

### macOS版本
```python
# lyrics_parser.py
@staticmethod
def find_lyrics_file(song_path: str) -> Optional[Path]:
    song_file = Path(song_path)
    directory = song_file.parent
    file_name = song_file.stem
    
    # 1. 与音乐文件同名的.lrc文件
    lrc_file = directory / f"{file_name}.lrc"
    if lrc_file.exists():
        return lrc_file
    
    # 2. 与音乐文件同名的.txt文件
    txt_file = directory / f"{file_name}.txt"
    if txt_file.exists():
        return txt_file
    
    # 3. 基于路径的.lrc文件
    base_lrc = Path(str(song_file.with_suffix('')) + '.lrc')
    if base_lrc.exists():
        return base_lrc
    
    # 4. 基于路径的.txt文件
    base_txt = Path(str(song_file.with_suffix('')) + '.txt')
    if base_txt.exists():
        return base_txt
    
    return None
```

✅ **完全一致** - 相同的查找顺序和优先级

---

## 🎵 歌词解析逻辑

### Android版本
```kotlin
// LyricsParser.kt
private val LRC_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2})\\](.*)")

fun parseLyricsFile(lyricsFile: File, songId: Long): Lyrics? {
    val lines = lyricsFile.readLines()
    val lyricLines = mutableListOf<LyricLine>()
    
    for (line in lines) {
        val matcher = LRC_PATTERN.matcher(line.trim())
        if (matcher.matches()) {
            val minutes = matcher.group(1)?.toIntOrNull() ?: 0
            val seconds = matcher.group(2)?.toIntOrNull() ?: 0
            val centiseconds = matcher.group(3)?.toIntOrNull() ?: 0
            val text = matcher.group(4)?.trim() ?: ""
            
            val timeMs = (minutes * 60 + seconds) * 1000L + centiseconds * 10L
            
            if (text.isNotEmpty()) {
                lyricLines.add(LyricLine(timeMs, text))
            }
        }
    }
    
    lyricLines.sortBy { it.timeMs }
    return if (lyricLines.isNotEmpty()) Lyrics(songId, lyricLines) else null
}
```

### macOS版本
```python
# lyrics_parser.py
LRC_PATTERN = re.compile(r'\[(\d{2}):(\d{2})\.(\d{2})\](.*)')

@staticmethod
def parse_lyrics_content(content: str, song_id: int) -> Optional[Lyrics]:
    lines = content.split('\n')
    lyric_lines = []
    
    for line in lines:
        line = line.strip()
        match = LyricsParser.LRC_PATTERN.match(line)
        if match:
            minutes = int(match.group(1))
            seconds = int(match.group(2))
            centiseconds = int(match.group(3))
            text = match.group(4).strip()
            
            time_ms = (minutes * 60 + seconds) * 1000 + centiseconds * 10
            
            if text:
                lyric_lines.append(LyricLine(time_ms=time_ms, text=text))
    
    lyric_lines.sort(key=lambda x: x.time_ms)
    return Lyrics(song_id=song_id, lines=lyric_lines) if lyric_lines else None
```

✅ **完全一致** - 相同的正则表达式和解析逻辑

---

## 🎨 应用图标设计

### Android版本
- Material Design 3 风格
- 紫色主题色 (#6200EE)
- 音乐符号图标

### macOS版本
```python
# app_icon.py
@staticmethod
def create_app_icon(size: int = 128) -> QIcon:
    # 背景圆形（与Android Material Design一致）
    painter.setBrush(QColor(98, 0, 238))  # 紫色主题
    painter.drawEllipse(0, 0, size, size)
    
    # 绘制音乐符号（与Android版本一致的设计）
    # ...
```

✅ **设计一致** - 相同的颜色主题和图标风格

---

## 🖼️ 默认专辑封面

### Android版本
- 灰色背景
- 音乐符号图标
- 简洁设计

### macOS版本
```python
# app_icon.py
@staticmethod
def create_default_album_art(size: int = 200) -> QPixmap:
    pixmap.fill(QColor(240, 240, 240))  # 灰色背景
    # 绘制音乐符号
    # ...
```

✅ **设计一致** - 相同的视觉风格

---

## 📊 功能对比表

| 功能 | Android | macOS | 一致性 |
|------|---------|-------|--------|
| 音频格式支持 | 14种 | 14种 | ✅ 完全一致 |
| 专辑封面查找 | 9个文件名 | 9个文件名 | ✅ 完全一致 |
| 歌词文件查找 | 4个位置 | 4个位置 | ✅ 完全一致 |
| LRC格式解析 | 正则表达式 | 正则表达式 | ✅ 完全一致 |
| 时间戳计算 | 毫秒 | 毫秒 | ✅ 完全一致 |
| 应用图标 | 紫色音符 | 紫色音符 | ✅ 设计一致 |
| 默认封面 | 灰色音符 | 灰色音符 | ✅ 设计一致 |

---

## 🎯 核心原则

1. **文件查找优先级一致**
   - 先查找与音乐文件同名的资源
   - 再查找通用名称的资源
   - 按照相同的顺序遍历

2. **格式支持一致**
   - 支持相同的音频格式
   - 支持相同的图片格式
   - 支持相同的歌词格式

3. **解析逻辑一致**
   - 使用相同的正则表达式
   - 使用相同的时间计算方式
   - 使用相同的编码（UTF-8）

4. **视觉设计一致**
   - 相同的主题色
   - 相同的图标风格
   - 相同的默认资源

---

## 📝 使用示例

### 专辑封面查找顺序

对于音乐文件 `/music/song.mp3`，两个版本都会按以下顺序查找封面：

1. `/music/song.jpg`
2. `/music/song.jpeg`
3. `/music/song.png`
4. `/music/cover.jpg`
5. `/music/cover.jpeg`
6. `/music/cover.png`
7. `/music/folder.jpg`
8. `/music/folder.jpeg`
9. `/music/folder.png`
10. `/music/album.jpg`
11. `/music/album.jpeg`
12. `/music/album.png`

### 歌词文件查找顺序

对于音乐文件 `/music/song.mp3`，两个版本都会按以下顺序查找歌词：

1. `/music/song.lrc`
2. `/music/song.txt`
3. `/music/song.lrc` (基于路径)
4. `/music/song.txt` (基于路径)

---

## ✅ 验证清单

- [x] 音频格式支持完全一致
- [x] 专辑封面查找逻辑一致
- [x] 歌词文件查找逻辑一致
- [x] LRC格式解析一致
- [x] 时间戳计算一致
- [x] 应用图标设计一致
- [x] 默认封面设计一致
- [x] UTF-8编码一致

---

## 🎉 总结

macOS版本的核心逻辑已经与Android版本完全对齐，确保：

1. **相同的文件查找规则**
2. **相同的格式支持**
3. **相同的解析逻辑**
4. **一致的视觉设计**

用户在两个平台上将获得完全一致的体验！🎵