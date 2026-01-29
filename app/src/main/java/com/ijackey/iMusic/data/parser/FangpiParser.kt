package com.ijackey.iMusic.data.parser

import com.ijackey.iMusic.data.api.FangpiTrack
import java.net.URLEncoder
import java.util.regex.Pattern

object FangpiParser {
    
    fun parseSearchResults(html: String): List<FangpiTrack> {
        val tracks = mutableListOf<FangpiTrack>()
        
        try {
            // 使用正则表达式解析音乐链接
            val musicLinkPattern = Pattern.compile(
                "<a href=\"/music/(\\d+)\"[^>]*class=\"music-link[^>]*>.*?" +
                "<span[^>]*>(.*?)</span>.*?" +
                "<small[^>]*text-jade[^>]*>(.*?)</small>",
                Pattern.DOTALL
            )
            
            val matcher = musicLinkPattern.matcher(html)
            while (matcher.find()) {
                val id = matcher.group(1)
                val title = matcher.group(2).trim()
                val artist = matcher.group(3).trim()
                
                tracks.add(FangpiTrack(
                    id = id,
                    title = title,
                    artist = artist,
                    detailUrl = "https://www.fangpi.net/music/$id"
                ))
            }
        } catch (e: Exception) {
            android.util.Log.e("FangpiParser", "Error parsing search results: ${e.message}")
        }
        
        return tracks
    }
    
    fun parseDownloadUrl(html: String): String? {
        try {
            // 首先查找JavaScript中的appData
            val appDataPattern = Pattern.compile("window\\.appData\\s*=\\s*JSON\\.parse\\('([^']+)'\\)")
            val appDataMatcher = appDataPattern.matcher(html)
            
            if (appDataMatcher.find()) {
                val jsonString = appDataMatcher.group(1)
                    .replace("\\u0022", "\"")
                    .replace("\\/", "/")
                    .replace("\\\\u", "\\u")
                
                android.util.Log.d("FangpiParser", "Found appData JSON: $jsonString")
                
                // 解析JSON中的extra_recommend_wap_url
                val urlPattern = Pattern.compile("\"extra_recommend_wap_url\":\"([^\"]+)\"")
                val urlMatcher = urlPattern.matcher(jsonString)
                
                if (urlMatcher.find()) {
                    val downloadUrl = urlMatcher.group(1)
                        .replace("\\/", "/")  // 去除转义字符
                    android.util.Log.d("FangpiParser", "Found download URL from appData: $downloadUrl")
                    return downloadUrl
                }
            }
            
            // 备用方案：查找下载按钮的href属性
            val downloadPattern = Pattern.compile("id=\"btn-download-mp3\"[^>]*href=\"([^\"]+)\"")
            val matcher = downloadPattern.matcher(html)
            
            if (matcher.find()) {
                val href = matcher.group(1)
                android.util.Log.d("FangpiParser", "Found download URL from button: $href")
                return href
            }
            
            // 最后备用方案：查找MP3链接
            val mp3Pattern = Pattern.compile("(https?://[^\\s\"']+\\.mp3)")
            val mp3Matcher = mp3Pattern.matcher(html)
            if (mp3Matcher.find()) {
                return mp3Matcher.group(1)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("FangpiParser", "Error parsing download URL: ${e.message}")
        }
        
        return null
    }
    
    fun encodeKeyword(keyword: String): String {
        return URLEncoder.encode(keyword, "UTF-8")
    }
}