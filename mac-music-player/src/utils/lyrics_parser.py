"""歌词解析器（与Android版本逻辑一致）"""
import re
from pathlib import Path
from typing import Optional
from ..models.song import Lyrics, LyricLine


class LyricsParser:
    """歌词解析器"""
    
    # LRC格式正则表达式: [mm:ss.xx]歌词文本
    LRC_PATTERN = re.compile(r'\[(\d{2}):(\d{2})\.(\d{2})\](.*)')
    
    @staticmethod
    def find_lyrics_file(song_path: str) -> Optional[Path]:
        """查找歌词文件（与Android版本逻辑一致）"""
        song_file = Path(song_path)
        directory = song_file.parent
        file_name = song_file.stem
        
        # 按照Android版本的优先级顺序查找
        # 1. 与音乐文件同名的.lrc文件
        lrc_file = directory / f"{file_name}.lrc"
        if lrc_file.exists():
            return lrc_file
        
        # 2. 与音乐文件同名的.txt文件
        txt_file = directory / f"{file_name}.txt"
        if txt_file.exists():
            return txt_file
        
        # 3. 基于路径的.lrc文件（去掉扩展名）
        base_path = song_file.with_suffix('')
        base_lrc = Path(str(base_path) + '.lrc')
        if base_lrc.exists():
            return base_lrc
        
        # 4. 基于路径的.txt文件
        base_txt = Path(str(base_path) + '.txt')
        if base_txt.exists():
            return base_txt
        
        return None
    
    @staticmethod
    def parse_lyrics_file(lyrics_file: Path, song_id: int) -> Optional[Lyrics]:
        """解析歌词文件（与Android版本逻辑一致）"""
        try:
            # 使用UTF-8编码读取（与Android版本一致）
            with open(lyrics_file, 'r', encoding='utf-8') as f:
                content = f.read()
            
            return LyricsParser.parse_lyrics_content(content, song_id)
        
        except Exception as e:
            print(f"Error parsing lyrics file {lyrics_file}: {e}")
            return None
    
    @staticmethod
    def parse_lyrics_content(content: str, song_id: int) -> Optional[Lyrics]:
        """解析歌词内容（与Android版本逻辑一致）"""
        try:
            lines = content.split('\n')
            lyric_lines = []
            
            for line in lines:
                line = line.strip()
                if not line:
                    continue
                
                # 尝试匹配LRC格式
                match = LyricsParser.LRC_PATTERN.match(line)
                if match:
                    minutes = int(match.group(1))
                    seconds = int(match.group(2))
                    centiseconds = int(match.group(3))
                    text = match.group(4).strip()
                    
                    # 计算毫秒（与Android版本一致）
                    time_ms = (minutes * 60 + seconds) * 1000 + centiseconds * 10
                    
                    if text:  # 只添加非空歌词
                        lyric_lines.append(LyricLine(time_ms=time_ms, text=text))
                
                # 纯文本格式（无时间戳）
                elif not line.startswith('['):
                    lyric_lines.append(LyricLine(time_ms=0, text=line))
            
            # 按时间排序（与Android版本一致）
            lyric_lines.sort(key=lambda x: x.time_ms)
            
            if lyric_lines:
                return Lyrics(song_id=song_id, lines=lyric_lines)
            
            return None
        
        except Exception as e:
            print(f"Error parsing lyrics content: {e}")
            return None
    
    @staticmethod
    def get_current_lyric_index(lyrics: Lyrics, current_position_ms: int) -> int:
        """获取当前歌词索引（与Android版本逻辑一致）"""
        if not lyrics.lines:
            return -1
        
        current_index = -1
        for i, line in enumerate(lyrics.lines):
            if line.time_ms <= current_position_ms:
                current_index = i
            else:
                break
        
        return current_index
    
    @staticmethod
    def save_lyrics(song_path: str, lyrics_content: str) -> bool:
        """保存歌词到文件（与Android版本逻辑一致）"""
        try:
            song_file = Path(song_path)
            file_name = song_file.stem
            directory = song_file.parent
            
            # 保存为.lrc文件
            lyrics_file = directory / f"{file_name}.lrc"
            lyrics_file.write_text(lyrics_content, encoding='utf-8')
            
            print(f"Lyrics saved to: {lyrics_file}")
            return True
        
        except Exception as e:
            print(f"Error saving lyrics: {e}")
            return False