//
//  MusicPlayerViewModel.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import Foundation
import Combine

/// 音乐播放器视图模型
@MainActor
class MusicPlayerViewModel: ObservableObject {
    // MARK: - Published Properties
    @Published var songs: [Song] = []
    @Published var currentSong: Song?
    @Published var currentLyrics: Lyrics?
    @Published var currentLyricIndex: Int = -1
    @Published var sortOrder: SortOrder = .dateAddedDesc
    @Published var playMode: PlayMode = .repeatAll
    @Published var isScanning: Bool = false
    @Published var isPlaying: Bool = false
    @Published var currentPosition: Double = 0
    @Published var currentDuration: Double = 0
    @Published var volume: Double = 0.7
    
    // MARK: - Services
    let audioPlayer = AudioPlayer()
    private var cancellables = Set<AnyCancellable>()
    
    // MARK: - Initialization
    init() {
        setupBindings()
        loadPlaybackState()
    }
    
    // MARK: - Setup
    private func setupBindings() {
        // 监听播放结束
        audioPlayer.onSongEnded = { [weak self] in
            Task { @MainActor in
                self?.next()
            }
        }
        
        // 监听播放状态
        audioPlayer.$isPlaying
            .assign(to: &$isPlaying)
        
        // 监听播放进度
        audioPlayer.$currentTime
            .sink { [weak self] currentTime in
                self?.currentPosition = currentTime
                self?.updateCurrentLyric(currentTime: currentTime)
            }
            .store(in: &cancellables)
        
        // 监听歌曲时长
        audioPlayer.$duration
            .assign(to: &$currentDuration)
    }
    
    // MARK: - Song Management
    
    /// 添加音乐文件夹
    func addMusicFolder(url: URL) async {
        isScanning = true
        let newSongs = await MusicScanner.scanDirectory(at: url)
        songs.append(contentsOf: newSongs)
        sortSongs()
        isScanning = false
    }
    
    /// 排序歌曲
    func sortSongs() {
        songs = sortOrder.sort(songs)
    }
    
    /// 更改排序方式
    func changeSortOrder(_ order: SortOrder) {
        sortOrder = order
        sortSongs()
    }
    
    // MARK: - Playback Control
    
    /// 播放歌曲
    func playSong(_ song: Song) {
        currentSong = song
        let url = URL(fileURLWithPath: song.path)
        audioPlayer.play(url: url)
        loadLyrics(for: song)
        savePlaybackState()
    }
    
    /// 播放/暂停
    func playPause() {
        if audioPlayer.isPlaying {
            audioPlayer.pause()
        } else if currentSong != nil {
            audioPlayer.resume()
        } else if let firstSong = songs.first {
            playSong(firstSong)
        }
    }
    
    /// 上一首
    func previous() {
        guard let current = currentSong,
              let currentIndex = songs.firstIndex(where: { $0.id == current.id }) else {
            return
        }
        
        let previousIndex = currentIndex > 0 ? currentIndex - 1 : songs.count - 1
        playSong(songs[previousIndex])
    }
    
    /// 下一首
    func next() {
        guard let current = currentSong else {
            if let firstSong = songs.first {
                playSong(firstSong)
            }
            return
        }
        
        switch playMode {
        case .repeatOne:
            playSong(current)
            
        case .repeatAll:
            guard let currentIndex = songs.firstIndex(where: { $0.id == current.id }) else { return }
            let nextIndex = (currentIndex + 1) % songs.count
            playSong(songs[nextIndex])
            
        case .shuffle:
            if let randomSong = songs.randomElement() {
                playSong(randomSong)
            }
        }
    }
    
    /// 跳转到指定位置
    func seek(to time: TimeInterval) {
        audioPlayer.seek(to: time)
    }
    
    /// 搜索在线歌词（带 UI 提示）
    func searchOnlineLyricsAction() {
        guard let song = currentSong else {
            print("❌ 没有正在播放的歌曲")
            return
        }
        
        print("🎵 开始搜索歌词: \(song.title) - \(song.artist)")
        
        Task {
            let lyrics = await searchOnlineLyrics()
            
            await MainActor.run {
                if let lyrics = lyrics {
                    print("✅ 歌词搜索成功，准备保存")
                    if saveLyrics(lyrics) {
                        print("✅ 歌词保存成功")
                    } else {
                        print("❌ 歌词保存失败")
                    }
                } else {
                    print("❌ 未找到歌词")
                }
            }
        }
    }
    
    /// 更改播放模式
    func changePlayMode(_ mode: PlayMode) {
        playMode = mode
        savePlaybackState()
    }
    
    // MARK: - Lyrics
    
    /// 加载歌词
    private func loadLyrics(for song: Song) {
        if let lyricsURL = LyricsParser.findLyricsFile(for: song.path) {
            currentLyrics = LyricsParser.parseLyricsFile(at: lyricsURL, songId: song.id)
        } else {
            currentLyrics = nil
        }
        currentLyricIndex = -1
    }
    
    /// 更新当前歌词
    private func updateCurrentLyric(currentTime: TimeInterval) {
        guard let lyrics = currentLyrics else { return }
        let currentTimeMs = Int(currentTime * 1000)
        currentLyricIndex = lyrics.getCurrentLyricIndex(currentTimeMs: currentTimeMs)
    }
    
    /// 搜索在线歌词
    func searchOnlineLyrics() async -> String? {
        guard let song = currentSong else { return nil }
        return await LyricsAPI.searchOnlineLyrics(title: song.title, artist: song.artist)
    }
    
    /// 保存歌词
    func saveLyrics(_ content: String) -> Bool {
        guard let song = currentSong else { return false }
        let success = LyricsParser.saveLyrics(content, for: song.path)
        if success {
            loadLyrics(for: song)
        }
        return success
    }
    
    // MARK: - Persistence
    
    /// 保存播放状态
    private func savePlaybackState() {
        let state = PlaybackState(
            currentSongId: currentSong?.id,
            position: audioPlayer.currentTime,
            playMode: playMode,
            sortOrder: sortOrder,
            isPlaying: audioPlayer.isPlaying,
            volume: audioPlayer.volume
        )
        
        if let encoded = try? JSONEncoder().encode(state) {
            UserDefaults.standard.set(encoded, forKey: "PlaybackState")
        }
    }
    
    /// 加载播放状态
    private func loadPlaybackState() {
        guard let data = UserDefaults.standard.data(forKey: "PlaybackState"),
              let state = try? JSONDecoder().decode(PlaybackState.self, from: data) else {
            return
        }
        
        sortOrder = state.sortOrder
        playMode = state.playMode
        audioPlayer.setVolume(state.volume)
        
        if let songId = state.currentSongId,
           let song = songs.first(where: { $0.id == songId }) {
            playSong(song)
            audioPlayer.seek(to: state.position)
            if !state.isPlaying {
                audioPlayer.pause()
            }
        }
    }
}
