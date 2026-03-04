#!/usr/bin/env python3
"""音乐播放器主程序入口"""
import sys
from PyQt5.QtWidgets import QApplication
from src.ui.main_window import MainWindow


def main():
    """主函数"""
    app = QApplication(sys.argv)
    app.setApplicationName("音乐播放器")
    app.setOrganizationName("MusicPlayer")
    
    window = MainWindow()
    window.show()
    
    sys.exit(app.exec_())


if __name__ == "__main__":
    main()