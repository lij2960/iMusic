#!/bin/bash

# 清理旧的应用版本，只保留最新的iMusic.app

echo "清理旧的应用版本..."
echo ""

# 删除旧版本
if [ -d "音乐播放器.app" ]; then
    echo "删除旧版本: 音乐播放器.app (64K)"
    rm -rf "音乐播放器.app"
fi

# 删除调试版本
if [ -d "iMusic_Debug.app" ]; then
    echo "删除调试版本: iMusic_Debug.app (84M)"
    rm -rf "iMusic_Debug.app"
fi

echo ""
echo "✅ 清理完成！"
echo ""
echo "保留的应用:"
ls -lh *.app 2>/dev/null | grep "^d" || echo "  iMusic.app"
echo ""
