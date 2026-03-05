//
//  LyricsView.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import SwiftUI

struct LyricsView: View {
    @EnvironmentObject var viewModel: MusicPlayerViewModel
    @State private var showingFullLyrics = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // 标题栏
            HStack {
                Text("歌词")
                    .font(.headline)
                
                Spacer()
                
                if viewModel.currentLyrics != nil {
                    Button(action: { showingFullLyrics = true }) {
                        Label("完整歌词", systemImage: "doc.text")
                            .font(.caption)
                    }
                }
            }
            .padding(.horizontal)
            .padding(.top, 8)
            
            Divider()
            
            // 歌词列表
            if let lyrics = viewModel.currentLyrics, !lyrics.lines.isEmpty {
                ScrollViewReader { proxy in
                    List(Array(lyrics.lines.enumerated()), id: \.offset) { index, line in
                        LyricLineView(
                            line: line,
                            isHighlighted: index == viewModel.currentLyricIndex
                        )
                        .id(index)
                        .onTapGesture {
                            if line.timeMs > 0 {
                                viewModel.seek(to: Double(line.timeMs) / 1000.0)
                            }
                        }
                    }
                    .onChange(of: viewModel.currentLyricIndex) { newIndex in
                        withAnimation {
                            proxy.scrollTo(newIndex, anchor: .center)
                        }
                    }
                }
            } else {
                // 无歌词
                VStack {
                    Spacer()
                    Text("暂无歌词")
                        .foregroundColor(.secondary)
                    Spacer()
                }
            }
        }
        .sheet(isPresented: $showingFullLyrics) {
            FullLyricsView(lyrics: viewModel.currentLyrics)
        }
    }
}

// MARK: - Lyric Line View
struct LyricLineView: View {
    let line: LyricLine
    let isHighlighted: Bool
    
    var body: some View {
        HStack {
            if line.timeMs > 0 {
                Text(formatTime(line.timeMs))
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .frame(width: 50, alignment: .leading)
            }
            
            Text(line.text)
                .font(.body)
                .foregroundColor(isHighlighted ? .blue : .primary)
                .fontWeight(isHighlighted ? .bold : .regular)
        }
        .padding(.vertical, 2)
    }
    
    private func formatTime(_ ms: Int) -> String {
        let totalSeconds = ms / 1000
        let minutes = totalSeconds / 60
        let seconds = totalSeconds % 60
        return String(format: "%02d:%02d", minutes, seconds)
    }
}

// MARK: - Full Lyrics View
struct FullLyricsView: View {
    let lyrics: Lyrics?
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        VStack {
            // 标题栏
            HStack {
                Text("完整歌词")
                    .font(.title2)
                    .fontWeight(.bold)
                
                Spacer()
                
                Button("关闭") {
                    dismiss()
                }
            }
            .padding()
            
            Divider()
            
            // 歌词内容
            ScrollView {
                if let lyrics = lyrics {
                    VStack(alignment: .leading, spacing: 8) {
                        ForEach(Array(lyrics.lines.enumerated()), id: \.offset) { _, line in
                            HStack(alignment: .top) {
                                if line.timeMs > 0 {
                                    Text(formatTime(line.timeMs))
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                        .frame(width: 60, alignment: .leading)
                                }
                                
                                Text(line.text)
                                    .font(.body)
                            }
                        }
                    }
                    .padding()
                } else {
                    Text("暂无歌词")
                        .foregroundColor(.secondary)
                        .padding()
                }
            }
        }
        .frame(width: 500, height: 600)
    }
    
    private func formatTime(_ ms: Int) -> String {
        let totalSeconds = ms / 1000
        let minutes = totalSeconds / 60
        let seconds = totalSeconds % 60
        return String(format: "[%02d:%02d]", minutes, seconds)
    }
}

struct LyricsView_Previews: PreviewProvider {
    static var previews: some View {
        LyricsView()
            .environmentObject(MusicPlayerViewModel())
            .frame(width: 400, height: 400)
    }
}
