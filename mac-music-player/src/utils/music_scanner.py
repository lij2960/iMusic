"""音乐文件扫描器"""
import os
from pathlib import Path
from datetime import datetime
from typing import List
from mutagen import File as MutagenFile
from mutagen.mp3 import MP3
from mutagen.flac import FLAC
from mutagen.mp4 import MP4
from ..models.song import Song


class MusicScanner:
    """音乐扫描器"""
    
    # 与Android版本保持一致的音频格式支持
    SUPPORTED_FORMATS = {
        '.mp3', '.wav', '.flac', '.aac', '.ogg', '.m4a', '.wma', '.opus',
        '.mp4', '.3gp', '.amr', '.awb', '.wv', '.ape', '.dts', '.ac3'
    }
    
    @staticmethod
    def scan_directory(directory_path: str) -> List[Song]:
        """扫描目录中的音乐文件（与Android版本逻辑一致）"""
        songs = []
        directory = Path(directory_path)
        
        if not directory.exists() or not directory.is_dir():
            return songs
        
        # 使用walkTopDown遍历（与Android的walkTopDown一致）
        for file_path in directory.rglob('*'):
            if file_path.is_file() and MusicScanner._is_audio_file(file_path):
                song = MusicScanner._create_song_from_file(file_path)
                if song:
                    songs.append(song)
        
        return songs
    
    @staticmethod
    def _is_audio_file(file: Path) -> bool:
        """检查是否为音频文件（与Android版本一致）"""
        return file.suffix.lower() in MusicScanner.SUPPORTED_FORMATS
    
    @staticmethod
    def _create_song_from_file(file_path: Path) -> Song:
        """从文件创建Song对象（与Android版本逻辑一致）"""
        try:
            audio = MutagenFile(str(file_path), easy=True)
            
            # 默认值（与Android版本一致）
            title = file_path.stem
            artist = "Unknown Artist"
            album = "Unknown Album"
            duration = 0.0
            
            if audio is not None:
                # 提取元数据
                title = MusicScanner._get_tag(audio, 'title', title)
                artist = MusicScanner._get_tag(audio, 'artist', artist)
                album = MusicScanner._get_tag(audio, 'album', album)
                
                # 获取时长
                if hasattr(audio, 'info') and hasattr(audio.info, 'length'):
                    duration = audio.info.length
            
            # 获取文件信息
            stat = file_path.stat()
            date_added = datetime.fromtimestamp(stat.st_ctime)
            date_modified = datetime.fromtimestamp(stat.st_mtime)
            size = stat.st_size
            
            # 获取专辑封面路径（与Android版本逻辑一致）
            album_art = MusicScanner._get_album_art_path(file_path)
            
            return Song(
                id=0,  # 将由数据库分配
                title=title,
                artist=artist,
                album=album,
                duration=duration,
                path=str(file_path),
                album_art=album_art,
                date_added=date_added,
                date_modified=date_modified,
                size=size
            )
        
        except Exception as e:
            print(f"Error extracting metadata from {file_path}: {e}")
            # 返回基本信息作为后备（与Android版本一致）
            return MusicScanner._create_fallback_song(file_path)
    
    @staticmethod
    def _get_tag(audio, tag_name: str, default: str) -> str:
        """获取标签值"""
        try:
            if hasattr(audio, 'tags') and audio.tags:
                value = audio.tags.get(tag_name)
                if value:
                    return value[0] if isinstance(value, list) else str(value)
            return default
        except:
            return default
    
    @staticmethod
    def _get_album_art_path(music_path: Path) -> str:
        """获取专辑封面路径（与Android版本逻辑完全一致）"""
        try:
            directory = music_path.parent
            file_name = music_path.stem
            
            # 按照Android版本的优先级顺序查找封面
            # 注意：Android版本会先检查内部目录，但macOS版本直接检查音乐文件目录
            art_files = [
                # 1. 与音乐文件同名的封面（优先级最高）
                f"{file_name}.jpg",
                f"{file_name}.jpeg", 
                f"{file_name}.png",
                # 2. 通用封面名称（按Android版本顺序）
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
            
            for art_file in art_files:
                art_path = directory / art_file
                if art_path.exists() and art_path.is_file():
                    print(f"✅ Found album art: {art_path}")
                    return str(art_path)
            
            print(f"ℹ️  No album art found for: {music_path.name}")
            return None
        except Exception as e:
            print(f"❌ Error finding album art: {e}")
            return None
    
    @staticmethod
    def _create_fallback_song(file_path: Path) -> Song:
        """创建备用歌曲对象（当无法提取元数据时）"""
        stat = file_path.stat()
        
        return Song(
            id=0,
            title=file_path.stem,
            artist='Unknown Artist',
            album='Unknown Album',
            duration=0.0,
            path=str(file_path),
            album_art=None,
            date_added=datetime.fromtimestamp(stat.st_ctime),
            date_modified=datetime.fromtimestamp(stat.st_mtime),
            size=stat.st_size
        )