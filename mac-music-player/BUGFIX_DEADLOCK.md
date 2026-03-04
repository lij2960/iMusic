# 🐛 修复：切歌时卡死问题

## 问题描述

在列表顺序播放时，切换歌曲会导致软件卡死。

## 原因分析

### 问题根源

VLC的事件回调机制导致的死锁：

```python
# 原来的代码（有问题）
events = self.player.event_manager()
events.event_attach(vlc.EventType.MediaPlayerEndReached, self._on_end_reached)

def _on_end_reached(self, event):
    # 在VLC的事件线程中调用
    self.next()  # 这会导致死锁
```

**死锁原因**：
1. VLC在自己的线程中触发 `MediaPlayerEndReached` 事件
2. 事件回调函数 `_on_end_reached` 在VLC线程中执行
3. 回调中调用 `self.next()` 尝试切换歌曲
4. `next()` 中调用 `play_song()` 需要操作VLC播放器
5. VLC播放器此时正在等待事件回调完成
6. 形成死锁：VLC等待回调完成，回调等待VLC响应

### 死锁示意图

```
VLC线程                     主线程
   |                          |
   |-- 触发EndReached事件 -->  |
   |                          |
   |-- 调用_on_end_reached --> |
   |                          |
   |   等待回调完成...         |
   |                          |
   |                      调用next()
   |                          |
   |                      调用play_song()
   |                          |
   |                      需要VLC响应
   |                          |
   |   (死锁！)            (等待VLC...)
```

## 解决方案

### 方案1：移除事件回调，使用轮询

**优点**：
- 简单可靠
- 避免线程问题
- 不会死锁

**实现**：

```python
# 不再使用VLC事件回调
# events.event_attach(vlc.EventType.MediaPlayerEndReached, self._on_end_reached)

# 在定时器中轮询检测播放状态
def update_progress(self):
    if self.player.is_playing and self.player.current_song:
        # ... 更新进度 ...
        
        # 检查是否播放结束
        if self.player.is_at_end():
            self.player.next()

def is_at_end(self) -> bool:
    """检查是否播放到结尾"""
    state = self.player.get_state()
    return state == vlc.State.Ended
```

### 方案2：添加切歌锁

防止在切歌过程中重复触发：

```python
def __init__(self):
    # ...
    self._is_switching: bool = False  # 切歌锁

def play_song(self, song: Song):
    if self._is_switching:
        return  # 如果正在切歌，忽略新的播放请求
    
    try:
        self._is_switching = True
        
        # 停止当前播放
        self.player.stop()
        
        # 加载新媒体
        media = self.instance.media_new(song.path)
        self.player.set_media(media)
        
        # 开始播放
        self.player.play()
        
    finally:
        self._is_switching = False
```

## 修改的文件

### 1. `src/player/music_player.py`

**修改内容**：
- ✅ 移除VLC事件回调
- ✅ 添加 `_is_switching` 切歌锁
- ✅ 添加 `is_at_end()` 方法检测播放结束
- ✅ 改进 `play_song()` 方法，先停止再播放
- ✅ 在 `next()` 和 `previous()` 中检查切歌锁

### 2. `src/ui/main_window.py`

**修改内容**：
- ✅ 在 `update_progress()` 中轮询检测播放结束
- ✅ 播放结束时自动调用 `next()`

## 测试验证

### 测试场景

1. **列表循环播放**
   - ✅ 播放到结尾自动切换下一首
   - ✅ 不会卡死
   - ✅ 切换流畅

2. **手动切歌**
   - ✅ 点击"下一首"按钮
   - ✅ 点击"上一首"按钮
   - ✅ 双击歌曲列表

3. **快速切歌**
   - ✅ 连续点击"下一首"
   - ✅ 不会卡死
   - ✅ 响应正常

4. **单曲循环**
   - ✅ 播放结束自动重播
   - ✅ 不会卡死

5. **随机播放**
   - ✅ 随机切换歌曲
   - ✅ 不会卡死

## 性能影响

### 轮询开销

- **轮询频率**: 1秒1次（与进度更新同步）
- **CPU开销**: 极低（只是检查状态）
- **用户体验**: 无影响（1秒延迟可接受）

### 对比

| 方案 | 响应速度 | CPU开销 | 稳定性 |
|------|---------|---------|--------|
| 事件回调 | 即时 | 低 | ❌ 会死锁 |
| 轮询检测 | ~1秒 | 极低 | ✅ 稳定 |

## 其他改进

### 1. 播放器状态管理

```python
def play_song(self, song: Song):
    # 先停止当前播放
    self.player.stop()
    
    # 加载新媒体
    media = self.instance.media_new(song.path)
    self.player.set_media(media)
    
    # 开始播放
    self.player.play()
```

**好处**：
- 确保旧媒体完全停止
- 避免资源泄漏
- 切换更可靠

### 2. 错误处理

```python
def play_song(self, song: Song):
    try:
        # ... 播放逻辑 ...
    except Exception as e:
        print(f"Error playing song: {e}")
        self.is_playing = False
    finally:
        self._is_switching = False
```

**好处**：
- 即使出错也能解锁
- 不会永久卡死
- 更健壮

## 使用建议

### 如果仍然遇到问题

1. **检查VLC版本**
   ```bash
   # 确保VLC已正确安装
   brew list vlc
   ```

2. **查看日志**
   ```bash
   # 运行程序并查看错误输出
   python main.py
   ```

3. **重启程序**
   - 关闭程序
   - 重新运行

4. **清理数据库**
   ```bash
   # 如果问题持续，删除数据库重新扫描
   rm music_player.db
   ```

## 总结

### 修复前
- ❌ 切歌时会卡死
- ❌ VLC事件回调导致死锁
- ❌ 用户体验差

### 修复后
- ✅ 切歌流畅不卡死
- ✅ 使用轮询避免死锁
- ✅ 添加切歌锁防止重复触发
- ✅ 用户体验良好

---

**修复状态**: ✅ 已完成
**测试状态**: ✅ 已验证
**稳定性**: ✅ 优秀

现在可以放心使用列表循环播放功能了！🎵
