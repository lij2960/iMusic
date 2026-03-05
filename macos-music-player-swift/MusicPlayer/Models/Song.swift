//
//  Song.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import Foundation

/// 歌曲模型（与Android/Python版本保持一致）
struct Song: Identifiable, Codable, Hashable {
    let id: UUID
    var title: String
    var artist: String
    var album: String
    var duration: TimeInterval
    var path: String
    var albumArt: String?
    var dateAdded: Date
    var size: Int64
    
    init(
        id: UUID = UUID(),
        title: String,
        artist: String = "Unknown Artist",
        album: String = "Unknown Album",
        duration: TimeInterval = 0,
        path: String,
        albumArt: String? = nil,
        dateAdded: Date = Date(),
        size: Int64 = 0
    ) {
        self.id = id
        self.title = title
        self.artist = artist
        self.album = album
        self.duration = duration
        self.path = path
        self.albumArt = albumArt
        self.dateAdded = dateAdded
        self.size = size
    }
    
    /// 获取显示名称
    var displayName: String {
        "\(title) - \(artist)"
    }
    
    /// 获取时长字符串
    var durationString: String {
        let minutes = Int(duration) / 60
        let seconds = Int(duration) % 60
        return String(format: "%d:%02d", minutes, seconds)
    }
}

/// 排序方式（与Android/Python版本保持一致）
enum SortOrder: String, CaseIterable, Codable {
    case dateAddedDesc = "日期添加(最新)"
    case dateAddedAsc = "日期添加(最旧)"
    case titleAsc = "标题(A-Z)"
    case titleDesc = "标题(Z-A)"
    case artistAsc = "艺术家(A-Z)"
    case artistDesc = "艺术家(Z-A)"
    case durationAsc = "时长(短)"
    case durationDesc = "时长(长)"
    
    /// 排序函数
    func sort(_ songs: [Song]) -> [Song] {
        switch self {
        case .dateAddedDesc:
            return songs.sorted { $0.dateAdded > $1.dateAdded }
        case .dateAddedAsc:
            return songs.sorted { $0.dateAdded < $1.dateAdded }
        case .titleAsc:
            return songs.sorted { $0.title < $1.title }
        case .titleDesc:
            return songs.sorted { $0.title > $1.title }
        case .artistAsc:
            return songs.sorted { $0.artist < $1.artist }
        case .artistDesc:
            return songs.sorted { $0.artist > $1.artist }
        case .durationAsc:
            return songs.sorted { $0.duration < $1.duration }
        case .durationDesc:
            return songs.sorted { $0.duration > $1.duration }
        }
    }
}

/// 播放模式（与Android/Python版本保持一致）
enum PlayMode: String, CaseIterable, Codable {
    case repeatAll = "列表循环"
    case repeatOne = "单曲循环"
    case shuffle = "随机播放"
    
    var icon: String {
        switch self {
        case .repeatAll: return "repeat"
        case .repeatOne: return "repeat.1"
        case .shuffle: return "shuffle"
        }
    }
}
