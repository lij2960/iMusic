import os
import sys

# 设置Qt插件路径
if hasattr(sys, '_MEIPASS'):
    # PyInstaller运行时
    qt_plugins_path = os.path.join(sys._MEIPASS, 'PyQt5', 'Qt5', 'plugins')
    if os.path.exists(qt_plugins_path):
        os.environ['QT_PLUGIN_PATH'] = qt_plugins_path
        print(f"✅ Qt插件路径已设置: {qt_plugins_path}")
    else:
        print(f"⚠️  Qt插件路径不存在: {qt_plugins_path}")
