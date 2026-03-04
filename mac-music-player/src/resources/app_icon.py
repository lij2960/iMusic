"""应用图标资源（使用Android版本的图标）"""
from PyQt5.QtGui import QIcon, QPixmap
from PyQt5.QtCore import Qt
from pathlib import Path


class AppIcon:
    """应用图标管理器"""
    
    # 资源文件路径
    RESOURCES_DIR = Path(__file__).parent
    APP_ICON_PATH = RESOURCES_DIR / "app_icon.png"
    DEFAULT_ALBUM_ART_PATH = RESOURCES_DIR / "default_album_art.png"
    
    @staticmethod
    def create_app_icon() -> QIcon:
        """创建应用图标（使用Android版本的图标）"""
        if AppIcon.APP_ICON_PATH.exists():
            return QIcon(str(AppIcon.APP_ICON_PATH))
        else:
            print(f"⚠️  App icon not found: {AppIcon.APP_ICON_PATH}")
            # 返回空图标作为后备
            return QIcon()
    
    @staticmethod
    def get_app_icon_pixmap(size: int = 200) -> QPixmap:
        """获取应用图标的Pixmap（用于显示封面）"""
        if AppIcon.APP_ICON_PATH.exists():
            pixmap = QPixmap(str(AppIcon.APP_ICON_PATH))
            if not pixmap.isNull():
                # 缩放到指定尺寸
                return pixmap.scaled(size, size, Qt.KeepAspectRatio, Qt.SmoothTransformation)
        
        print(f"⚠️  App icon pixmap not found: {AppIcon.APP_ICON_PATH}")
        # 返回空Pixmap作为后备
        return QPixmap(size, size)
    
    @staticmethod
    def create_default_album_art(size: int = 200) -> QPixmap:
        """创建默认专辑封面（使用Android版本的默认封面，已废弃，改用应用图标）"""
        # 注意：根据新需求，没有封面时应该显示应用图标，而不是默认封面
        # 保留此方法是为了兼容性，但实际应该使用 get_app_icon_pixmap()
        return AppIcon.get_app_icon_pixmap(size)