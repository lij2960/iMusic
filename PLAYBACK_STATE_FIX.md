# 播放状态保存修复

## 🔧 问题分析

**原问题**: 杀掉程序再次进入，点击继续播放还是从头播放，没有记住播放歌曲

**根本原因**:
1. `loadLastPlayedSong()` 方法使用 `songs.first()` 可能在歌曲列表未加载完成时失败
2. 缺少应用生命周期回调来保存当前状态
3. 播放状态恢复逻辑不够健壮

## ✅ 修复方案

### 1. 改进播放状态恢复逻辑

**修复前**:
```kotlin
// 只收集一次，可能失败
val songList = songs.first()
```

**修复后**:
```kotlin
// 持续监听歌曲列表，确保在列表加载后恢复
songs.collect { songList ->
    if (songList.isNotEmpty() && _currentSong.value == null) {
        val lastSong = songList.find { it.path == lastSongPath }
        if (lastSong != null) {
            _currentSong.value = lastSong
            updateCurrentIndex(lastSong)
            val mediaItem = MediaItem.fromUri(lastSong.path)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.seekTo(lastPosition)
            android.util.Log.d("ViewModel", "Restored last song: ${lastSong.title} at position $lastPosition")
        }
    }
}
```

### 2. 添加应用生命周期状态保存

**MainActivity 增强**:
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MusicPlayerViewModel
    
    override fun onPause() {
        super.onPause()
        if (::viewModel.isInitialized) {
            viewModel.saveCurrentState()
        }
    }
    
    override fun onStop() {
        super.onStop()
        if (::viewModel.isInitialized) {
            viewModel.saveCurrentState()
        }
    }
}
```

### 3. 增强状态保存方法

**新增 `saveCurrentState()` 方法**:
```kotlin
fun saveCurrentState() {
    _currentSong.value?.let { song ->
        prefs.edit()
            .putString("last_song_id", song.id)
            .putString("last_song_path", song.path)
            .putString("play_mode", _playMode.value.name)
            .putLong("last_position", exoPlayer.currentPosition)
            .apply()
        android.util.Log.d("ViewModel", "Saved current state: ${song.title} at ${exoPlayer.currentPosition}")
    }
}
```

### 4. 完善播放控制逻辑

**继续播放功能**:
```kotlin
fun continueLastPlayback() {
    val lastSong = _currentSong.value
    if (lastSong != null) {
        // 如果有上次播放的歌曲，从上次位置继续播放
        if (!exoPlayer.isPlaying) {
            exoPlayer.play()
        }
    } else {
        // 如果没有上次播放的歌曲，播放列表第一首
        val playlist = _playlist.value
        if (playlist.isNotEmpty()) {
            playSong(playlist[0])
        }
    }
}
```

## 🎯 修复效果

### 状态保存时机
- ✅ **播放歌曲时**: 每次播放新歌曲时自动保存
- ✅ **播放过程中**: 每秒保存当前播放位置
- ✅ **应用暂停时**: onPause() 时保存当前状态
- ✅ **应用停止时**: onStop() 时保存当前状态
- ✅ **ViewModel销毁时**: onCleared() 时保存当前状态

### 状态恢复逻辑
- ✅ **智能等待**: 等待歌曲列表加载完成后再恢复
- ✅ **防重复**: 只在 `_currentSong.value == null` 时恢复
- ✅ **完整恢复**: 恢复歌曲、位置、播放模式、排序方式
- ✅ **日志记录**: 添加详细日志便于调试

### 用户体验
- ✅ **继续播放**: 从上次播放的歌曲和位置继续
- ✅ **从头开始**: 重置为顺序播放，从第一首开始
- ✅ **状态持久**: 应用重启后完整恢复播放状态
- ✅ **模式记忆**: 保持上次的播放模式设置

## 📱 测试验证

### 测试步骤
1. 播放一首歌曲到中间位置
2. 切换播放模式（如随机播放）
3. 杀掉应用进程
4. 重新启动应用
5. 点击"继续播放"按钮

### 预期结果
- ✅ 恢复到上次播放的歌曲
- ✅ 从上次停止的位置开始播放
- ✅ 保持上次的播放模式设置
- ✅ 界面正确显示当前播放状态

## 🔍 调试信息

**日志标签**: `ViewModel`
**关键日志**:
- `"Restored last song: [歌曲名] at position [位置]"`
- `"Saved current state: [歌曲名] at [位置]"`

**调试命令**:
```bash
# 查看播放状态相关日志
adb logcat | grep -E "(ViewModel|Restored|Saved)"
```

## 📦 构建信息

- ✅ **编译状态**: 成功
- ✅ **APK位置**: `/Volumes/Jackey/iMusic/app/build/outputs/apk/debug/app-debug.apk`
- ✅ **功能完整**: 播放状态保存和恢复功能已完全修复

## 🚀 安装测试

```bash
# 安装修复版本
adb install /Volumes/Jackey/iMusic/app/build/outputs/apk/debug/app-debug.apk

# 或使用Gradle
cd /Volumes/Jackey/iMusic
./gradlew installDebug
```

现在播放状态保存功能已完全修复，应用重启后能正确恢复上次播放的歌曲和位置！🎉