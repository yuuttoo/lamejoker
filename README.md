# StoryTeller

一個 Android 應用程式，使用 Jetpack Compose 和 Google Generative AI。

## 設置

### 1. 克隆專案
```bash
git clone <your-repository-url>
cd StoryTeller
```

### 2. 設置 API Key
1. 複製 `local.properties.example` 到 `local.properties`
2. 在 `local.properties` 中添加你的 API key：
   ```properties
   apiKey=YOUR_ACTUAL_API_KEY_HERE
   ```

**重要：** 不要將 `local.properties` 提交到版本控制系統！

### 3. 同步專案
在 Android Studio 中同步 Gradle 文件，或執行：
```bash
./gradlew build
```

## 功能
- 使用 Google Generative AI 生成內容
- Jetpack Compose UI
- MVVM 架構
- 響應式設計

## 技術棧
- Kotlin
- Jetpack Compose
- Android Architecture Components
- Google Generative AI
- Gradle (Kotlin DSL) 