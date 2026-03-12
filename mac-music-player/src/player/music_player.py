"""音乐播放器核心"""
import vlc
import random
import time
from typing import List, Optional, Callable
from ..models.song import Song, PlayMode


class MusicPlayer:
    """音乐播放器类"""
    
    def __init__(self):
        """初始化播放器"""
        # 创建VLC实例，添加参数来抑制音频设备错误和详细日志
        vlc_args = [
            '--no-xlib',
            '--quiet',  # 减少日志输出
            '--no-video',  # 不需要视频
            '--aout=auhal',  # 使用macOS的音频输出
            '--verbose=0',  # 最小日志级别
        ]
        self.instance = vlc.Instance(' '.join(vlc_args))
        self.player = self.instance.media_player_new()
        
        self.playlist: List[Song] = []
        self.current_index: int = 0
        self.current_song: Optional[Song] = None
        self.play_mode: str = PlayMode.REPEAT_ALL
        self.is_playing: bool = False
        self.volume: float = 70
        self._is_switching: bool = False  # 防止切歌时的死锁
        
        # 回调函数
        self.on_song_changed: Optional[Callable[[Song], None]] = None
        self.on_playback_finished: Optional[Callable[[], None]] = None
        
        self.player.audio_set_volume(int(self.volume))
        
        # 不使用VLC的事件回调，改用轮询方式检测播放结束
        # 这样可以避免回调导致的死锁问题
    
    def set_playlist(self, songs: List[Song], start_index: int = 0):
        """设置播放列表"""
        self.playlist = songs
        self.current_index = max(0, min(start_index, len(songs) - 1))
    
    def play_song(self, song: Song):
        """播放指定歌曲"""
        if self._is_switching:
            return  # 如果正在切歌，忽略新的播放请求
        
        try:
            self._is_switching = True
            self.current_song = song
            
            # 更新当前索引
            if song in self.playlist:
                self.current_index = self.playlist.index(song)
            
            # 停止当前播放
            self.player.stop()
            
            # 加载新媒体
            media = self.instance.media_new(song.path)
            self.player.set_media(media)
            
            # 开始播放
            self.player.play()
            self.is_playing = True
            
            # 触发回调
            if self.on_song_changed:
                self.on_song_changed(song)
        
        except Exception as e:
            print(f"Error playing song {song.path}: {e}")
            self.is_playing = False
        finally:
            self._is_switching = False
    
    def play_pause(self):
        """播放/暂停切换"""
        if self.is_playing:
            self.player.pause()
            self.is_playing = False
        else:
            self.player.play()
            self.is_playing = True
    
    def stop(self):
        """停止播放"""
        self.player.stop()
        self.is_playing = False
    
    def next(self):
        """下一首"""
        if not self.playlist or self._is_switching:
            return
        
        if self.play_mode == PlayMode.SHUFFLE:
            self.current_index = random.randint(0, len(self.playlist) - 1)
        elif self.play_mode == PlayMode.REPEAT_ONE:
            # 单曲循环，保持当前索引
            pass
        else:  # REPEAT_ALL
            self.current_index = (self.current_index + 1) % len(self.playlist)
        
        self.play_song(self.playlist[self.current_index])
    
    def previous(self):
        """上一首"""
        if not self.playlist or self._is_switching:
            return
        
        if self.play_mode == PlayMode.SHUFFLE:
            self.current_index = random.randint(0, len(self.playlist) - 1)
        elif self.play_mode == PlayMode.REPEAT_ONE:
            # 单曲循环，保持当前索引
            pass
        else:  # REPEAT_ALL
            self.current_index = (self.current_index - 1) % len(self.playlist)
        
        self.play_song(self.playlist[self.current_index])
    
    def seek(self, position: float):
        """跳转到指定位置（秒）"""
        try:
            # VLC使用0-1之间的比例
            if self.current_song and self.current_song.duration > 0:
                ratio = position / self.current_song.duration
                self.player.set_position(ratio)
        except Exception as e:
            print(f"Error seeking to position {position}: {e}")
    
    def get_position(self) -> float:
        """获取当前播放位置（秒）"""
        try:
            if self.current_song and self.current_song.duration > 0:
                position_ratio = self.player.get_position()
                return position_ratio * self.current_song.duration
            return 0.0
        except:
            return 0.0
    
    def is_at_end(self) -> bool:
        """检查是否播放到结尾"""
        try:
            state = self.player.get_state()
            # 检查是否已结束
            return state == vlc.State.Ended
        except:
            return False
    
    def set_volume(self, volume: float):
        """设置音量（0-100）"""
        self.volume = max(0, min(100, volume))
        self.player.audio_set_volume(int(self.volume))
    
    def get_volume(self) -> float:
        """获取当前音量"""
        return self.volume
    
    def set_play_mode(self, mode: str):
        """设置播放模式"""
        if mode in [PlayMode.REPEAT_ALL, PlayMode.REPEAT_ONE, PlayMode.SHUFFLE]:
            self.play_mode = mode
    
    def cleanup(self):
        """清理资源"""
        self.player.stop()
        self.player.release()
        self.instance.release()