package com.ijackey.iMusic.data.parser

import com.ijackey.iMusic.data.api.FangpiTrack
import java.net.URLEncoder
import java.util.regex.Pattern

object FangpiParser {
    
    fun parseSearchResults(html: String): List<FangpiTrack> {
        val tracks = mutableListOf<FangpiTrack>()
        
        try {
            android.util.Log.d("FangpiParser", "Parsing HTML length: ${html.length}")
            
            // 更新的正则表达式，适应新的页面结构
            val patterns = listOf(
                // 模式1: 原有格式
                Pattern.compile(
                    "<a href=\"/music/(\\d+)\"[^>]*class=\"music-link[^>]*>.*?" +
                    "<span[^>]*>(.*?)</span>.*?" +
                    "<small[^>]*text-jade[^>]*>(.*?)</small>",
                    Pattern.DOTALL
                ),
                // 模式2: 新格式
                Pattern.compile(
                    "<a[^>]*href=\"/music/(\\d+)\"[^>]*>.*?" +
                    "<div[^>]*class=\"[^\"]*title[^\"]*\"[^>]*>(.*?)</div>.*?" +
                    "<div[^>]*class=\"[^\"]*artist[^\"]*\"[^>]*>(.*?)</div>",
                    Pattern.DOTALL
                ),
                // 模式3: 简化格式
                Pattern.compile(
                    "href=\"/music/(\\d+)\"[^>]*>([^<]+)</a>[^<]*<[^>]*>([^<]+)",
                    Pattern.DOTALL
                )
            )
            
            for (pattern in patterns) {
                val matcher = pattern.matcher(html)
                while (matcher.find()) {
                    val id = matcher.group(1)?.trim()
                    val title = matcher.group(2)?.trim()?.replace("<[^>]+>".toRegex(), "")
                    val artist = matcher.group(3)?.trim()?.replace("<[^>]+>".toRegex(), "")
                    
                    if (!id.isNullOrEmpty() && !title.isNullOrEmpty()) {
                        tracks.add(FangpiTrack(
                            id = id,
                            title = title,
                            artist = artist ?: "未知艺术家",
                            detailUrl = "https://www.fangpi.net/music/$id"
                        ))
                    }
                }
                if (tracks.isNotEmpty()) break
            }
            
            android.util.Log.d("FangpiParser", "Found ${tracks.size} tracks")
            
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