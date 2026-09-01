# 📖 小说续写 App

Android 端小说续写工具：开启无障碍服务后自动读取屏幕上显示的小说文字（翻页自动捕获、自动存档），调用 LLM API（默认 DeepSeek）自动续写。

## ✨ 功能亮点

- **无障碍自动捕获**：翻页时自动读取屏幕上的小说文字，无需手动复制
- **翻页不丢内容**：识别到的文字自动**追加保存**到当前小说，重复内容自动去重
- **小说管理**：可新建多本小说、切换、重命名、删除，原文与续写分开保存
- **悬浮窗控制**：屏幕边缘悬浮按钮，一键启动/暂停识别，可在任何 App 内操作
- **截图 OCR 兜底**：无障碍读不到时，用 ML Kit 离线中文 OCR 识别截图
- **LLM API 续写**：OpenAI 兼容接口（默认 DeepSeek），温度/字数/风格可调
- **现代 UI**：靛蓝紫渐变主题、大圆角卡片、Material 图标

## 🛠 技术栈

| 模块 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 + Material Icons Extended |
| 屏幕文字读取 | AccessibilityService |
| 截图 OCR 兜底 | ML Kit Text Recognition (Chinese) + MediaProjection |
| 续写接口 | OkHttp + OpenAI 兼容 Chat Completions |
| 本地存储 | Room（小说/原文/续写）+ DataStore（设置） |

## 📦 环境要求

- Android Studio Hedgehog 或更新
- Gradle 8.10.2 / AGP 8.7.3 / Kotlin 2.1.0 / compileSdk 35 / minSdk 26（Android 8.0+）

## 🚀 快速开始

1. 用 Android Studio 打开本项目目录
2. 等待 Gradle 同步完成（首次需下载依赖）
3. 连接 Android 设备或启动模拟器（API 26+），点击 Run
4. 或命令行构建 APK：
   ```bash
   ./gradlew assembleDebug
   # APK 输出在 app/build/outputs/apk/debug/app-debug.apk
   ```

## 📱 使用流程

1. **开启无障碍**：打开 App → 捕获页点「去开启」→ 系统无障碍设置中找到「小说续写」并开启
2. **新建小说**：切到「小说」标签 → 点「新建小说」输入书名
3. **开启悬浮窗**：捕获页打开「悬浮窗控制」开关（首次需授予悬浮窗权限），点悬浮窗从 ⏸ 切到 ▶
4. **看小说**：打开小说 App 翻页，文字自动捕获并保存到当前小说
5. **续写**：切到「续写」标签 → 确认原文 → 点「开始续写」，结果自动保存

> 💡 无障碍读不到文字时（如画布渲染的 App），在捕获页点「截图 OCR」手动捕获。
> 💡 续写需要先在「设置」里填写 OpenAI 兼容接口的 API Key（默认 DeepSeek：`https://api.deepseek.com`，模型 `deepseek-chat`）。

## 🗂 项目结构

```
novel-continue-app/
├── app/src/main/
│   ├── java/com/novel/continueapp/
│   │   ├── MainActivity.kt                    # 入口
│   │   ├── NovelApp.kt                        # Application
│   │   ├── model/                             # Book / NovelRecord 数据模型
│   │   ├── data/                              # Room、DAO、Repository、ApiClient、OcrEngine、设置
│   │   ├── service/                           # 无障碍服务、悬浮窗、屏幕截图
│   │   └── ui/                                # Compose 界面 + ViewModel + 主题
│   ├── res/                                   # 图标、无障碍配置、字符串
│   └── AndroidManifest.xml
├── build.gradle.kts / settings.gradle.kts
└── gradle/wrapper/
```

## ⚠️ 注意事项

- App 仅用于**个人学习与创作辅助**，请尊重原作者版权，勿将识别结果用于商业传播
- 悬浮窗/无障碍/屏幕录制权限仅在本机用于识别文字，数据不上传（除续写时调用你配置的 LLM API）
- 抖音等 App 可能对无障碍/截屏有限制，实际效果以目标 App 为准

## 📄 License

仅供学习交流使用。
