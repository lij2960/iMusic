//
//  MusicScanner.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import AVFoundation
import AppKit

/// 音乐文件扫描器
class MusicScanner {
    /// 支持的音频格式（与Android/Python版本一致）
    static let supportedFormats = [
        "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus",
        "mp4", "3gp", "amr", "awb", "wv", "ape", "dts", "ac3"
    ]
    
    /// 扫描目录
    static func scanDirectory(at url: URL) async -> [Song] {
        var songs: [Song] = []
        
        guard let enumerator = FileManager.default.enumerator(
            at: url,
            includingPropertiesForKeys: [.isRegularFileKey, .fileSizeKey, .creationDateKey],
            options: [.skipsHiddenFiles]
        ) else { return songs }
        
        for case let fileURL as URL in enumerator {
            if supportedFormats.contains(fileURL.pathExtension.lowercased()) {
                if let song = await createSong(from: fileURL) {
                    songs.append(song)
                }
            }
        }
        
        return songs
    }
    
    /// 从文件创建歌曲对象
    private static func createSong(from url: URL) async -> Song? {
        let asset = AVAsset(url: url)
        
        var title = url.deletingPathExtension().lastPathComponent
        var artist = "Unknown Artist"
        var album = "Unknown Album"
        var duration: TimeInterval = 0
        
        // 提取元数据
        do {
            let metadata = try await asset.load(.metadata)
            
            for item in metadata {
                guard let key = item.commonKey?.rawValue,
                      let value = try? await item.load(.stringValue) else { continue }
                
                switch key {
                case "title":
                    title = value
                case "artist":
                    artist = value
                case "albumName":
                    album = value
                default:
                    break
                }
            }
            
            // 获取时长
            duration = try await asset.load(.duration).seconds
        } catch {
            print("提取元数据失败: \(error.localizedDescription)")
        }
        
        // 获取文件信息
        let attributes = try? FileManager.default.attributesOfItem(atPath: url.path)
        let size = attributes?[.size] as? Int64 ?? 0
        let dateAdded = attributes?[.creationDate] as? Date ?? Date()
        
        // 查找专辑封面
        let albumArtPath = findAlbumArt(for: url)
        
        return Song(
            title: title,
            artist: artist,
            album: album,
            duration: duration,
            path: url.path,
            albumArt: albumArtPath,
            dateAdded: dateAdded,
            size: size
        )
    }
    
    /// 查找专辑封面（与Android/Python版本逻辑一致）
    private static func findAlbumArt(for musicURL: URL) -> String? {
        let directory = musicURL.deletingLastPathComponent()
        let fileName = musicURL.deletingPathExtension().lastPathComponent
        
        // 按照优先级顺序查找
        let artFiles = [
            // 1. 与音乐文件同名的封面
            "\(fileName).jpg",
            "\(fileName).jpeg",
            "\(fileName).png",
            // 2. 通用封面名称
            "cover.jpg",
            "cover.jpeg",
            "cover.png",
            "folder.jpg",
            "folder.jpeg",
            "folder.png",
            "album.jpg",
            "album.jpeg",
            "album.png"
        ]
        
        for artFile in artFiles {
            let artURL = directory.appendingPathComponent(artFile)
            if FileManager.default.fileExists(atPath: artURL.path) {
                return artURL.path
            }
        }
        
        return nil
    }
}
