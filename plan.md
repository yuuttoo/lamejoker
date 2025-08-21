# 冷笑話大師 (Cold Joke Teller) App 開發計畫 (v3)

## 1. 專案目標

開發一款 Android 原生應用程式，旨在為生活苦悶的用戶提供輕鬆的娛樂。此 App 以講冷笑話為核心，並根據用戶的反應動態調整互動方式，創造更有趣、更具互動性的體驗。專案嚴格遵循 MVVM 架構，**使用 Google Gemini API 作為笑話的動態來源**，使用 Jetpack Compose 進行 UI 開發，並包含完整的單元測試，確保程式碼品質與穩定性。

## 2. 已實現的核心功能

### ✅ 已完成功能：

1. **雙語冷笑話顯示**：
   - App 啟動時自動透過 Gemini API 產生一則隨機的英文冷笑話
   - 同時提供繁體中文翻譯，以「---」分隔
   - 支援多種笑話主題：動物、食物、工作、學校、科技、人際關係、旅行、運動、音樂、電影、書籍、科學、歷史、政治、天氣、購物、烹飪、園藝、健身等

2. **用戶反饋機制**：
   - 畫面上提供「不好笑」按鈕
   - 當用戶點擊「不好笑」按鈕後，下一次 App 顯示的笑話結尾將自動加上 20 個「哈」字和 10 個「笑到流淚」的 emoji (😂)
   - 這個效果僅持續一次，再下一個笑話恢復正常

3. **取得新笑話**：
   - 提供「換一個笑話」按鈕，讓用戶可以隨時呼叫 Gemini API 以獲取新的笑話
   - 實現了防重複機制，避免連續顯示相同笑話
   - 包含笑話歷史記錄管理（最多50個笑話）

4. **智能防重複系統**：
   - 使用多種隨機化策略：時間戳、隨機種子、會話ID、主題變化
   - 檢測到重複笑話時自動重試，使用不同的提示詞
   - 歷史記錄過大時自動清理

5. **多樣化提示詞系統**：
   - 6種不同的提示詞變體，確保笑話的多樣性
   - 每次請求都包含隨機參數，避免 API 快取
   - 主題隨機選擇，增加內容變化

## 3. 技術架構與實現

### ✅ 已實現的技術架構：

*   **架構**：MVVM (Model-View-ViewModel) ✅
*   **UI**：Jetpack Compose ✅
*   **資料來源**：Google Gemini API (`gemini-1.5-flash` 模型) ✅
*   **語言**：Kotlin ✅
*   **非同步處理**：Kotlin Coroutines + Flow ✅
*   **依賴注入**：手動注入模式 ✅
*   **測試**：JUnit 5, MockK/Mockito ✅

### ✅ 已實現的檔案結構：

```
storyteller/
├── data/
│   └── repository/
│       └── JokeRepository.kt   ✅ // 負責處理 Gemini API 呼叫
│
├── ui/
│   ├── theme/                  ✅ // Compose UI 主題
│   ├── joke/
│   │   ├── JokeScreen.kt       ✅ // 主要的 Composable UI 畫面
│   │   └── JokeViewModel.kt    ✅ // 對應 JokeScreen 的 ViewModel
│   └── state/
│       └── JokeUiState.kt      ✅ // 定義 UI 狀態 (Initial, Loading, Success, Error)
│
└── MainActivity.kt             ✅ // App 進入點
```

## 4. 核心組件實作細節

### a. Model / Repository ✅

*   **`JokeRepository.kt`**:
    *   包含一個 `GenerativeModel` 實例，使用 `gemini-1.5-flash` 模型
    *   提供 `getJoke(prompt: String): Result<String>` 函式
    *   處理 API 例外，回傳 `Result` 型別
    *   實現隨機參數機制避免 API 快取

### b. ViewModel ✅

*   **`JokeViewModel.kt`**:
    *   注入 `JokeRepository`
    *   使用 `MutableStateFlow<JokeUiState>` 管理 UI 狀態
    *   **`getNewJoke()`**: 在 Coroutine 中呼叫 `jokeRepository.getJoke()`
        *   呼叫前將 state 設為 `JokeUiState.Loading`
        *   根據 Repository 回傳的 `Result` 更新狀態
        *   實現防重複機制和懲罰系統
    *   **`onNotFunnyClicked()`**: 將 `addPunishment` 旗標設為 true
    *   **智能提示詞建構**: 使用多種變體和隨機化策略

### c. View ✅

*   **`JokeScreen.kt`**:
    *   Composable 函式，負責渲染整體 UI
    *   使用 `collectAsState()` 訂閱 ViewModel 的 `StateFlow`
    *   根據 `JokeUiState` 的不同狀態顯示對應的 UI：
        *   `Loading`: 顯示 `CircularProgressIndicator`
        *   `Success`: 顯示英文笑話和中文翻譯
        *   `Error`: 顯示錯誤訊息
        *   `Initial`: 顯示歡迎訊息
    *   UI 佈局：使用 `Column`、`Row`、`Text`、`Button` 等元件

## 5. 未來開發計劃

### 🔄 下一階段功能：

1. **笑話分類系統**：
   - 實現用戶可選擇的笑話分類
   - 根據分類調整傳送給 Gemini API 的 prompt

2. **語音朗讀功能**：
   - 加入朗讀按鈕，使用 Android TTS 引擎
   - 用平淡、沒有感情的語氣念出笑話，增強「冷」的效果

3. **分享功能**：
   - 用戶可以將當前笑話以文字形式分享到其他 App

4. **「冷度」評分系統**：
   - 在每個笑話下方，讓用戶用 1 到 5 個雪花 (❄️) 評價笑話的「冷度」
   - 統計每個笑話的平均冷度

5. **Material Design 3 優化**：
   - 採用 Material Design 3 的最新設計語言
   - 加入動態色彩 (Dynamic Color)
   - 在切換笑話時加入淡入淡出或滑動動畫
   - 當「不好笑」懲罰機制觸發時，用趣味的動畫方式呈現

## 6. 測試策略

### ✅ 已實現的測試：

*   **單元測試 (Unit Tests)**：
    *   **目標**：`JokeViewModel` 和 `JokeRepository`
    *   **內容**：測試業務邏輯、狀態轉換、API 結果處理等
    *   **工具**：JUnit 5, MockK

### 🔄 計劃中的測試：

*   **UI 測試 (Instrumentation Tests)**：
    *   **目標**：`JokeScreen.kt`
    *   **內容**：驗證在不同 `JokeUiState` 下 UI 的呈現是否正確
    *   **工具**：Compose Test Rule, Espresso

## 7. 專案狀態總結

### ✅ 已完成 (100%)：
- 核心 MVVM 架構
- Gemini API 整合
- 雙語笑話顯示
- 用戶反饋機制
- 防重複系統
- 基礎 UI 實現
- 單元測試框架

### 🔄 進行中 (0%)：
- 無

### 📋 待開發 (0%)：
- 笑話分類系統
- 語音朗讀功能
- 分享功能
- 冷度評分系統
- UI/UX 優化
- 動畫效果

## 8. 部署與維護

### ✅ 已完成：
- Git 版本控制設置
- API Key 安全保護（使用 local.properties 和 .gitignore）
- 專案文檔（README.md）
- 範例配置文件（local.properties.example）

### 🔄 下一步：
- 推送到遠程 Git 倉庫
- 持續集成/持續部署 (CI/CD) 設置
- 應用程式發布準備

---

**專案進度：核心功能 100% 完成，已準備好進行功能擴展和 UI/UX 優化**