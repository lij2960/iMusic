"""应用图标资源（使用Android版本的图标）"""
from PyQt5.QtGui import QIcon, QPixmap
from PyQt5.QtCore import Qt
from pathlib import Path
import sys
import os


def get_resource_path(relative_path):
    """获取资源文件的绝对路径（支持打包后的应用）"""
    try:
        # PyInstaller创建临时文件夹，将路径存储在_MEIPASS中
        base_path = sys._MEIPASS
    except Exception:
        # 开发环境
        base_path = os.path.abspath(".")
    
    return os.path.join(base_path, relative_path)


class AppIcon:
    """应用图标管理器"""
    
    @staticmethod
    def create_app_icon() -> QIcon:
        """创建应用图标（使用Android版本的图标）"""
        # 尝试多个可能的路径
        possible_paths = [
            get_resource_path("resources/app_icon.png"),
            get_resource_path("src/resources/app_icon.png"),
            "src/resources/app_icon.png",
        ]
        
        for icon_path in possible_paths:
            if os.path.exists(icon_path):
                return QIcon(icon_path)
        
        # 返回空图标作为后备
        return QIcon()
    
    @staticmethod
    def get_app_icon_pixmap(size: int = 200) -> QPixmap:
        """获取应用图标的Pixmap（用于显示封面）"""
        # 尝试多个可能的路径
        possible_paths = [
            get_resource_path("resources/app_icon.png"),
            get_resource_path("src/resources/app_icon.png"),
            "src/resources/app_icon.png",
        ]
        
        for icon_path in possible_paths:
            if os.path.exists(icon_path):
                pixmap = QPixmap(icon_path)
                if not pixmap.isNull():
                    return pixmap.scaled(size, size, Qt.KeepAspectRatio, Qt.SmoothTransformation)
        
        # 返回灰色占位图
        pixmap = QPixmap(size, size)
        pixmap.fill(Qt.gray)
        return pixmap
    
    @staticmethod
    def create_default_album_art(size: int = 200) -> QPixmap:
        """创建默认专辑封面（使用应用图标）"""
        return AppIcon.get_app_icon_pixmap(size)