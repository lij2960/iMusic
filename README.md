# iMusic - Android 音乐播放器

一个功能完整的Android音乐播放器，支持本地音乐播放、歌词显示、多种播放模式等功能。

## 主要功能

### 1. 音乐导入和扫描
- ✅ 指定目录导入本地音乐
- ✅ 全盘扫描音乐文件
- ✅ 支持多种音频格式：MP3, WAV, FLAC, AAC, OGG, M4A, WMA, OPUS
- ✅ 自动提取音乐元数据（标题、艺术家、专辑、时长等）

### 2. 音乐排序
- ✅ 默认按文件创建日期排序
- ✅ 支持多种排序方式：
  - 创建日期（默认）
  - 歌曲标题
  - 艺术家
  - 时长

### 3. 播放模式
- ✅ 默认列表循环播放
- ✅ 支持三种播放模式：
  - 顺序播放（SEQUENTIAL）
  - 单曲循环（REPEAT_ONE）
  - 随机播放（SHUFFLE）

### 4. 歌词功能
- ✅ 自动加载同目录歌词文件
- ✅ 支持LRC格式歌词同步显示
- ✅ 支持纯文本歌词显示
- ✅ 点击歌词行跳转到对应时间点
- ✅ 歌词界面类似QQ音乐风格
- ✅ 当前播放行高亮显示
- ✅ 在线歌词搜索功能（网易云音乐API）

### 5. 播放状态缓存
- ✅ 记住上次播放的歌曲
- ✅ 记住上次播放位置
- ✅ 记住播放模式设置
- ✅ 记住排序方式设置
- ✅ 下次启动从上次位置继续

### 6. 音质优化
- ✅ 使用ExoPlayer高质量音频引擎
- ✅ 配置最佳音频属性
- ✅ 支持高比特率音频
- ✅ 音频焦点管理
- ✅ 均衡器功能

### 7. 搜索功能
- ✅ 按歌名搜索
- ✅ 按艺术家搜索
- ✅ 按文件名搜索
- ✅ 实时搜索结果
- ✅ 在线音乐搜索（网易云音乐API）

### 8. 专辑封面显示
- ✅ 自动加载专辑封面
- ✅ 支持多种封面文件格式
- ✅ 播放器界面显示专辑封面
- ✅ 在线专辑封面搜索和下载

## 技术特性

### 架构
- **MVVM架构模式**
- **Jetpack Compose** 现代UI框架
- **Hilt** 依赖注入
- **Room** 本地数据库
- **ExoPlayer** 音频播放引擎
- **Coroutines + Flow** 异步编程
- **Retrofit** 网络请求
- **Coil** 图片加载

### 网络服务
- **网易云音乐API** - 歌词和音乐搜索
- **国内CDN加速** - 更快的访问速度
- **完整的错误处理** - 网络异常处理

### 权限管理
- 自动请求存储权限
- 适配Android 13+ 媒体权限
- 优雅的权限请求界面

### 用户体验
- Material Design 3 设计语言
- 流畅的动画效果
- 响应式界面设计
- 直观的操作逻辑

## 界面截图

### 主要界面
1. **音乐库** - 显示所有音乐，支持搜索和排序
2. **播放器** - 音乐播放控制，显示专辑封面和进度
3. **导入** - 选择目录导入音乐
4. **歌词** - 显示同步歌词，支持点击跳转
5. **均衡器** - 音频均衡器调节
6. **在线** - 在线音乐搜索和资源下载

### 功能特色
- 当前播放歌曲高亮显示
- 播放模式图标实时更新
- 歌词同步滚动显示
- 搜索结果实时过滤
- 专辑封面自动显示
- 均衡器预设和手动调节
- 在线资源搜索和下载

## 使用说明

### 首次使用
1. 授予存储权限和网络权限
2. 点击"导入"选择音乐目录或全盘扫描
3. 等待扫描完成
4. 在音乐库中选择歌曲播放

### 歌词使用
1. 将 .lrc 或 .txt 歌词文件放在音乐文件同目录
2. 确保歌词文件名与音乐文件名相同
3. 播放音乐时点击歌词按钮查看
4. 点击歌词行可跳转到对应时间点
5. 使用"搜索歌词"按钮在线搜索歌词（网易云音乐）

### 专辑封面
1. 将封面图片文件放在音乐文件同目录
2. 支持的文件名：cover.jpg, folder.jpg, album.jpg等
3. 播放器会自动显示专辑封面
4. 使用"搜索封面"按钮在线搜索和下载封面

### 在线功能
1. 点击底部导航"在线"标签页
2. 搜索歌曲、艺术家或专辑
3. 点击搜索结果下载专辑封面
4. 所有在线功能使用网易云音乐API

### 均衡器使用
1. 在播放器界面点击均衡器按钮
2. 选择预设或手动调节频段
3. 支持多种音乐风格预设

### 播放控制
- 点击歌曲开始播放
- 使用播放器界面控制播放/暂停/上一首/下一首
- 拖动进度条调整播放位置
- 点击播放模式按钮切换播放方式

## 开发环境

- **Android Studio** Hedgehog | 2023.1.1+
- **Kotlin** 1.9.0+
- **Compose BOM** 2024.02.00+
- **Target SDK** 36
- **Min SDK** 24

## 依赖库

```kotlin
// UI
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose")

// 音频播放
implementation("androidx.media3:media3-exoplayer")
implementation("androidx.media3:media3-session")
implementation("androidx.media3:media3-effect")

// 数据库
implementation("androidx.room:room-runtime")
implementation("androidx.room:room-ktx")

// 依赖注入
implementation("com.google.dagger:hilt-android")
implementation("androidx.hilt:hilt-navigation-compose")

// 权限
implementation("com.google.accompanist:accompanist-permissions")

// 网络请求
implementation("com.squareup.retrofit2:retrofit")
implementation("com.squareup.retrofit2:converter-gson")
implementation("com.squareup.okhttp3:logging-interceptor")

// 图片加载
implementation("io.coil-kt:coil-compose")
```

## API服务

### 网易云音乐API
- **基础URL**: https://music-api.heheda.top/
- **歌词搜索**: /lyric?keywords={关键词}
- **音乐搜索**: /search?keywords={关键词}&type=1&limit=20
- **访问速度**: 国内优化，响应更快
- **数据质量**: 丰富的中文音乐资源

### 功能对比
| 功能 | 海外API | 国内API |
|------|---------|---------|
| 访问速度 | 较慢 | 快速 |
| 中文支持 | 一般 | 优秀 |
| 歌词质量 | 一般 | 高质量 |
| 专辑封面 | 有限 | 丰富 |

## 构建说明

1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击运行按钮构建并安装应用

## 注意事项

- 需要Android 7.0 (API 24) 及以上版本
- 首次使用需要授予存储权限和网络权限
- 歌词文件需要UTF-8编码
- 建议使用高质量音频文件获得最佳体验
- 在线功能需要网络连接
- 使用国内API，访问速度更快

## 已完成功能

- ✅ 在线歌词搜索（网易云音乐API）
- ✅ 专辑封面显示和在线搜索
- ✅ 均衡器功能
- ✅ 在线音乐搜索（网易云音乐API）

## 未来计划

- [ ] 播放列表管理
- [ ] 主题切换
- [ ] 睡眠定时器
- [ ] 音频可视化
- [ ] 云端同步
- [ ] 完整歌词下载功能

## 许可证

MIT License