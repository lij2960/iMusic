# 创建Swift原生macOS音乐播放器项目

## 项目概述

使用Swift和SwiftUI创建原生macOS音乐播放器，功能与Python版本完全一致。

## 创建步骤

### 1. 使用Xcode创建项目

1. **打开Xcode**
   - 启动Xcode
   - 选择 "Create a new Xcode project"

2. **选择模板**
   - 平台: macOS
   - 模板: App
   - 点击 Next

3. **配置项目**
   - Product Name: `MusicPlayer`
   - Team: 选择你的开发团队
   - Organization Identifier: `com.ijackey`
   - Bundle Identifier: `com.ijackey.MusicPlayer`
   - Interface: SwiftUI
   - Language: Swift
   - 取消勾选 "Use Core Data"（我们手动添加）
   - 取消勾选 "Include Tests"
   - 点击 Next

4. **选择位置**
   - 选择 `macos-music-player-swift` 目录
   - 点击 Create

### 2. 项目结构

创建以下文件夹结构：

```
MusicPlayer/
├── App/
│   └── MusicPlayerApp.swift
├── Models/
│   ├── Song.swift
│   ├── Lyrics.swift
│   └── PlaybackState.swift
├── ViewModels/
│   └── MusicPlayerViewModel.swift
├── Views/
│   ├── ContentView.swift
│   ├── PlayerView.swift
│   ├── SongListView.swift
│   └── LyricsView.swift
├── Services/
│   ├── AudioPlayer.swift
│   ├── MusicScanner.swift
│   ├── LyricsParser.swift
│   ├── LyricsAPI.swift
│   └── DatabaseManager.swift
└── Resources/
    ├── Assets.xcassets
    └── MusicPlayer.entitlements
```

### 3. 添加权限

在 `MusicPlayer.entitlements` 中添加：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.security.app-sandbox</key>
    <true/>
    <key>com.apple.security.files.user-selected.read-write</key>
    <true/>
    <key>com.apple.security.network.client</key>
    <true/>
</dict>
</plist>
```

### 4. 配置Info.plist

添加以下键值：

```xml
<key>CFBundleName</key>
<string>音乐播放器</string>
<key>CFBundleDisplayName</key>
<string>音乐播放器</string>
<key>LSMinimumSystemVersion</key>
<string>13.0</string>
<key>NSHumanReadableCopyright</key>
<string>Copyright © 2026 iJackey. All rights reserved.</string>
```

### 5. 核心文件说明

#### MusicPlayerApp.swift
应用入口，配置窗口和环境对象。

#### Song.swift
歌曲数据模型，包含：
- 基本信息（标题、艺术家、专辑）
- 文件信息（路径、大小、时长）
- 排序方式枚举
- 播放模式枚举

#### Lyrics.swift
歌词数据模型，包含：
- 歌词行（时间戳+文本）
- LRC格式解析
- 当前歌词索引计算

#### MusicPlayerViewModel.swift
视图模型，管理：
- 歌曲列表
- 播放状态
- 用户交互
- 数据持久化

#### AudioPlayer.swift
音频播放器服务，使用AVFoundation：
- 播放/暂停/停止
- 进度控制
- 音量控制
- 播放模式

#### MusicScanner.swift
音乐文件扫描器：
- 扫描目录
- 提取元数据
- 查找封面
- 支持的格式

#### LyricsParser.swift
歌词解析器：
- LRC格式解析
- 查找歌词文件
- 时间戳解析
- 歌词同步

#### LyricsAPI.swift
在线歌词API：
- 网易云音乐API
- 搜索歌词
- 下载歌词
- 保存本地

#### ContentView.swift
主视图，包含：
- 顶部工具栏
- 歌曲列表
- 播放器控制
- 歌词显示

## 核心代码示例

### AudioPlayer.swift 基础结构

```swift
import AVFoundation
import Combine

class AudioPlayer: ObservableObject {
    @Published var isPlaying = false
    @Published var currentTime: TimeInterval = 0
    @Published var duration: TimeInterval = 0
    
    private var player: AVAudioPlayer?
    private var timer: Timer?
    
    func play(url: URL) {
        do {
            player = try AVAudioPlayer(contentsOf: url)
            player?.prepareToPlay()
            player?.play()
            isPlaying = true
            startTimer()
        } catch {
            print("播放失败: \(error)")
        }
    }
    
    func pause() {
        player?.pause()
        isPlaying = false
    }
    
    func stop() {
        player?.stop()
        isPlaying = false
        currentTime = 0
    }
    
    private func startTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in
            guard let self = self, let player = self.player else { return }
            self.currentTime = player.currentTime
            self.duration = player.duration
        }
    }
}
```

### MusicScanner.swift 基础结构

```swift
import AVFoundation

class MusicScanner {
    static let supportedFormats = ["mp3", "wav", "flac", "aac", "m4a", "ogg"]
    
    static func scanDirectory(at url: URL) -> [Song] {
        var songs: [Song] = []
        
        guard let enumerator = FileManager.default.enumerator(
            at: url,
            includingPropertiesForKeys: [.isRegularFileKey],
            options: [.skipsHiddenFiles]
        ) else { return songs }
        
        for case let fileURL as URL in enumerator {
            if supportedFormats.contains(fileURL.pathExtension.lowercased()) {
                if let song = createSong(from: fileURL) {
                    songs.append(song)
                }
            }
        }
        
        return songs
    }
    
    private static func createSong(from url: URL) -> Song? {
        let asset = AVAsset(url: url)
        
        var title = url.deletingPathExtension().lastPathComponent
        var artist = "Unknown Artist"
        var album = "Unknown Album"
        
        // 提取元数据
        for format in asset.availableMetadataFormats {
            let metadata = asset.metadata(forFormat: format)
            
            for item in metadata {
                if let key = item.commonKey?.rawValue {
                    switch key {
                    case "title":
                        title = item.stringValue ?? title
                    case "artist":
                        artist = item.stringValue ?? artist
                    case "albumName":
                        album = item.stringValue ?? album
                    default:
                        break
                    }
                }
            }
        }
        
        let duration = asset.duration.seconds
        let size = (try? FileManager.default.attributesOfItem(atPath: url.path)[.size] as? Int64) ?? 0
        
        return Song(
            title: title,
            artist: artist,
            album: album,
            duration: duration,
            path: url.path,
            size: size
        )
    }
}
```

## 构建和运行

### 开发模式
```bash
# 在Xcode中按 ⌘R
```

### 发布模式
```bash
# Product > Archive
# 然后导出应用
```

## 优势

### 性能
- 原生Swift性能
- 启动速度快
- 内存占用低
- 响应迅速

### 用户体验
- 原生macOS界面
- 系统集成完美
- 手势支持
- 快捷键支持

### 开发体验
- Xcode强大的IDE
- SwiftUI实时预览
- 类型安全
- 现代语法

### 分发
- 打包简单
- 文件小
- 无需Python环境
- 可以上架App Store

## 下一步

1. 完成所有核心文件的实现
2. 测试所有功能
3. 优化性能和UI
4. 准备发布

## 参考资料

- [Swift官方文档](https://swift.org/documentation/)
- [SwiftUI教程](https://developer.apple.com/tutorials/swiftui)
- [AVFoundation指南](https://developer.apple.com/av-foundation/)
- [Core Data教程](https://developer.apple.com/documentation/coredata)

## 总结

使用Swift和SwiftUI创建原生macOS应用是最佳选择：
- 性能最优
- 体验最好
- 分发最简单
- 维护最容易

开始创建你的Swift项目吧！🚀
