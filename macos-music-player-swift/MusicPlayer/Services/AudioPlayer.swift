//
//  AudioPlayer.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import AVFoundation
import Combine

/// 音频播放器服务
class AudioPlayer: NSObject, ObservableObject {
    @Published var isPlaying = false
    @Published var currentTime: TimeInterval = 0
    @Published var duration: TimeInterval = 0
    @Published var volume: Float = 0.7
    
    private var player: AVAudioPlayer?
    private var timer: Timer?
    var onSongEnded: (() -> Void)?
    
    override init() {
        super.init()
    }
    
    /// 播放歌曲
    func play(url: URL) {
        stop()
        
        do {
            player = try AVAudioPlayer(contentsOf: url)
            player?.delegate = self
            player?.volume = volume
            player?.prepareToPlay()
            player?.play()
            
            isPlaying = true
            duration = player?.duration ?? 0
            startTimer()
        } catch {
            print("播放失败: \(error.localizedDescription)")
        }
    }
    
    /// 暂停
    func pause() {
        player?.pause()
        isPlaying = false
        stopTimer()
    }
    
    /// 继续播放
    func resume() {
        player?.play()
        isPlaying = true
        startTimer()
    }
    
    /// 停止
    func stop() {
        player?.stop()
        player = nil
        isPlaying = false
        currentTime = 0
        stopTimer()
    }
    
    /// 跳转到指定位置
    func seek(to time: TimeInterval) {
        player?.currentTime = time
        currentTime = time
    }
    
    /// 设置音量
    func setVolume(_ value: Float) {
        volume = value
        player?.volume = value
    }
    
    /// 开始定时器
    private func startTimer() {
        stopTimer()
        timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in
            guard let self = self, let player = self.player else { return }
            self.currentTime = player.currentTime
        }
    }
    
    /// 停止定时器
    private func stopTimer() {
        timer?.invalidate()
        timer = nil
    }
}

// MARK: - AVAudioPlayerDelegate
extension AudioPlayer: AVAudioPlayerDelegate {
    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        isPlaying = false
        stopTimer()
        onSongEnded?()
    }
}
