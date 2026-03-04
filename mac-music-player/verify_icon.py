#!/usr/bin/env python3
"""验证图标文件和加载"""

from pathlib import Path

print("="*60)
print("图标文件验证")
print("="*60)

# 直接检查文件路径
resources_dir = Path(__file__).parent / "src" / "resources"
app_icon_path = resources_dir / "app_icon.png"
default_album_art_path = resources_dir / "default_album_art.png"

# 检查图标文件
if app_icon_path.exists():
    size = app_icon_path.stat().st_size
    print(f"✅ 应用图标文件存在")
    print(f"   路径: {app_icon_path}")
    print(f"   大小: {size / 1024:.1f} KB")
else:
    print(f"❌ 应用图标文件不存在")
    print(f"   预期路径: {app_icon_path}")

print()

# 检查默认封面文件（备用）
if default_album_art_path.exists():
    size = default_album_art_path.stat().st_size
    print(f"ℹ️  默认封面文件存在（备用，未使用）")
    print(f"   路径: {default_album_art_path}")
    print(f"   大小: {size / 1024:.1f} KB")
else:
    print(f"ℹ️  默认封面文件不存在（不影响使用）")

print()
print("="*60)
print("封面显示逻辑")
print("="*60)
print("1. 如果歌曲有封面 → 显示歌曲封面")
print("2. 如果歌曲没有封面 → 显示应用图标")
print("="*60)

print("\n" + "="*60)
print("验证完成！")
print("="*60)
print("\n提示：运行 'python3 mac-music-player/test_icons.py' 查看图标效果")
print("提示：运行 './run.sh' 启动音乐播放器")
