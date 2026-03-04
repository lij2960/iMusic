"""应用图标资源（与Android版本保持一致的设计理念）"""
from PyQt5.QtGui import QIcon, QPixmap, QPainter, QColor, QFont
from PyQt5.QtCore import Qt, QRect


class AppIcon:
    """应用图标生成器"""
    
    @staticmethod
    def create_app_icon(size: int = 128) -> QIcon:
        """创建应用图标（音乐符号设计，与Android版本一致）"""
        pixmap = QPixmap(size, size)
        pixmap.fill(Qt.transparent)
        
        painter = QPainter(pixmap)
        painter.setRenderHint(QPainter.Antialiasing)
        
        # 背景圆形（与Android Material Design一致）
        painter.setBrush(QColor(98, 0, 238))  # 紫色主题
        painter.setPen(Qt.NoPen)
        painter.drawEllipse(0, 0, size, size)
        
        # 绘制音乐符号
        painter.setPen(QColor(255, 255, 255))
        painter.setBrush(QColor(255, 255, 255))
        
        # 音符杆
        note_width = size // 20
        note_height = size // 2
        note_x = size // 3
        note_y = size // 4
        painter.drawRect(note_x, note_y, note_width, note_height)
        
        # 音符头
        head_size = size // 6
        painter.drawEllipse(
            note_x - head_size // 2,
            note_y + note_height - head_size // 2,
            head_size,
            head_size
        )
        
        # 第二个音符
        note_x2 = note_x + size // 4
        painter.drawRect(note_x2, note_y, note_width, note_height)
        painter.drawEllipse(
            note_x2 - head_size // 2,
            note_y + note_height - head_size // 2,
            head_size,
            head_size
        )
        
        # 连接线
        painter.drawLine(
            note_x + note_width,
            note_y,
            note_x2,
            note_y
        )
        
        painter.end()
        
        return QIcon(pixmap)
    
    @staticmethod
    def create_default_album_art(size: int = 200) -> QPixmap:
        """创建默认专辑封面（与Android版本一致）"""
        pixmap = QPixmap(size, size)
        pixmap.fill(QColor(240, 240, 240))
        
        painter = QPainter(pixmap)
        painter.setRenderHint(QPainter.Antialiasing)
        
        # 绘制音乐符号
        painter.setPen(QColor(200, 200, 200))
        painter.setBrush(QColor(200, 200, 200))
        
        # 简化的音符
        note_size = size // 3
        note_x = (size - note_size) // 2
        note_y = (size - note_size) // 2
        
        # 音符杆
        stem_width = note_size // 10
        stem_height = note_size
        painter.drawRect(note_x + note_size // 2, note_y, stem_width, stem_height)
        
        # 音符头
        head_size = note_size // 2
        painter.drawEllipse(
            note_x,
            note_y + stem_height - head_size,
            head_size,
            head_size
        )
        
        painter.end()
        
        return pixmap