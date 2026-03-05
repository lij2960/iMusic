//
//  LyricsAPI.swift
//  MusicPlayer
//
//  Created on 2026-03-04.
//  Copyright © 2026 iJackey. All rights reserved.
//

import Foundation

/// 在线歌词API（与Android版本使用相同的API）
class LyricsAPI {
    // 使用与Android版本相同的API地址
    private static let baseURL = "https://music-api.heheda.top"
    private static let searchAPI = "\(baseURL)/search"
    private static let lyricsAPI = "\(baseURL)/lyric"
    
    /// 搜索在线歌词
    static func searchOnlineLyrics(title: String, artist: String) async -> String? {
        let keywords = "\(title) \(artist)"
        print("🔍 搜索歌词: \(keywords)")
        
        // 搜索歌曲
        guard let searchURL = URL(string: searchAPI),
              var components = URLComponents(url: searchURL, resolvingAgainstBaseURL: false) else {
            print("❌ 无效的搜索URL")
            return nil
        }
        
        components.queryItems = [
            URLQueryItem(name: "keywords", value: keywords),
            URLQueryItem(name: "type", value: "1"),
            URLQueryItem(name: "limit", value: "20")
        ]
        
        guard let url = components.url else {
            print("❌ 无法构建URL")
            return nil
        }
        
        print("📡 请求URL: \(url.absoluteString)")
        
        var request = URLRequest(url: url)
        request.setValue("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36", forHTTPHeaderField: "User-Agent")
        request.timeoutInterval = 30
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            
            if let httpResponse = response as? HTTPURLResponse {
                print("📥 响应状态码: \(httpResponse.statusCode)")
                
                if httpResponse.statusCode != 200 {
                    print("❌ HTTP错误: \(httpResponse.statusCode)")
                    return nil
                }
            }
            
            // 打印原始响应用于调试
            if let jsonString = String(data: data, encoding: .utf8) {
                print("📄 响应内容前200字符: \(jsonString.prefix(200))...")
            }
            
            // 解析JSON
            guard let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
                print("❌ 无法解析JSON")
                if let rawString = String(data: data, encoding: .utf8) {
                    print("原始响应: \(rawString.prefix(500))")
                }
                return nil
            }
            
            // 检查code
            if let code = json["code"] as? Int, code != 200 {
                print("❌ API返回错误码: \(code)")
                return nil
            }
            
            guard let result = json["result"] as? [String: Any],
                  let songs = result["songs"] as? [[String: Any]],
                  !songs.isEmpty else {
                print("❌ 未找到歌曲")
                return nil
            }
            
            guard let firstSong = songs.first,
                  let songId = firstSong["id"] as? Int else {
                print("❌ 无法获取歌曲ID")
                return nil
            }
            
            print("✅ 找到歌曲ID: \(songId)")
            
            // 获取歌词
            return await getLyrics(songId: songId)
        } catch {
            print("❌ 搜索失败: \(error.localizedDescription)")
            print("错误详情: \(error)")
            return nil
        }
    }
    
    /// 获取歌词
    private static func getLyrics(songId: Int) async -> String? {
        guard let lyricsURL = URL(string: lyricsAPI),
              var components = URLComponents(url: lyricsURL, resolvingAgainstBaseURL: false) else {
            print("❌ 无效的歌词URL")
            return nil
        }
        
        components.queryItems = [
            URLQueryItem(name: "id", value: "\(songId)")
        ]
        
        guard let url = components.url else {
            print("❌ 无法构建歌词URL")
            return nil
        }
        
        print("📡 请求歌词URL: \(url.absoluteString)")
        
        var request = URLRequest(url: url)
        request.setValue("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36", forHTTPHeaderField: "User-Agent")
        request.timeoutInterval = 30
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            
            if let httpResponse = response as? HTTPURLResponse {
                print("📥 歌词响应状态码: \(httpResponse.statusCode)")
                
                if httpResponse.statusCode != 200 {
                    print("❌ HTTP错误: \(httpResponse.statusCode)")
                    return nil
                }
            }
            
            guard let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
                print("❌ 无法解析歌词JSON")
                return nil
            }
            
            // 检查code
            if let code = json["code"] as? Int, code != 200 {
                print("❌ 歌词API返回错误码: \(code)")
                return nil
            }
            
            guard let lrc = json["lrc"] as? [String: Any],
                  let lyric = lrc["lyric"] as? String else {
                print("❌ 未找到歌词内容")
                return nil
            }
            
            print("✅ 歌词获取成功，长度: \(lyric.count) 字符")
            print("📝 歌词前100字符: \(lyric.prefix(100))...")
            return lyric
        } catch {
            print("❌ 获取歌词失败: \(error.localizedDescription)")
            print("错误详情: \(error)")
            return nil
        }
    }
    
    /// 搜索多个歌词选项
    static func searchMultipleLyrics(title: String, artist: String) async -> [(String, String)] {
        let keywords = "\(title) \(artist)"
        print("🔍 搜索多个歌词选项: \(keywords)")
        
        guard let searchURL = URL(string: searchAPI),
              var components = URLComponents(url: searchURL, resolvingAgainstBaseURL: false) else {
            return []
        }
        
        components.queryItems = [
            URLQueryItem(name: "keywords", value: keywords),
            URLQueryItem(name: "type", value: "1"),
            URLQueryItem(name: "limit", value: "10")
        ]
        
        guard let url = components.url else { return [] }
        
        var request = URLRequest(url: url)
        request.setValue("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36", forHTTPHeaderField: "User-Agent")
        request.timeoutInterval = 30
        
        do {
            let (data, _) = try await URLSession.shared.data(for: request)
            
            guard let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                  let result = json["result"] as? [String: Any],
                  let songs = result["songs"] as? [[String: Any]] else {
                return []
            }
            
            var lyricsOptions: [(String, String)] = []
            
            for song in songs.prefix(5) {
                guard let songId = song["id"] as? Int,
                      let name = song["name"] as? String,
                      let artists = song["artists"] as? [[String: Any]],
                      let artistName = artists.first?["name"] as? String else {
                    continue
                }
                
                if let lyrics = await getLyrics(songId: songId), !lyrics.isEmpty {
                    let displayName = "\(name) - \(artistName)"
                    lyricsOptions.append((displayName, lyrics))
                }
            }
            
            print("✅ 找到 \(lyricsOptions.count) 个歌词选项")
            return lyricsOptions
        } catch {
            print("❌ 搜索失败: \(error.localizedDescription)")
            return []
        }
    }
}
