# 🔍 通知栏功能调试指南

## 当前状态
已添加详细日志来诊断通知栏问题。

## 安装调试版本
```bash
adb install /Volumes/Jackey/iMusic/app/build/outputs/apk/debug/app-debug.apk
```

## 查看日志
安装后，在播放音乐时运行以下命令查看日志：

```bash
# 查看所有MusicService相关日志
adb logcat | grep MusicService

# 或者更详细的日志
adb logcat | grep -E "(MusicService|ViewModel)"
```

## 关键日志说明

### 1. 服务启动日志
```
MusicService: onCreate called
MusicService: createNotification - title: [歌曲名], artist: [艺术家]
```
- 如果title显示"iMusic"，说明MediaMetadata未正确设置
- 如果artist显示"准备播放"，说明还没有加载歌曲信息

### 2. 播放状态变化日志
```
MusicService: onIsPlayingChanged: true/false
MusicService: onMediaMetadataChanged: [歌曲名]
MusicService: onMediaItemTransition: [歌曲名]
```
- 这些日志应该在播放、暂停、切歌时出现
- 如果没有出现，说明监听器未正确工作

### 3. 按钮点击日志
```
MusicService: onStartCommand - action: NEXT
MusicService: Next action - hasNext: true
```
- 点击通知栏按钮时应该出现
- 如果没有出现，说明PendingIntent未正确触发

### 4. 封面加载日志
```
MusicService: Loading album art from: [路径]
```
- 显示封面文件路径
- 如果出现错误，会显示Error loading album art

## 可能的问题和解决方案

### 问题1：歌名不显示
**症状**: 通知栏显示"iMusic"而不是歌曲名

**可能原因**:
1. MediaMetadata未正确设置到MediaItem
2. 通知创建时机太早，歌曲还未加载

**检查日志**:
```bash
adb logcat | grep "createNotification - title"
```
如果显示"iMusic"，说明metadata为空

**解决方案**: 
- 确认ViewModel中playSong方法正确设置了MediaMetadata
- 确认setMediaItems被调用

### 问题2：按钮无效
**症状**: 点击上一曲/下一曲没反应

**可能原因**:
1. PendingIntent未正确创建
2. Service未收到Intent
3. ExoPlayer播放列表未正确加载

**检查日志**:
```bash
adb logcat | grep "onStartCommand"
```
点击按钮时应该看到action日志

**解决方案**:
- 检查是否有"onStartCommand - action: NEXT/PREVIOUS"
- 检查"hasNext"和"hasPrevious"的值
- 如果都是false，说明播放列表未加载

### 问题3：通知不更新
**症状**: 切歌后通知内容不变

**可能原因**:
1. 监听器未触发
2. updateNotification未被调用

**检查日志**:
```bash
adb logcat | grep "onMediaItemTransition"
```
切歌时应该看到这个日志

## 测试步骤

### 步骤1：安装并启动
```bash
adb install /Volumes/Jackey/iMusic/app/build/outputs/apk/debug/app-debug.apk
adb logcat -c  # 清空日志
adb logcat | grep MusicService
```

### 步骤2：播放音乐
1. 打开应用
2. 选择一首歌播放
3. 观察日志输出

**预期日志**:
```
MusicService: onIsPlayingChanged: true
MusicService: onMediaMetadataChanged: [歌曲名]
MusicService: createNotification - title: [歌曲名], artist: [艺术家]
```

### 步骤3：测试通知栏
1. 下拉通知栏
2. 检查是否显示歌曲信息
3. 点击"下一曲"按钮

**预期日志**:
```
MusicService: onStartCommand - action: NEXT
MusicService: Next action - hasNext: true
MusicService: onMediaItemTransition: [新歌曲名]
MusicService: createNotification - title: [新歌曲名], artist: [新艺术家]
```

### 步骤4：测试上一曲
1. 点击"上一曲"按钮

**预期日志**:
```
MusicService: onStartCommand - action: PREVIOUS
MusicService: Previous action - hasPrevious: true
MusicService: onMediaItemTransition: [上一首歌名]
```

## 收集完整日志

如果问题仍然存在，请收集完整日志：

```bash
# 清空日志
adb logcat -c

# 开始记录
adb logcat > imusic_debug.log

# 然后进行以下操作：
# 1. 打开应用
# 2. 播放一首歌
# 3. 下拉通知栏
# 4. 点击"下一曲"
# 5. 点击"上一曲"
# 6. 点击"播放/暂停"

# 停止记录（Ctrl+C）
# 查看日志文件
cat imusic_debug.log | grep -E "(MusicService|ViewModel|ExoPlayer)"
```

## 下一步
根据日志输出，我们可以确定：
1. MediaMetadata是否正确设置
2. 播放列表是否正确加载
3. 按钮点击是否正确处理
4. 通知是否正确更新

请运行上述测试并提供日志输出，这样我可以准确定位问题所在。