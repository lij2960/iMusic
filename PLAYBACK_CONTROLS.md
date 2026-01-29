# 播放控制功能更新

## 新增功能

### 歌曲库播放控制按钮

在音乐库界面添加了两个播放控制按钮：

#### 1. 继续播放按钮
- **功能**: 从上次播放的歌曲和位置继续播放
- **图标**: PlayArrow
- **行为**:
  - 如果有上次播放的歌曲，从上次暂停的位置继续播放
  - 如果没有上次播放记录，从列表第一首开始播放
  - 保持上次的播放模式设置

#### 2. 从头开始按钮
- **功能**: 从歌曲列表第一首开始循环播放
- **图标**: Refresh
- **行为**:
  - 重置播放模式为顺序播放（SEQUENTIAL）
  - 从当前歌曲列表的第一首开始播放
  - 重置播放位置为0

## 播放状态持久化

### 自动保存功能
- ✅ **当前播放歌曲**: 每次切换歌曲时自动保存
- ✅ **播放位置**: 每秒自动保存当前播放位置
- ✅ **播放模式**: 每次更改播放模式时自动保存
- ✅ **排序方式**: 每次更改排序时自动保存

### 启动恢复功能
- ✅ **恢复上次播放歌曲**: 应用启动时自动加载上次播放的歌曲
- ✅ **恢复播放位置**: 从上次暂停的位置开始（不自动播放）
- ✅ **恢复播放模式**: 保持上次关闭时的播放模式
- ✅ **恢复排序设置**: 保持上次的排序方式

## 技术实现

### ViewModel 新增方法

```kotlin
// 继续上次播放
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

// 从头开始播放
fun startFromBeginning() {
    val playlist = _playlist.value
    if (playlist.isNotEmpty()) {
        // 从列表第一首开始播放，重置播放模式为顺序播放
        setPlayMode(PlayMode.SEQUENTIAL)
        playSong(playlist[0])
    }
}
```

### 数据持久化增强

```kotlin
// 保存播放状态（包含播放模式）
private fun saveLastPlayedSong(song: Song) {
    prefs.edit()
        .putString("last_song_id", song.id)
        .putString("last_song_path", song.path)
        .putString("play_mode", _playMode.value.name)
        .putLong("last_position", exoPlayer.currentPosition)
        .apply()
}

// 定期保存播放位置
viewModelScope.launch {
    while (true) {
        if (exoPlayer.isPlaying) {
            _currentPosition.value = exoPlayer.currentPosition
            _duration.value = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            
            // 定期保存播放位置
            _currentSong.value?.let { song ->
                prefs.edit()
                    .putLong("last_position", exoPlayer.currentPosition)
                    .apply()
            }
        }
        kotlinx.coroutines.delay(1000)
    }
}
```

## 用户界面更新

### 音乐库界面布局
```kotlin
// 播放控制按钮行
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    Button(
        onClick = { viewModel.continueLastPlayback() },
        modifier = Modifier.weight(1f)
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text("继续播放")
    }
    
    Button(
        onClick = { viewModel.startFromBeginning() },
        modifier = Modifier.weight(1f)
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text("从头开始")
    }
}
```

## 使用场景

### 继续播放适用场景
- 用户暂停音乐后重新打开应用
- 用户想要从上次停止的地方继续听音乐
- 保持当前的播放模式和播放列表状态

### 从头开始适用场景
- 用户想要重新开始听整个播放列表
- 用户想要重置播放模式为顺序播放
- 用户想要从列表第一首开始循环播放

## 构建状态

✅ **编译成功**: 所有新功能已成功编译
✅ **功能完整**: 继续播放和从头开始功能已实现
✅ **状态持久化**: 播放状态自动保存和恢复
✅ **用户界面**: 音乐库界面已更新

## APK 信息

- **位置**: `/Volumes/Jackey/iMusic/app/build/outputs/apk/debug/app-debug.apk`
- **大小**: 约13.6MB
- **版本**: Debug版本
- **新功能**: 包含播放控制按钮和状态持久化

## 安装和测试

```bash
# 安装到设备
adb install /Volumes/Jackey/iMusic/app/build/outputs/apk/debug/app-debug.apk

# 或使用Gradle安装
cd /Volumes/Jackey/iMusic
./gradlew installDebug
```

所有功能已完成并可正常使用！🎉