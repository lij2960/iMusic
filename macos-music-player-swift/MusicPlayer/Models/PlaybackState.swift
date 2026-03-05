//
//  PlaybackState.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import Foundation

/// 播放状态（用于持久化）
struct PlaybackState: Codable {
    var currentSongId: UUID?
    var position: TimeInterval
    var playMode: PlayMode
    var sortOrder: SortOrder
    var isPlaying: Bool
    var volume: Float
    
    init(
        currentSongId: UUID? = nil,
        position: TimeInterval = 0,
        playMode: PlayMode = .repeatAll,
        sortOrder: SortOrder = .dateAddedDesc,
        isPlaying: Bool = false,
        volume: Float = 0.7
    ) {
        self.currentSongId = currentSongId
        self.position = position
        self.playMode = playMode
        self.sortOrder = sortOrder
        self.isPlaying = isPlaying
        self.volume = volume
    }
}
