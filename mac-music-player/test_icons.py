#!/usr/bin/env python3
"""测试图标和封面生成"""

import sys
from PyQt5.QtWidgets import QApplication, QLabel, QVBoxLayout, QWidget
from PyQt5.QtCore import Qt
from src.resources.app_icon import AppIcon


def test_icons():
    """测试图标生成"""
    app = QApplication(sys.argv)
    
    window = QWidget()
    window.setWindowTitle("图标测试 - 使用Android图标")
    window.setGeometry(100, 100, 500, 700)
    
    # 设置窗口图标
    window.setWindowIcon(AppIcon.create_app_icon())
    
    layout = QVBoxLayout()
    
    # 显示应用图标
    icon_label = QLabel("应用图标 (从Android复制):")
    layout.addWidget(icon_label)
    
    app_icon_display = QLabel()
    app_icon_pixmap = AppIcon.get_app_icon_pixmap(200)
    app_icon_display.setPixmap(app_icon_pixmap)
    app_icon_display.setAlignment(Qt.AlignCenter)
    app_icon_display.setStyleSheet("border: 1px solid #ccc;")
    layout.addWidget(app_icon_display)
    
    # 说明文字
    info_label = QLabel("""
    \n图标说明：
    - 应用图标：从Android版本复制的 ic_launcher.png
    - 没有封面时显示：应用图标（而不是灰色默认封面）
    - 与Android版本完全一致
    
    \n封面显示逻辑：
    1. 如果歌曲有封面 → 显示歌曲封面
    2. 如果歌曲没有封面 → 显示应用图标
    """)
    layout.addWidget(info_label)
    
    # 检查图标文件
    status_label = QLabel()
    if AppIcon.APP_ICON_PATH.exists():
        status_label.setText(f"✅ 图标文件存在: {AppIcon.APP_ICON_PATH}")
        status_label.setStyleSheet("color: green;")
    else:
        status_label.setText(f"❌ 图标文件不存在: {AppIcon.APP_ICON_PATH}")
        status_label.setStyleSheet("color: red;")
    layout.addWidget(status_label)
    
    window.setLayout(layout)
    window.show()
    
    print("="*60)
    print("图标测试")
    print("="*60)
    if AppIcon.APP_ICON_PATH.exists():
        print(f"✅ 应用图标文件: {AppIcon.APP_ICON_PATH}")
        print(f"✅ 图标已从Android版本复制")
    else:
        print(f"❌ 应用图标文件不存在: {AppIcon.APP_ICON_PATH}")
    
    print("\n封面显示逻辑:")
    print("  1. 有封面 → 显示歌曲封面")
    print("  2. 无封面 → 显示应用图标")
    print("="*60)
    
    sys.exit(app.exec_())


if __name__ == '__main__':
    test_icons()
