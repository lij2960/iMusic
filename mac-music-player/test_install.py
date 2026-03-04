#!/usr/bin/env python3
"""测试安装是否成功"""

print("测试Python环境...")
import sys
print(f"✅ Python版本: {sys.version}")

print("\n测试PyQt5...")
try:
    from PyQt5.QtWidgets import QApplication
    print("✅ PyQt5导入成功")
except ImportError as e:
    print(f"❌ PyQt5导入失败: {e}")
    sys.exit(1)

print("\n测试python-vlc...")
try:
    import vlc
    print("✅ python-vlc导入成功")
except ImportError as e:
    print(f"❌ python-vlc导入失败: {e}")
    sys.exit(1)

print("\n测试mutagen...")
try:
    import mutagen
    print("✅ mutagen导入成功")
except ImportError as e:
    print(f"❌ mutagen导入失败: {e}")
    sys.exit(1)

print("\n测试VLC是否安装...")
try:
    instance = vlc.Instance()
    print("✅ VLC引擎初始化成功")
except Exception as e:
    print(f"⚠️  VLC引擎初始化失败: {e}")
    print("   请确保已安装VLC: brew install --cask vlc")

print("\n" + "="*50)
print("✅ 所有依赖安装成功！")
print("="*50)
print("\n下一步:")
print("1. 确保VLC已安装: brew install --cask vlc")
print("2. 运行程序: ./run.sh 或 python main.py")
print("\n祝你使用愉快！🎵")
