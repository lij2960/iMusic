"""
版本信息
"""

__version__ = "1.0.0"
__app_name__ = "音乐播放器"
__app_name_en__ = "MusicPlayer"
__author__ = "iJackey"
__copyright__ = "Copyright © 2026 iJackey. All rights reserved."
__description__ = "macOS音乐播放器 - 基于Android版本的Python实现"

# 版本历史
VERSION_HISTORY = {
    "1.0.0": {
        "date": "2026-03-04",
        "features": [
            "支持14种音频格式",
            "8种排序方式",
            "3种播放模式",
            "本地歌词加载和同步显示",
            "在线歌词搜索（网易云音乐API）",
            "专辑封面自动加载",
            "播放状态缓存",
            "播放列表高亮",
            "使用Android版本图标",
        ],
        "fixes": [
            "修复播放切换时的死锁问题",
            "修复歌词显示问题",
            "统一图标和封面显示逻辑",
        ]
    }
}

def get_version_string():
    """获取版本字符串"""
    return f"{__app_name__} v{__version__}"

def get_about_text():
    """获取关于文本"""
    return f"""
{__app_name__} v{__version__}

{__description__}

{__copyright__}

功能特性:
- 支持多种音频格式
- 8种排序方式
- 3种播放模式
- 歌词同步显示
- 在线歌词搜索
- 专辑封面显示

系统要求:
- macOS 12.7.6 或更高版本
- Python 3.12+
- VLC播放器
"""

if __name__ == "__main__":
    print(get_version_string())
    print(get_about_text())
