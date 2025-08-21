# 冷笑話大師 (Cold Joke Teller) App 開發計畫 (v2)

## 1. 專案目標

開發一款 Android 原生應用程式，旨在為生活苦悶的用戶提供輕鬆的娛樂。此 App 將以講冷笑話為核心，並根據用戶的反應動態調整互動方式，創造更有趣、更具互動性的體驗。專案將嚴格遵循 MVVM 架構，**使用 Google Gemini API 作為笑話的動態來源**，使用 Jetpack Compose 進行 UI 開發，並包含完整的單元測試，確保程式碼品質與穩定性。

## 2. 核心功能

1.  **顯示冷笑話**：
    *   App 啟動時，或用戶點擊「換一個笑話」按鈕時，透過 Gemini API 產生一則隨機的冷笑話並顯示在畫面上。

2.  **用戶反饋機制**：
    *   畫面上提供一個「不好笑」按鈕。
    *   當用戶點擊「不好笑」按鈕後，下一次 App 顯示的笑話結尾將自動加上 20 個「哈」字和 10 個「笑到流淚」的 emoji (😂)。
    *   這個效果僅持續一次，再下一個笑話恢復正常。

3.  **取得新笑話**：
    *   提供一個按鈕，讓用戶可以隨時呼叫 Gemini API 以獲取新的笑話。

## 3. 創意與附加功能

1.  **笑話分類**：
    *   提供多種笑話分類，例如「諧音梗」、「工程師梗」、「生活觀察」等。這將透過修改傳送給 Gemini API 的 prompt 來實現。

2.  **語音朗讀功能**：
    *   加入一個朗讀按鈕，使用 Android 的 TTS (Text-to-Speech) 引擎將笑話用平淡、沒有感情的語氣念出來，增強「冷」的效果。

3.  **分享功能**：
    *   用戶可以將當前看到的笑話以文字形式分享到其他 App (如 LINE, Messenger)。

4.  **「冷度」評分系統**：
    *   在每個笑話下方，讓用戶可以用 1 到 5 個雪花 (❄️) 來評價這個笑話的「冷度」，並簡單統計每個笑話的平均冷度。

5.  **Material Design 3 與動畫**：
    *   UI 將採用 Material Design 3 的最新設計語言，包含動態色彩 (Dynamic Color)。
    *   在切換笑話時加入簡單的淡入淡出或滑動動畫，並處理 `Loading` 狀態的 UI 顯示（例如顯示 `CircularProgressIndicator`）。
    *   當「不好笑」的懲罰機制觸發時，20 個「哈」字和 emoji 可以用趣味的動畫方式（例如逐字彈出）呈現。

## 4. 技術架構與選型

*   **架構**：MVVM (Model-View-ViewModel)
*   **UI**：Jetpack Compose
*   **資料來源**：**Google Gemini API (`gemini-1.5-flash` 模型)**
*   **語言**：Kotlin
*   **非同步處理**：Kotlin Coroutines + Flow
*   **依賴注入**：Hilt (或手動注入，參照現有 `BakingViewModel` 模式)
*   **測試**：JUnit 5, MockK/Mockito

## 5. 檔案結構 (基於 `app/src/main/java/com/example/storyteller/`)

```
storyteller/
├── data/
│   └── repository/
│       └── JokeRepository.kt   // 負責處理 Gemini API 呼叫
│
├── di/
│   └── AppModule.kt           // Hilt 依賴注入模組 (如果使用 Hilt)
│
├── ui/
│   ├── theme/                  // Compose UI 主題
│   ├── joke/
│   │   ├── JokeScreen.kt       // 主要的 Composable UI 畫面
│   │   └── JokeViewModel.kt    // 對應 JokeScreen 的 ViewModel
│   └── state/
│       └── JokeUiState.kt      // 定義 UI 狀態 (Initial, Loading, Success, Error)
│
└── MainActivity.kt             // App 進入點
```
*（註：為使結構清晰，建議將笑話功能相關的 UI 檔案放在 `ui/joke` 子目錄下，狀態檔案放在 `ui/state` 下）*

## 6. 核心組件實作細節

### a. Model / Repository

*   **`JokeRepository.kt`**:
    *   將包含一個 `GenerativeModel` 實例（類似於 `BakingViewModel`）。
    *   提供一個 suspend 函式 `getJoke(prompt: String): Result<String>`。
    *   此函式將呼叫 `generativeModel.generateContent()`，並將 prompt 作為輸入。
    *   它會處理 API 可能發生的例外，並回傳 `Result` 型別（`Result.success` 或 `Result.failure`）。

### b. ViewModel

*   **`JokeViewModel.kt`**:
    *   注入 `JokeRepository`。
    *   使用 `MutableStateFlow<JokeUiState>` 向 UI 暴露當前狀態，`JokeUiState` 將沿用現有的 `UiState` 密封介面模式。
    *   **`getNewJoke()`**: 在 Coroutine 中呼叫 `jokeRepository.getJoke()`。
        *   呼叫前，將 state 設為 `JokeUiState.Loading`。
        *   根據 Repository 回傳的 `Result`，將 state 更新為 `JokeUiState.Success` 或 `JokeUiState.Error`。
        *   在 `Success` 狀態下，檢查 `extraPunishment` 旗標，如果為 true，則在笑話結尾附加文字並重設旗標。
    *   **`onNotFunnyClicked()`**: 將 `extraPunishment` 旗標設為 true。
    *   **Prompt Engineering**: ViewModel 將負責建構傳送給 Gemini API 的 prompt，例如：「`請用繁體中文說一個簡短的冷笑話`」。

### c. View

*   **`JokeScreen.kt`**:
    *   一個 Composable 函式，負責渲染整體 UI。
    *   使用 `collectAsStateWithLifecycle()` 訂閱 ViewModel 的 `StateFlow`。
    *   根據 `JokeUiState` 的不同狀態顯示對應的 UI：
        *   `Loading`: 顯示 `CircularProgressIndicator`。
        *   `Success`: 顯示笑話內容。
        *   `Error`: 顯示錯誤訊息。
        *   `Initial`: 顯示歡迎訊息或提示。
    *   UI 佈局：使用 `Column`、`Row`、`Text`、`Button` 等 Composable 元件。

## 7. 開發步驟 (Milestones)

1.  **專案結構調整**：依照計畫建立新的檔案與目錄 (`joke`, `state` 等)。
2.  **Repository 層開發**：建立 `JokeRepository.kt`，並在其中實作呼叫 Gemini API 的邏輯。
3.  **ViewModel 層開發**：建立 `JokeViewModel.kt` 和 `JokeUiState.kt`，並實作核心邏輯（呼叫 Repository、管理 UI 狀態、處理「不好笑」點擊）。
4.  **撰寫單元測試**：為 `JokeViewModel` 和 `JokeRepository` 撰寫單元測試。對於 Repository，可以使用 MockK 來模擬 `GenerativeModel` 的行為。
5.  **View 層開發**：使用 Jetpack Compose 建立 `JokeScreen.kt`，將 UI 與 ViewModel 連接，並處理各種 UI 狀態的顯示。
6.  **整合與測試**：修改 `MainActivity.kt` 以載入 `JokeScreen`，運行 App 進行手動測試。
7.  **附加功能開發**：逐步實現語音朗讀、分享、評分等創意功能。
8.  **UI/UX 優化**：加入動畫效果，調整佈局與顏色，確保符合 Material Design 規範。

## 8. 測試策略

*   **單元測試 (Unit Tests)**：
    *   **目標**：`JokeViewModel` 和 `JokeRepository`。
    *   **內容**：測試業務邏輯、狀態轉換、Prompt 建構、API 結果處理等。
    *   **工具**：JUnit 5, MockK (用以模擬 `GenerativeModel`)。

*   **UI 測試 (Instrumentation Tests)**：
    *   **目標**：`JokeScreen.kt`。
    *   **內容**：驗證在不同 `JokeUiState` 下 UI 的呈現是否正確（例如 Loading 是否顯示、錯誤訊息是否出現）。
    *   **工具**：Compose Test Rule, Espresso。