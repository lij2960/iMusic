"""主窗口UI"""
from PyQt5.QtWidgets import (
    QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, 
    QPushButton, QLabel, QSlider, QListWidget, QListWidgetItem,
    QFileDialog, QComboBox, QMessageBox, QDialog, QTextEdit,
    QProgressDialog, QInputDialog
)
from PyQt5.QtCore import Qt, QTimer, QThread, pyqtSignal
from PyQt5.QtGui import QPixmap, QIcon
from typing import Optional
from ..models.song import Song, PlayMode, SortOrder, Lyrics
from ..player.music_player import MusicPlayer
from ..database.db_manager import DatabaseManager
from ..utils.music_scanner import MusicScanner
from ..utils.lyrics_parser import LyricsParser
from ..resources.app_icon import AppIcon
from ..api.lyrics_api import LyricsAPI


class MainWindow(QMainWindow):
    """主窗口类"""
    
    def __init__(self):
        super().__init__()
        self.setWindowTitle("音乐播放器")
        self.setGeometry(100, 100, 1000, 700)
        
        # 设置应用图标（使用Android版本的图标）
        self.setWindowIcon(AppIcon.create_app_icon())
        
        # 初始化组件
        self.db = DatabaseManager()
        self.player = MusicPlayer()
        self.current_lyrics: Optional[Lyrics] = None
        self.current_lyric_index: int = -1
        
        # 应用图标Pixmap（用于没有封面时显示）
        self.app_icon_pixmap = AppIcon.get_app_icon_pixmap(200)
        
        # 设置播放器回调
        self.player.on_song_changed = self.on_song_changed
        
        # 初始化UI
        self.init_ui()
        
        # 定时器更新播放进度
        self.timer = QTimer()
        self.timer.timeout.connect(self.update_progress)
        self.timer.start(1000)  # 每秒更新一次
        
        # 加载保存的状态
        self.load_saved_state()
    
    def init_ui(self):
        """初始化UI"""
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        
        main_layout = QVBoxLayout()
        central_widget.setLayout(main_layout)
        
        # 顶部控制栏
        top_layout = QHBoxLayout()
        
        self.btn_add_music = QPushButton("添加音乐文件夹")
        self.btn_add_music.clicked.connect(self.add_music_folder)
        top_layout.addWidget(self.btn_add_music)
        
        top_layout.addWidget(QLabel("排序:"))
        self.combo_sort = QComboBox()
        self.combo_sort.addItems([
            "日期添加(最新)",
            "日期添加(最旧)",
            "标题(A-Z)",
            "标题(Z-A)",
            "艺术家(A-Z)",
            "艺术家(Z-A)",
            "时长(短)",
            "时长(长)"
        ])
        self.combo_sort.currentIndexChanged.connect(self.on_sort_changed)
        top_layout.addWidget(self.combo_sort)
        
        top_layout.addWidget(QLabel("播放模式:"))
        self.combo_play_mode = QComboBox()
        self.combo_play_mode.addItems(["列表循环", "单曲循环", "随机播放"])
        self.combo_play_mode.currentIndexChanged.connect(self.on_play_mode_changed)
        top_layout.addWidget(self.combo_play_mode)
        
        top_layout.addStretch()
        main_layout.addLayout(top_layout)
        
        # 中间内容区域
        content_layout = QHBoxLayout()
        
        # 左侧：歌曲列表
        self.song_list = QListWidget()
        self.song_list.itemDoubleClicked.connect(self.on_song_double_clicked)
        content_layout.addWidget(self.song_list, 2)
        
        # 右侧：播放器和歌词
        right_layout = QVBoxLayout()
        
        # 专辑封面
        self.album_art_label = QLabel()
        self.album_art_label.setFixedSize(200, 200)
        self.album_art_label.setAlignment(Qt.AlignCenter)
        self.album_art_label.setStyleSheet("border: 1px solid #ccc; background-color: #f0f0f0;")
        # 显示应用图标作为初始封面
        self.album_art_label.setPixmap(self.app_icon_pixmap)
        right_layout.addWidget(self.album_art_label, alignment=Qt.AlignCenter)
        
        # 歌曲信息
        self.label_song_title = QLabel("未播放")
        self.label_song_title.setAlignment(Qt.AlignCenter)
        self.label_song_title.setStyleSheet("font-size: 16px; font-weight: bold;")
        right_layout.addWidget(self.label_song_title)
        
        self.label_artist = QLabel("")
        self.label_artist.setAlignment(Qt.AlignCenter)
        self.label_artist.setStyleSheet("font-size: 14px; color: #666;")
        right_layout.addWidget(self.label_artist)
        
        # 进度条
        progress_layout = QHBoxLayout()
        self.label_current_time = QLabel("0:00")
        progress_layout.addWidget(self.label_current_time)
        
        self.slider_progress = QSlider(Qt.Horizontal)
        self.slider_progress.setMinimum(0)
        self.slider_progress.setMaximum(100)
        self.slider_progress.sliderReleased.connect(self.on_slider_released)
        progress_layout.addWidget(self.slider_progress)
        
        self.label_total_time = QLabel("0:00")
        progress_layout.addWidget(self.label_total_time)
        
        right_layout.addLayout(progress_layout)
        
        # 播放控制按钮
        control_layout = QHBoxLayout()
        
        self.btn_previous = QPushButton("⏮ 上一首")
        self.btn_previous.clicked.connect(self.player.previous)
        control_layout.addWidget(self.btn_previous)
        
        self.btn_play_pause = QPushButton("▶ 播放")
        self.btn_play_pause.clicked.connect(self.play_pause)
        control_layout.addWidget(self.btn_play_pause)
        
        self.btn_next = QPushButton("下一首 ⏭")
        self.btn_next.clicked.connect(self.player.next)
        control_layout.addWidget(self.btn_next)
        
        self.btn_show_lyrics = QPushButton("📄 完整歌词")
        self.btn_show_lyrics.clicked.connect(self.show_full_lyrics)
        self.btn_show_lyrics.setEnabled(False)
        control_layout.addWidget(self.btn_show_lyrics)
        
        self.btn_search_lyrics = QPushButton("🔍 搜索歌词")
        self.btn_search_lyrics.clicked.connect(self.search_online_lyrics)
        self.btn_search_lyrics.setEnabled(False)
        control_layout.addWidget(self.btn_search_lyrics)
        
        right_layout.addLayout(control_layout)
        
        # 音量控制
        volume_layout = QHBoxLayout()
        volume_layout.addWidget(QLabel("音量:"))
        self.slider_volume = QSlider(Qt.Horizontal)
        self.slider_volume.setMinimum(0)
        self.slider_volume.setMaximum(100)
        self.slider_volume.setValue(70)
        self.slider_volume.valueChanged.connect(self.on_volume_changed)
        volume_layout.addWidget(self.slider_volume)
        right_layout.addLayout(volume_layout)
        
        # 歌词显示区域
        right_layout.addWidget(QLabel("歌词:"))
        self.lyrics_list = QListWidget()
        self.lyrics_list.itemClicked.connect(self.on_lyric_clicked)
        right_layout.addWidget(self.lyrics_list)
        
        content_layout.addLayout(right_layout, 1)
        main_layout.addLayout(content_layout)
        
        # 加载歌曲列表
        self.refresh_song_list()
    
    def add_music_folder(self):
        """添加音乐文件夹"""
        folder = QFileDialog.getExistingDirectory(self, "选择音乐文件夹")
        if folder:
            # 扫描音乐文件
            songs = MusicScanner.scan_directory(folder)
            
            # 保存到数据库
            for song in songs:
                existing = self.db.get_song_by_path(song.path)
                if not existing:
                    self.db.insert_song(song)
            
            # 刷新列表
            self.refresh_song_list()
            
            QMessageBox.information(self, "完成", f"已添加 {len(songs)} 首歌曲")
    
    def refresh_song_list(self):
        """刷新歌曲列表"""
        sort_order = self.get_current_sort_order()
        songs = self.db.get_all_songs(sort_order)
        
        self.song_list.clear()
        for song in songs:
            item = QListWidgetItem(song.get_display_name())
            item.setData(Qt.UserRole, song)
            self.song_list.addItem(item)
        
        # 更新播放器播放列表
        self.player.set_playlist(songs)
    
    def on_song_double_clicked(self, item: QListWidgetItem):
        """双击歌曲项"""
        song = item.data(Qt.UserRole)
        if song:
            self.player.play_song(song)
    
    def highlight_current_song(self, song: Song):
        """高亮播放列表中正在播放的歌曲"""
        for i in range(self.song_list.count()):
            item = self.song_list.item(i)
            item_song = item.data(Qt.UserRole)
            
            if item_song and item_song.id == song.id:
                # 正在播放的歌曲 - 使用蓝色粗体
                item.setForeground(Qt.blue)
                font = item.font()
                font.setBold(True)
                item.setFont(font)
                # 添加播放图标
                item.setText(f"▶ {item_song.get_display_name()}")
                # 滚动到当前歌曲
                self.song_list.scrollToItem(item)
            else:
                # 其他歌曲 - 恢复默认样式
                item.setForeground(Qt.black)
                font = item.font()
                font.setBold(False)
                item.setFont(font)
                if item_song:
                    item.setText(item_song.get_display_name())
    
    def on_song_changed(self, song: Song):
        """歌曲改变回调"""
        self.label_song_title.setText(song.title)
        self.label_artist.setText(f"{song.artist} - {song.album}")
        self.label_total_time.setText(song.get_duration_string())
        self.slider_progress.setMaximum(int(song.duration))
        
        # 启用搜索歌词按钮
        self.btn_search_lyrics.setEnabled(True)
        
        # 高亮播放列表中正在播放的歌曲
        self.highlight_current_song(song)
        
        # 加载专辑封面（与Android版本逻辑一致）
        # 如果歌曲有封面，显示封面；如果没有，显示应用图标
        cover_loaded = False
        if song.album_art:
            try:
                pixmap = QPixmap(song.album_art)
                if not pixmap.isNull():
                    scaled_pixmap = pixmap.scaled(200, 200, Qt.KeepAspectRatio, Qt.SmoothTransformation)
                    self.album_art_label.setPixmap(scaled_pixmap)
                    cover_loaded = True
                    print(f"✅ Loaded album art from: {song.album_art}")
            except Exception as e:
                print(f"❌ Error loading album art: {e}")
        
        if not cover_loaded:
            # 没有封面或加载失败，显示应用图标（与Android版本逻辑一致）
            self.album_art_label.setPixmap(self.app_icon_pixmap)
            try:
                print(f"ℹ️  Using app icon for: {song.title}")
            except UnicodeEncodeError:
                # 处理中文编码问题
                print(f"ℹ️  Using app icon for song ID: {song.id}")
        
        # 加载歌词
        self.load_lyrics(song)
        
        # 更新播放按钮
        self.btn_play_pause.setText("⏸ 暂停")
    
    def load_lyrics(self, song: Song):
        """加载歌词"""
        lyrics_file = LyricsParser.find_lyrics_file(song.path)
        if lyrics_file:
            print(f"Found lyrics file: {lyrics_file}")
            self.current_lyrics = LyricsParser.parse_lyrics_file(lyrics_file, song.id)
            if self.current_lyrics:
                print(f"Parsed {len(self.current_lyrics.lines)} lyrics lines")
                self.display_lyrics()
                self.btn_show_lyrics.setEnabled(True)
                return
            else:
                print("Failed to parse lyrics file")
        else:
            try:
                print(f"No lyrics file found for: {song.path}")
            except UnicodeEncodeError:
                print(f"No lyrics file found for song ID: {song.id}")
        
        # 没有歌词
        self.current_lyrics = None
        self.lyrics_list.clear()
        self.lyrics_list.addItem("暂无歌词")
        self.btn_show_lyrics.setEnabled(False)
    
    def display_lyrics(self):
        """显示歌词"""
        if not self.current_lyrics:
            print("No lyrics to display")
            return
        
        print(f"Displaying {len(self.current_lyrics.lines)} lyrics lines")
        self.lyrics_list.clear()
        for line in self.current_lyrics.lines:
            item = QListWidgetItem(line.text)
            item.setData(Qt.UserRole, line)
            self.lyrics_list.addItem(item)
        
        # 重置当前歌词索引
        self.current_lyric_index = -1
    
    def update_progress(self):
        """更新播放进度"""
        if self.player.is_playing and self.player.current_song:
            position = self.player.get_position()
            self.slider_progress.setValue(int(position))
            
            minutes = int(position // 60)
            seconds = int(position % 60)
            self.label_current_time.setText(f"{minutes}:{seconds:02d}")
            
            # 更新当前歌词
            if self.current_lyrics:
                position_ms = int(position * 1000)
                new_index = LyricsParser.get_current_lyric_index(self.current_lyrics, position_ms)
                
                if new_index != self.current_lyric_index:
                    self.current_lyric_index = new_index
                    self.highlight_current_lyric()
            
            # 检查是否播放结束（轮询方式，避免VLC回调死锁）
            if self.player.is_at_end():
                # 播放结束，自动播放下一首
                self.player.next()
    
    def highlight_current_lyric(self):
        """高亮当前歌词"""
        for i in range(self.lyrics_list.count()):
            item = self.lyrics_list.item(i)
            if i == self.current_lyric_index:
                item.setForeground(Qt.blue)
                self.lyrics_list.scrollToItem(item)
            else:
                item.setForeground(Qt.black)
    
    def on_lyric_clicked(self, item: QListWidgetItem):
        """点击歌词行"""
        lyric_line = item.data(Qt.UserRole)
        if lyric_line and lyric_line.time_ms > 0:
            self.player.seek(lyric_line.time_ms / 1000.0)
    
    def play_pause(self):
        """播放/暂停"""
        if not self.player.current_song:
            # 如果没有当前歌曲，播放第一首
            if self.song_list.count() > 0:
                first_item = self.song_list.item(0)
                song = first_item.data(Qt.UserRole)
                self.player.play_song(song)
        else:
            self.player.play_pause()
            
            if self.player.is_playing:
                self.btn_play_pause.setText("⏸ 暂停")
            else:
                self.btn_play_pause.setText("▶ 播放")
    
    def on_slider_released(self):
        """进度条释放"""
        position = self.slider_progress.value()
        self.player.seek(position)
    
    def on_volume_changed(self, value: int):
        """音量改变"""
        self.player.set_volume(value)
    
    def on_sort_changed(self, index: int):
        """排序改变"""
        self.refresh_song_list()
    
    def on_play_mode_changed(self, index: int):
        """播放模式改变"""
        modes = [PlayMode.REPEAT_ALL, PlayMode.REPEAT_ONE, PlayMode.SHUFFLE]
        self.player.set_play_mode(modes[index])
    
    def get_current_sort_order(self) -> str:
        """获取当前排序方式"""
        index = self.combo_sort.currentIndex()
        orders = [
            SortOrder.DATE_ADDED_DESC,
            SortOrder.DATE_ADDED_ASC,
            SortOrder.TITLE_ASC,
            SortOrder.TITLE_DESC,
            SortOrder.ARTIST_ASC,
            SortOrder.ARTIST_DESC,
            SortOrder.DURATION_ASC,
            SortOrder.DURATION_DESC,
        ]
        return orders[index]
    
    def show_full_lyrics(self):
        """显示完整歌词对话框"""
        if not self.current_lyrics:
            return
        
        dialog = QDialog(self)
        dialog.setWindowTitle("完整歌词")
        dialog.setGeometry(200, 200, 500, 600)
        
        layout = QVBoxLayout()
        
        text_edit = QTextEdit()
        text_edit.setReadOnly(True)
        
        lyrics_text = ""
        for line in self.current_lyrics.lines:
            if line.time_ms > 0:
                minutes = line.time_ms // 60000
                seconds = (line.time_ms % 60000) // 1000
                lyrics_text += f"[{minutes:02d}:{seconds:02d}] {line.text}\n"
            else:
                lyrics_text += f"{line.text}\n"
        
        text_edit.setText(lyrics_text)
        layout.addWidget(text_edit)
        
        btn_close = QPushButton("关闭")
        btn_close.clicked.connect(dialog.close)
        layout.addWidget(btn_close)
        
        dialog.setLayout(layout)
        dialog.exec()
    
    def search_online_lyrics(self):
        """搜索在线歌词（与Android版本逻辑一致）"""
        if not self.player.current_song:
            QMessageBox.warning(self, "提示", "请先选择一首歌曲")
            return
        
        song = self.player.current_song
        
        # 显示进度对话框
        progress = QProgressDialog("正在搜索歌词...", "取消", 0, 0, self)
        progress.setWindowTitle("搜索歌词")
        progress.setWindowModality(Qt.WindowModal)
        progress.show()
        
        try:
            # 搜索多个歌词选项
            lyrics_options = LyricsAPI.search_multiple_lyrics(song.title, song.artist)
            
            progress.close()
            
            if not lyrics_options:
                QMessageBox.information(self, "提示", "未找到歌词")
                return
            
            # 如果只有一个选项，直接应用
            if len(lyrics_options) == 1:
                self.apply_lyrics(lyrics_options[0][1])
                return
            
            # 多个选项，让用户选择
            items = [option[0] for option in lyrics_options]
            item, ok = QInputDialog.getItem(
                self, 
                "选择歌词", 
                "找到多个歌词版本，请选择：", 
                items, 
                0, 
                False
            )
            
            if ok and item:
                index = items.index(item)
                self.apply_lyrics(lyrics_options[index][1])
        
        except Exception as e:
            progress.close()
            QMessageBox.critical(self, "错误", f"搜索歌词失败：{str(e)}")
    
    def apply_lyrics(self, lyrics_content: str):
        """应用歌词（与Android版本逻辑一致）"""
        if not self.player.current_song:
            return
        
        song = self.player.current_song
        
        print(f"Applying lyrics for song: {song.title}")
        print(f"Lyrics content length: {len(lyrics_content)}")
        
        # 保存歌词到文件
        if LyricsParser.save_lyrics(song.path, lyrics_content):
            print("Lyrics saved successfully, reloading...")
            
            # 重新加载歌词
            self.load_lyrics(song)
            
            # 检查是否成功加载
            if self.current_lyrics and len(self.current_lyrics.lines) > 0:
                QMessageBox.information(self, "成功", f"歌词已保存并加载\n共 {len(self.current_lyrics.lines)} 行歌词")
            else:
                QMessageBox.warning(self, "警告", "歌词已保存，但重新加载失败，请重新播放歌曲")
        else:
            QMessageBox.critical(self, "错误", "保存歌词失败")
    
    def load_saved_state(self):
        """加载保存的状态"""
        state = self.db.get_playback_state()
        if state:
            # 恢复排序方式
            sort_orders = [
                SortOrder.DATE_ADDED_DESC,
                SortOrder.DATE_ADDED_ASC,
                SortOrder.TITLE_ASC,
                SortOrder.TITLE_DESC,
                SortOrder.ARTIST_ASC,
                SortOrder.ARTIST_DESC,
                SortOrder.DURATION_ASC,
                SortOrder.DURATION_DESC,
            ]
            if state.sort_order in sort_orders:
                self.combo_sort.setCurrentIndex(sort_orders.index(state.sort_order))
            
            # 恢复播放模式
            play_modes = [PlayMode.REPEAT_ALL, PlayMode.REPEAT_ONE, PlayMode.SHUFFLE]
            if state.play_mode in play_modes:
                self.combo_play_mode.setCurrentIndex(play_modes.index(state.play_mode))
                self.player.set_play_mode(state.play_mode)
            
            # 恢复当前歌曲
            if state.current_song_id:
                song = self.db.get_song_by_id(state.current_song_id)
                if song:
                    self.player.play_song(song)
                    self.player.seek(state.position)
                    if not state.is_playing:
                        self.player.play_pause()
    
    def closeEvent(self, event):
        """窗口关闭事件"""
        # 保存播放状态
        from ..models.song import PlaybackState
        
        state = PlaybackState(
            current_song_id=self.player.current_song.id if self.player.current_song else None,
            position=self.player.get_position(),
            play_mode=self.player.play_mode,
            sort_order=self.get_current_sort_order(),
            is_playing=self.player.is_playing
        )
        self.db.save_playback_state(state)
        
        # 清理资源
        self.player.cleanup()
        self.db.close()
        
        event.accept()