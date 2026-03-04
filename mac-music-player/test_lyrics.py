#!/usr/bin/env python3
"""测试歌词解析功能"""

from src.utils.lyrics_parser import LyricsParser

# 测试歌词内容
test_lyrics = """[00:00.00]测试歌曲
[00:05.50]第一句歌词
[00:10.20]第二句歌词
[00:15.80]第三句歌词
[00:20.00]第四句歌词
"""

print("测试歌词内容:")
print(test_lyrics)
print("\n" + "="*50 + "\n")

# 解析歌词
lyrics = LyricsParser.parse_lyrics_content(test_lyrics, song_id=1)

if lyrics:
    print(f"✅ 成功解析歌词，共 {len(lyrics.lines)} 行")
    print("\n解析结果:")
    for i, line in enumerate(lyrics.lines):
        minutes = line.time_ms // 60000
        seconds = (line.time_ms % 60000) // 1000
        print(f"  {i+1}. [{minutes:02d}:{seconds:02d}] {line.text}")
else:
    print("❌ 歌词解析失败")
