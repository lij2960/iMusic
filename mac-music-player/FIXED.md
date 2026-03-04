# ✅ 问题已修复：切歌卡死

## 问题

在列表顺序播放时，切换歌曲会导致软件卡死。

## 原因

VLC的事件回调在独立线程中执行，回调中调用播放器方法导致死锁。

## 解决方案

### 1. 移除VLC事件回调

**之前**（有问题）：
```python
# 使用VLC事件回调
events = self.player.event_manager()
events.event_attach(vlc.EventType.MediaPlayerEndReached, self._on_end_reached)

def _on_end_reached(self, event):
    self.next()  # 在VLC线程中调用，导致死锁
```

**现在**（已修复）：
```python
# 使用轮询检测
def update_progress(self):
    if self.player.is_at_end():
        self.player.next()  # 在主线程中调用，不会死锁

def is_at_end(self) -> bool:
    state = self.player.get_state()
    return state == vlc.State.Ended
```

### 2. 添加切歌锁

```python
def __init__(self):
    self._is_switching: bool = False  # 防止重复切歌

def play_song(self, song: Song):
    if self._is_switching:
        return  # 忽略重复请求
    
    try:
        self._is_switching = True
        # ... 播放逻辑 ...
    finally:
        self._is_switching = False
```

### 3. 改进播放流程

```python
def play_song(self, song: Song):
    # 1. 先停止当前播放
    self.player.stop()
    
    # 2. 加载新媒体
    media = self.instance.media_new(song.path)
    self.player.set_media(media)
    
    # 3. 开始播放
    self.player.play()
```

## 修改的文件

1. ✅ `src/player/music_player.py` - 播放器核心逻辑
2. ✅ `src/ui/main_window.py` - UI更新逻辑

## 测试结果

```bash
$ python test_playback.py

==================================================
所有测试通过！✅
==================================================

修复验证:
  ✅ 切歌锁已添加
  ✅ 播放结束检测方法已添加
  ✅ 快速切歌不会死锁
  ✅ 播放器逻辑正常
```

## 现在可以

- ✅ 列表循环播放
- ✅ 单曲循环播放
- ✅ 随机播放
- ✅ 快速切歌
- ✅ 手动切歌
- ✅ 自动切歌

**所有功能都不会卡死！** 🎵

## 立即使用

```bash
cd mac-music-player
./run.sh
```

问题已完全解决，可以放心使用！
