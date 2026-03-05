//
//  PlayerView.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import SwiftUI

struct PlayerView: View {
    @EnvironmentObject var viewModel: MusicPlayerViewModel
    @State private var isDraggingSlider = false
    @State private var sliderValue: Double = 0
    
    var body: some View {
        VStack(spacing: 16) {
            // 专辑封面和控制按钮 - 左右布局
            HStack(spacing: 20) {
                // 左侧：专辑封面
                albumArtView
                
                // 右侧：控制区域
                VStack(spacing: 12) {
                    // 歌曲信息
                    songInfoView
                    
                    // 播放控制按钮
                    controlButtonsView
                    
                    // 搜索歌词按钮
                    Button(action: { viewModel.searchOnlineLyricsAction() }) {
                        Label("搜索歌词", systemImage: "magnifyingglass")
                    }
                    .disabled(viewModel.currentSong == nil)
                }
                .frame(maxWidth: .infinity)
            }
            
            // 进度条
            progressView
            
            // 音量控制
            volumeView
        }
        .padding()
        .frame(maxWidth: .infinity)
    }
    
    // MARK: - Album Art
    private var albumArtView: some View {
        Group {
            if let song = viewModel.currentSong {
                if let albumArtPath = song.albumArt,
                   let nsImage = NSImage(contentsOfFile: albumArtPath) {
                    // 有封面，显示歌曲封面
                    Image(nsImage: nsImage)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 120, height: 120)
                        .cornerRadius(8)
                } else {
                    // 没有封面，显示应用图标
                    appIconView
                }
            } else {
                // 没有歌曲，显示应用图标
                appIconView
            }
        }
    }
    
    private var appIconView: some View {
        Group {
            // 尝试加载 Android 版本的应用图标
            if let resourcePath = Bundle.main.resourcePath,
               let iconImage = NSImage(contentsOfFile: resourcePath + "/ic_launcher.png") {
                Image(nsImage: iconImage)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 120, height: 120)
                    .cornerRadius(8)
            } else {
                // 降级方案：使用系统图标
                Rectangle()
                    .fill(Color.purple.opacity(0.2))
                    .frame(width: 120, height: 120)
                    .cornerRadius(8)
                    .overlay(
                        Image(systemName: "music.note")
                            .font(.system(size: 50))
                            .foregroundColor(.purple)
                    )
            }
        }
    }
    
    // MARK: - Song Info
    private var songInfoView: some View {
        VStack(spacing: 4) {
            Text(viewModel.currentSong?.title ?? "未播放")
                .font(.headline)
                .fontWeight(.bold)
                .lineLimit(1)
            
            Text(viewModel.currentSong != nil ? 
                 "\(viewModel.currentSong!.artist) - \(viewModel.currentSong!.album)" : "")
                .font(.caption)
                .foregroundColor(.secondary)
                .lineLimit(1)
        }
    }
    
    // MARK: - Progress
    private var progressView: some View {
        VStack(spacing: 4) {
            Slider(
                value: isDraggingSlider ? $sliderValue : $viewModel.currentPosition,
                in: 0...max(viewModel.currentDuration, 1),
                onEditingChanged: { editing in
                    isDraggingSlider = editing
                    if !editing {
                        viewModel.seek(to: sliderValue)
                    }
                }
            )
            
            HStack {
                Text(formatTime(isDraggingSlider ? sliderValue : viewModel.currentPosition))
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                Spacer()
                
                Text(formatTime(viewModel.currentDuration))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
    }
    
    // MARK: - Control Buttons
    private var controlButtonsView: some View {
        HStack(spacing: 16) {
            // 上一首
            Button(action: { viewModel.previous() }) {
                Image(systemName: "backward.fill")
                    .font(.title2)
            }
            .disabled(viewModel.currentSong == nil)
            
            // 播放/暂停
            Button(action: { viewModel.playPause() }) {
                Image(systemName: viewModel.isPlaying ? "pause.circle.fill" : "play.circle.fill")
                    .font(.system(size: 44))
            }
            .disabled(viewModel.currentSong == nil && viewModel.songs.isEmpty)
            
            // 下一首
            Button(action: { viewModel.next() }) {
                Image(systemName: "forward.fill")
                    .font(.title2)
            }
            .disabled(viewModel.currentSong == nil)
        }
    }
    
    // MARK: - Volume
    private var volumeView: some View {
        HStack {
            Image(systemName: "speaker.fill")
                .foregroundColor(.secondary)
            
            Slider(value: $viewModel.volume, in: 0...1)
                .frame(width: 150)
            
            Image(systemName: "speaker.wave.3.fill")
                .foregroundColor(.secondary)
        }
    }
    
    // MARK: - Helper
    private func formatTime(_ seconds: Double) -> String {
        let minutes = Int(seconds) / 60
        let secs = Int(seconds) % 60
        return String(format: "%d:%02d", minutes, secs)
    }
}

struct PlayerView_Previews: PreviewProvider {
    static var previews: some View {
        PlayerView()
            .environmentObject(MusicPlayerViewModel())
            .frame(width: 400, height: 500)
    }
}
