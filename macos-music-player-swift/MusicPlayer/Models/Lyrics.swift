//
//  Lyrics.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import Foundation

/// 歌词行
struct LyricLine: Identifiable, Codable, Hashable {
    let id: UUID
    var timeMs: Int
    var text: String
    
    init(id: UUID = UUID(), timeMs: Int, text: String) {
        self.id = id
        self.timeMs = timeMs
        self.text = text
    }
    
    /// 获取时间字符串
    var timeString: String {
        let minutes = timeMs / 60000
        let seconds = (timeMs % 60000) / 1000
        return String(format: "%02d:%02d", minutes, seconds)
    }
}

/// 歌词
struct Lyrics: Codable {
    var songId: UUID
    var lines: [LyricLine]
    
    init(songId: UUID, lines: [LyricLine]) {
        self.songId = songId
        self.lines = lines.sorted { $0.timeMs < $1.timeMs }
    }
    
    /// 获取当前歌词索引
    func getCurrentLyricIndex(currentTimeMs: Int) -> Int {
        var currentIndex = -1
        
        for (index, line) in lines.enumerated() {
            if line.timeMs <= currentTimeMs {
                currentIndex = index
            } else {
                break
            }
        }
        
        return currentIndex
    }
}
