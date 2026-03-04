"""音乐数据模型"""
from dataclasses import dataclass
from typing import Optional
from datetime import datetime


@dataclass
class Song:
    """歌曲数据类"""
    id: int
    title: str
    artist: str
    album: str
    duration: float  # 秒
    path: str
    album_art: Optional[str]
    date_added: datetime
    date_modified: datetime
    size: int
    
    def get_display_name(self) -> str:
        """获取显示名称"""
        if self.artist and self.artist != "Unknown Artist":
            return f"{self.artist} - {self.title}"
        return self.title
    
    def get_duration_string(self) -> str:
        """获取时长字符串"""
        minutes = int(self.duration // 60)
        seconds = int(self.duration % 60)
        return f"{minutes}:{seconds:02d}"


@dataclass
class LyricLine:
    """歌词行数据类"""
    time_ms: int
    text: str


@dataclass
class Lyrics:
    """歌词数据类"""
    song_id: int
    lines: list[LyricLine]


class PlayMode:
    """播放模式枚举"""
    REPEAT_ALL = "repeat_all"
    REPEAT_ONE = "repeat_one"
    SHUFFLE = "shuffle"


class SortOrder:
    """排序方式枚举"""
    DATE_ADDED_ASC = "date_added_asc"
    DATE_ADDED_DESC = "date_added_desc"
    TITLE_ASC = "title_asc"
    TITLE_DESC = "title_desc"
    ARTIST_ASC = "artist_asc"
    ARTIST_DESC = "artist_desc"
    DURATION_ASC = "duration_asc"
    DURATION_DESC = "duration_desc"


@dataclass
class PlaybackState:
    """播放状态数据类"""
    current_song_id: Optional[int]
    position: float  # 秒
    play_mode: str
    sort_order: str
    is_playing: bool