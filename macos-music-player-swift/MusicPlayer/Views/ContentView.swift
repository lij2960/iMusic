//
//  ContentView.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject var viewModel: MusicPlayerViewModel
    @State private var showingFolderPicker = false
    @State private var showingLyricsSearch = false
    @State private var searchingLyrics = false
    
    var body: some View {
        VStack(spacing: 0) {
            // 顶部工具栏
            toolbarView
            
            Divider()
            
            // 主内容区域
            HSplitView {
                // 左侧：歌曲列表
                songListView
                    .frame(minWidth: 300)
                
                // 右侧：播放器和歌词
                playerAndLyricsView
                    .frame(minWidth: 400)
            }
        }
        .fileImporter(
            isPresented: $showingFolderPicker,
            allowedContentTypes: [.folder],
            allowsMultipleSelection: false
        ) { result in
            handleFolderSelection(result)
        }
        .alert("搜索歌词", isPresented: $showingLyricsSearch) {
            Button("取消", role: .cancel) {}
            Button("搜索") {
                searchLyrics()
            }
        } message: {
            Text("为当前歌曲搜索在线歌词？")
        }
    }
    
    // MARK: - Toolbar
    private var toolbarView: some View {
        HStack {
            // 添加音乐按钮
            Button(action: { showingFolderPicker = true }) {
                Label("添加音乐文件夹", systemImage: "folder.badge.plus")
            }
            
            Divider()
                .frame(height: 20)
            
            // 排序选择
            Text("排序:")
            Picker("", selection: $viewModel.sortOrder) {
                ForEach(SortOrder.allCases, id: \.self) { order in
                    Text(order.rawValue).tag(order)
                }
            }
            .frame(width: 150)
            .onChange(of: viewModel.sortOrder) { newValue in
                viewModel.changeSortOrder(newValue)
            }
            
            Divider()
                .frame(height: 20)
            
            // 播放模式选择
            Text("播放模式:")
            Picker("", selection: $viewModel.playMode) {
                ForEach(PlayMode.allCases, id: \.self) { mode in
                    Label(mode.rawValue, systemImage: mode.icon).tag(mode)
                }
            }
            .frame(width: 150)
            .onChange(of: viewModel.playMode) { newValue in
                viewModel.changePlayMode(newValue)
            }
            
            Spacer()
            
            // 扫描状态
            if viewModel.isScanning {
                ProgressView()
                    .scaleEffect(0.7)
                Text("扫描中...")
                    .font(.caption)
            }
        }
        .padding()
    }
    
    // MARK: - Song List
    private var songListView: some View {
        List(viewModel.songs) { song in
            SongRow(song: song, isPlaying: viewModel.currentSong?.id == song.id)
                .onTapGesture(count: 2) {
                    viewModel.playSong(song)
                }
        }
    }
    
    // MARK: - Player and Lyrics
    private var playerAndLyricsView: some View {
        VStack {
            // 播放器视图
            PlayerView()
            
            Divider()
            
            // 歌词视图
            LyricsView()
        }
    }
    
    // MARK: - Actions
    private func handleFolderSelection(_ result: Result<[URL], Error>) {
        switch result {
        case .success(let urls):
            if let url = urls.first {
                Task {
                    await viewModel.addMusicFolder(url: url)
                }
            }
        case .failure(let error):
            print("选择文件夹失败: \(error.localizedDescription)")
        }
    }
    
    private func searchLyrics() {
        searchingLyrics = true
        Task {
            if let lyrics = await viewModel.searchOnlineLyrics() {
                if viewModel.saveLyrics(lyrics) {
                    print("歌词保存成功")
                }
            }
            searchingLyrics = false
        }
    }
}

// MARK: - Song Row
struct SongRow: View {
    let song: Song
    let isPlaying: Bool
    
    var body: some View {
        HStack {
            if isPlaying {
                Image(systemName: "play.fill")
                    .foregroundColor(.blue)
            }
            
            VStack(alignment: .leading) {
                Text(song.title)
                    .font(.headline)
                    .foregroundColor(isPlaying ? .blue : .primary)
                
                Text("\(song.artist) - \(song.album)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Text(song.durationString)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(.vertical, 4)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .environmentObject(MusicPlayerViewModel())
            .frame(width: 1000, height: 700)
    }
}
