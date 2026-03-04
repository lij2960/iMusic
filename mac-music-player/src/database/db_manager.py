"""数据库管理器"""
import sqlite3
from typing import List, Optional
from datetime import datetime
from pathlib import Path
from ..models.song import Song, PlaybackState, PlayMode, SortOrder


class DatabaseManager:
    """数据库管理类"""
    
    def __init__(self, db_path: str = "music_player.db"):
        """初始化数据库"""
        self.db_path = db_path
        self.conn = sqlite3.connect(db_path, check_same_thread=False)
        self.conn.row_factory = sqlite3.Row
        self._create_tables()
    
    def _create_tables(self):
        """创建数据库表"""
        cursor = self.conn.cursor()
        
        # 创建歌曲表
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS songs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                artist TEXT,
                album TEXT,
                duration REAL,
                path TEXT UNIQUE NOT NULL,
                album_art TEXT,
                date_added TIMESTAMP,
                date_modified TIMESTAMP,
                size INTEGER
            )
        """)
        
        # 创建播放状态表
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS playback_state (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                current_song_id INTEGER,
                position REAL,
                play_mode TEXT,
                sort_order TEXT,
                is_playing INTEGER
            )
        """)
        
        self.conn.commit()
    
    def insert_song(self, song: Song) -> int:
        """插入歌曲"""
        cursor = self.conn.cursor()
        cursor.execute("""
            INSERT OR REPLACE INTO songs 
            (title, artist, album, duration, path, album_art, date_added, date_modified, size)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            song.title, song.artist, song.album, song.duration, song.path,
            song.album_art, song.date_added, song.date_modified, song.size
        ))
        self.conn.commit()
        return cursor.lastrowid
    
    def get_all_songs(self, sort_order: str = SortOrder.DATE_ADDED_DESC) -> List[Song]:
        """获取所有歌曲"""
        order_by = self._get_order_by_clause(sort_order)
        cursor = self.conn.cursor()
        cursor.execute(f"SELECT * FROM songs ORDER BY {order_by}")
        
        songs = []
        for row in cursor.fetchall():
            songs.append(self._row_to_song(row))
        
        return songs
    
    def get_song_by_id(self, song_id: int) -> Optional[Song]:
        """根据ID获取歌曲"""
        cursor = self.conn.cursor()
        cursor.execute("SELECT * FROM songs WHERE id = ?", (song_id,))
        row = cursor.fetchone()
        
        if row:
            return self._row_to_song(row)
        return None
    
    def get_song_by_path(self, path: str) -> Optional[Song]:
        """根据路径获取歌曲"""
        cursor = self.conn.cursor()
        cursor.execute("SELECT * FROM songs WHERE path = ?", (path,))
        row = cursor.fetchone()
        
        if row:
            return self._row_to_song(row)
        return None
    
    def delete_song(self, song_id: int):
        """删除歌曲"""
        cursor = self.conn.cursor()
        cursor.execute("DELETE FROM songs WHERE id = ?", (song_id,))
        self.conn.commit()
    
    def save_playback_state(self, state: PlaybackState):
        """保存播放状态"""
        cursor = self.conn.cursor()
        cursor.execute("""
            INSERT OR REPLACE INTO playback_state 
            (id, current_song_id, position, play_mode, sort_order, is_playing)
            VALUES (1, ?, ?, ?, ?, ?)
        """, (
            state.current_song_id, state.position, state.play_mode,
            state.sort_order, 1 if state.is_playing else 0
        ))
        self.conn.commit()
    
    def get_playback_state(self) -> Optional[PlaybackState]:
        """获取播放状态"""
        cursor = self.conn.cursor()
        cursor.execute("SELECT * FROM playback_state WHERE id = 1")
        row = cursor.fetchone()
        
        if row:
            return PlaybackState(
                current_song_id=row['current_song_id'],
                position=row['position'],
                play_mode=row['play_mode'],
                sort_order=row['sort_order'],
                is_playing=bool(row['is_playing'])
            )
        return None
    
    def _row_to_song(self, row) -> Song:
        """将数据库行转换为Song对象"""
        return Song(
            id=row['id'],
            title=row['title'],
            artist=row['artist'],
            album=row['album'],
            duration=row['duration'],
            path=row['path'],
            album_art=row['album_art'],
            date_added=datetime.fromisoformat(row['date_added']) if row['date_added'] else datetime.now(),
            date_modified=datetime.fromisoformat(row['date_modified']) if row['date_modified'] else datetime.now(),
            size=row['size']
        )
    
    def _get_order_by_clause(self, sort_order: str) -> str:
        """获取排序子句"""
        order_map = {
            SortOrder.DATE_ADDED_ASC: "date_added ASC",
            SortOrder.DATE_ADDED_DESC: "date_added DESC",
            SortOrder.TITLE_ASC: "title ASC",
            SortOrder.TITLE_DESC: "title DESC",
            SortOrder.ARTIST_ASC: "artist ASC",
            SortOrder.ARTIST_DESC: "artist DESC",
            SortOrder.DURATION_ASC: "duration ASC",
            SortOrder.DURATION_DESC: "duration DESC",
        }
        return order_map.get(sort_order, "date_added DESC")
    
    def close(self):
        """关闭数据库连接"""
        self.conn.close()