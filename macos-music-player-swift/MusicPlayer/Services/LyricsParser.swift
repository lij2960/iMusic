//
//  LyricsParser.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import Foundation

/// 歌词解析器
class LyricsParser {
    /// LRC格式正则表达式
    private static let lrcPattern = #"\[(\d{2}):(\d{2})\.(\d{2})\](.*)"#
    
    /// 查找歌词文件（与Android/Python版本逻辑一致）
    static func findLyricsFile(for songPath: String) -> URL? {
        let songURL = URL(fileURLWithPath: songPath)
        let directory = songURL.deletingLastPathComponent()
        let fileName = songURL.deletingPathExtension().lastPathComponent
        
        // 按照优先级顺序查找
        let lyricsFiles = [
            "\(fileName).lrc",
            "\(fileName).txt"
        ]
        
        for lyricsFile in lyricsFiles {
            let lyricsURL = directory.appendingPathComponent(lyricsFile)
            if FileManager.default.fileExists(atPath: lyricsURL.path) {
                return lyricsURL
            }
        }
        
        return nil
    }
    
    /// 解析歌词文件
    static func parseLyricsFile(at url: URL, songId: UUID) -> Lyrics? {
        guard let content = try? String(contentsOf: url, encoding: .utf8) else {
            return nil
        }
        
        return parseLyricsContent(content, songId: songId)
    }
    
    /// 解析歌词内容
    static func parseLyricsContent(_ content: String, songId: UUID) -> Lyrics? {
        var lines: [LyricLine] = []
        
        let contentLines = content.components(separatedBy: .newlines)
        
        for line in contentLines {
            let trimmedLine = line.trimmingCharacters(in: .whitespaces)
            guard !trimmedLine.isEmpty else { continue }
            
            // 尝试匹配LRC格式
            if let match = try? NSRegularExpression(pattern: lrcPattern)
                .firstMatch(in: trimmedLine, range: NSRange(trimmedLine.startIndex..., in: trimmedLine)) {
                
                let nsString = trimmedLine as NSString
                let minutes = Int(nsString.substring(with: match.range(at: 1))) ?? 0
                let seconds = Int(nsString.substring(with: match.range(at: 2))) ?? 0
                let centiseconds = Int(nsString.substring(with: match.range(at: 3))) ?? 0
                let text = nsString.substring(with: match.range(at: 4)).trimmingCharacters(in: .whitespaces)
                
                let timeMs = (minutes * 60 + seconds) * 1000 + centiseconds * 10
                
                if !text.isEmpty {
                    lines.append(LyricLine(timeMs: timeMs, text: text))
                }
            } else if !trimmedLine.hasPrefix("[") {
                // 纯文本格式（无时间戳）
                lines.append(LyricLine(timeMs: 0, text: trimmedLine))
            }
        }
        
        guard !lines.isEmpty else { return nil }
        
        return Lyrics(songId: songId, lines: lines)
    }
    
    /// 保存歌词到文件
    static func saveLyrics(_ content: String, for songPath: String) -> Bool {
        let songURL = URL(fileURLWithPath: songPath)
        let directory = songURL.deletingLastPathComponent()
        let fileName = songURL.deletingPathExtension().lastPathComponent
        
        let lyricsURL = directory.appendingPathComponent("\(fileName).lrc")
        
        do {
            try content.write(to: lyricsURL, atomically: true, encoding: .utf8)
            print("歌词已保存: \(lyricsURL.path)")
            return true
        } catch {
            print("保存歌词失败: \(error.localizedDescription)")
            return false
        }
    }
}
