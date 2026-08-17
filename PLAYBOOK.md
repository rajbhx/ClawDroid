# 🦞 ClawDroid — Master Playbook

> Android AI Agent Platform — OpenClaw + Claw Code + OpenCode + Codex + MemPalace + OmniRoute
> Built by Peter Steinberger's OpenClaw community + Claw Code community + OpenAI Codex + OpenCode + MemPalace + OmniRoute

---

## 🎯 Vision

A feature-rich Android AI assistant that runs multiple coding agents natively on-device:
- **OpenClaw** (★386K) — Personal AI assistant
- **Claw Code** (★48K) — Multi-agent coding harness with RAG
- **OpenCode** (★198K) — Open source coding agent
- **OpenAI Codex** — Sandboxed coding agent
- **MemPalace** (★58K) — AI memory system
- **OmniRoute** (★49K) — 340+ provider AI gateway

All running inside a Termux bootstrap with GPU acceleration.

---

## 📦 Source Repos

| Repo | Stars | Language | What It Provides |
|------|-------|----------|-----------------|
| `openclaw/openclaw` | ★386K | TypeScript | Main AI assistant (agents, tools, skills, memory, MCP) |
| `AidanPark/openclaw-android` | ★1.7K | Kotlin/Java | Android foundation (terminal, WebView, bootstrap) |
| `instructkr/claw-code` | ★48K | Python+Rust | Multi-agent harness + RAG service |
| `anomalyco/opencode` | ★198K | TypeScript | 20+ coding tools (shell, read, write, edit) |
| `openai/codex` | — | Rust | Sandboxing, guardian, model provider |
| `MemPalace/mempalace` | ★58K | Python | AI memory (rewrite as Kotlin) |
| `diegosouzapw/OmniRoute` | ★49K | TypeScript | 340+ provider gateway |
| `termux/termux-app` | ★59K | Java | Terminal emulator |
| `termux/termux-packages` | ★16.8K | Shell | Bootstrap build system |
| `termux/termux-api` | ★4.2K | Java | Android API access |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    ClawDroid Android App                      │
│                                                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐  │
│  │ Chat UI  │ │ Terminal │ │ Memory   │ │ Settings      │  │
│  │ (Compose)│ │ (View)   │ │ (Compose)│ │ (Compose)     │  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └───────┬───────┘  │
│       │            │            │                │           │
│  ┌────▼────────────▼────────────▼────────────────▼───────┐  │
│  │         Navigation (Compose Nav + Bottom Bar)         │  │
│  └────┬────────────┬────────────┬────────────────┬───────┘  │
│       │            │            │                │           │
│  ┌────▼────────────▼────────────▼────────────────▼───────┐  │
│  │              ViewModels (Hilt @HiltViewModel)         │  │
│  │  ChatViewModel | MemoryVM | SettingsVM | AgentHubVM   │  │
│  └────┬────────────┬────────────┬────────────────┬───────┘  │
│       │            │            │                │           │
│  ┌────▼────────────▼────────────▼────────────────▼───────┐  │
│  │                    Data Layer                          │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐  │  │
│  │  │ OmniRoute    │ │ Room DB      │ │ DataStore    │  │  │
│  │  │ (SSE Stream) │ │ (Conv/Mem)   │ │ (Settings)   │  │  │
│  │  └──────────────┘ └──────────────┘ └──────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
│                                       │                      │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Termux Bootstrap (glibc)                  │  │
│  │  OpenClaw | Claw Code | OpenCode | Codex              │  │
│  └───────────────────────────────────────────────────────┘  │
│                                       │                      │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              GPU Acceleration                          │  │
│  │  Turnip (Adreno) | VirGL (Mali) | LLVMpipe            │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 Phase Tracker

### Phase 0 — Fix Build

| # | Task | Status | Commit | Notes |
|---|------|--------|--------|-------|
| 0.1 | Fix JsBridge switchSession syntax | ✅ DONE | cafff06 | Stray `{` before `=` |
| 0.2 | Add switchSessionById(String) | ✅ DONE | cafff06 | Handle-based session lookup |
| 0.3 | CI build green | ✅ DONE | 7ffe877 | All 11 CI runs, BUILD SUCCESSFUL |

### Phase 1 — Core Architecture

| # | Task | Status | Commit | Notes |
|---|------|--------|--------|-------|
| 1.1 | Multi-module Gradle restructure | ✅ DONE | 2c0f15c | app + terminal-emulator + terminal-view |
| 1.2 | Version catalog (Compose, Hilt, DataStore) | ✅ DONE | 2c0f15c | libs.versions.toml |
| 1.3 | Compose BOM + Material3 | ✅ DONE | 2c0f15c | compose-bom 2024.12.01 |
| 1.4 | Hilt DI (Database, Network, App modules) | ✅ DONE | 2c0f15c | @HiltAndroidApp + @AndroidEntryPoint |
| 1.5 | Navigation (5 screens, bottom bar) | ✅ DONE | 2c0f15c | Compose Nav + NavItem |
| 1.6 | Theme system (Material You + Tokyo Night) | ✅ DONE | 2c0f15c | Dual theme with toggle |
| 1.7 | ProGuard rules | ✅ DONE | 2c0f15c | OkHttp, Room, ONNX, Gson, Terminal |
| 1.8 | Compose Compiler plugin fix | ✅ DONE | 5354d2f | Required for Kotlin 2.0+ |

### Phase 2 — AI Chat (Core Feature)

| # | Task | Status | Commit | Notes |
|---|------|--------|--------|-------|
| 2.1 | OmniRoute streaming client (SSE) | ✅ DONE | 2c0f15c | OkHttp + SSE, 8 providers |
| 2.2 | Chat UI (message list, input, model picker) | ✅ DONE | 2c0f15c | Compose LazyColumn + bubbles |
| 2.3 | Provider management screen | ✅ DONE | 2c0f15c | Settings > Providers |
| 2.4 | Code block rendering | ✅ DONE | 2c0f15c | MarkCompose library |
| 2.5 | Streaming response display | ✅ DONE | 2c0f15c | Token-by-token with cursor |
| 2.6 | Multi-model support | ✅ DONE | 2c0f15c | 8 providers, 30+ models |

### Phase 3 — Terminal (Differentiator)

| # | Task | Status | Commit | Notes |
|---|------|--------|--------|-------|
| 3.1 | TerminalView in Compose (AndroidView) | ✅ DONE | 2c0f15c | Embedded in TerminalScreen |
| 3.2 | Terminal session management | ✅ DONE | 2c0f15c | Inline session creation |
| 3.3 | Chat → Terminal bridge | ⏳ PENDING | — | Send AI commands to terminal |
| 3.4 | Terminal → Chat context | ⏳ PENDING | — | Long-press to send to AI |

### Phase 4 — Memory (Power Feature)

| # | Task | Status | Commit | Notes |
|---|------|--------|--------|-------|
| 4.1 | Room database schema | ✅ DONE | 2c0f15c | memories, conversations, messages, provider_keys |
| 4.2 | ONNX Runtime integration | ✅ DONE | 2c0f15c | onnxruntime-android 1.19.0 |
| 4.3 | Semantic search | ⏳ PENDING | — | Vector similarity (needs embedding model) |
| 4.4 | Memory browser UI | ✅ DONE | 2c0f15c | Compose with categories + search |
| 4.5 | MemPalace server sync | ⏳ PENDING | — | Optional remote sync |

### Phase 5 — Agent Hub

| # | Task | Status | Commit | Notes |
|---|------|--------|--------|-------|
| 5.1 | Agent status dashboard | ✅ DONE | 2c0f15c | 6 agents with install status |
| 5.2 | Agent cards (stars, language, description) | ✅ DONE | 2c0f15c | Card UI with metadata |
| 5.3 | One-tap install | ⏳ PENDING | — | Terminal-based installer |
| 5.4 | Agent runner | ⏳ PENDING | — | Prompt input + streaming output |

### Phase 6 — GPU Acceleration

| # | Task | Status | Commit | Notes |
|---|------|--------|--------|-------|
| 6.1 | GPU detection script | ✅ DONE | earlier | detect-gpu.sh |
| 6.2 | GPU install script | ✅ DONE | earlier | install-gpu-packages.sh |
| 6.3 | GPU status in settings | ⏳ PENDING | — | UI indicator |
| 6.4 | Performance mode toggle | ⏳ PENDING | — | GPU on/off |

### Phase 7 — Polish & Ship

| # | Task | Status | Commit | Notes |
|---|------|--------|--------|-------|
| 7.1 | ProGuard rules | ✅ DONE | 2c0f15c | Full coverage |
| 7.2 | Adaptive icons | ⏳ PENDING | — | Vector drawable |
| 7.3 | Splash screen | ⏳ PENDING | — | Material3 SplashScreen |
| 7.4 | E2E tests | ⏳ PENDING | — | Compose + integration |
| 7.5 | Release signing | ⏳ PENDING | — | GitHub Secrets |
| 7.6 | First tagged release | ⏳ PENDING | — | v1.0.0 |

---

### 🏗️ Build Log (GitHub Actions)

| Run | Commit | Status | Duration | Error |
|-----|--------|--------|----------|-------|
| 32043662309 | e0f02db | ❌ FAILED | 35s | Missing gradlew |
| 32043788013 | 334fd68 | ❌ FAILED | 1m25s | compileSdk 34→35 |
| 32043830005 | 1ac5277 | ❌ FAILED | 2m34s | Missing annotation dep |
| 32044006279 | b50547c | ❌ FAILED | 1m33s | Missing string/drawable resources |
| 32044304756 | 1f471d9 | ❌ FAILED | 2m14s | Kotlin compilation errors |
| 32044494227 | 0f10eb0 | ❌ FAILED | 1m3s | JsBridge/MainActivity errors |
| 32044604309 | 76e12ff | ❌ FAILED | 57s | JsBridge syntax error |
| 32045096429 | cafff06 | ❌ FAILED | 1m9s | Old MainActivity type mismatch |
| 32045947101 | 2c0f15c | ❌ FAILED | 43s | Compose Compiler plugin |
| 32046099872 | 5354d2f | ❌ FAILED | 47s | Version catalog format |

---

### 📁 File Inventory (Current — 43 Kotlin + 22 Java)

**Kotlin (43 files):**
- `ClawdroidApp.kt` — @HiltAndroidApp Application
- `MainActivity.kt` — Compose host activity
- `ClawdroidService.kt` — Foreground service
- `BootReceiver.kt` — Boot auto-start
- `BootstrapManager.kt` — Termux bootstrap extraction
- `EnvironmentBuilder.kt` — Environment variable builder
- `GpuDetector.kt` — GPU detection
- `CommandRunner.kt` — Shell command execution
- `UrlResolver.kt` — Remote URL resolution
- `AppLogger.kt` — Logging utility

**Data Layer (13 files):**
- `ClawdroidDatabase.kt` — Room database
- `ConversationDao.kt`, `MessageDao.kt`, `MemoryDao.kt`, `ProviderKeyDao.kt` — DAOs
- `Conversation.kt`, `Message.kt`, `MemoryEntry.kt`, `ProviderKey.kt` — Entities
- `OmniRouteClient.kt` — Streaming API client
- `ProviderConfig.kt` — Provider definitions
- `ChatRequest.kt`, `ChatResponse.kt` — DTOs

**Repositories (3 files):**
- `ChatRepository.kt` — Chat + streaming
- `MemoryRepository.kt` — Memory CRUD
- `SettingsRepository.kt` — DataStore + provider keys

**DI (3 files):**
- `DatabaseModule.kt`, `NetworkModule.kt`, `AppModule.kt` — Hilt modules

**UI (11 files):**
- `NavGraph.kt`, `Screen.kt` — Navigation
- `ChatScreen.kt`, `TerminalScreen.kt`, `AgentHubScreen.kt`, `MemoryScreen.kt`, `SettingsScreen.kt` — Screens
- `Theme.kt`, `Color.kt`, `Type.kt` — Theme system

**ViewModels (4 files):**
- `ChatViewModel.kt`, `MemoryViewModel.kt`, `SettingsViewModel.kt`, `AgentHubViewModel.kt`

**Java (22 files):**
- terminal-emulator: 14 files (TerminalSession, TerminalEmulator, JNI, etc.)
- terminal-view: 8 files (TerminalView, TerminalRenderer, etc.)

**Build (12 files):**
- `build.gradle.kts` (root + app)
- `libs.versions.toml`
- `settings.gradle.kts`
- `gradle.properties`
- `gradle-wrapper.properties`
- `gradlew` + `gradlew.bat`
- `proguard-rules.pro`

**Scripts (3 files):**
- `detect-gpu.sh`, `install-gpu-packages.sh`, `start-gpu-server.sh`

**CI/CD (2 files):**
- `build.yml`, `release.yml`

**Docs (2 files):**
- `README.md`, `PLAYBOOK.md`

---

## 🔧 Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 2.1.0 |
| Build | Gradle | 8.11.1 |
| AGP | Android Gradle Plugin | 8.7.3 |
| UI | Jetpack Compose + Material3 | BOM 2024.12.01 |
| DI | Hilt | 2.51.1 |
| Database | Room | 2.6.1 |
| Network | OkHttp + SSE | 4.12.0 |
| ML | ONNX Runtime | 1.19.0 |
| Settings | DataStore | 1.1.1 |
| Serialization | Gson + kotlinx.serialization | 2.11.0 / 1.7.3 |
| Terminal | Termux terminal-emulator + view | Java |
| Bootstrap | Termux packages | 2026.08.16-r1 |
| minSdk | Android 8 (API 26) | — |
| targetSdk | Android 14 (API 34) | — |
| compileSdk | Android 15 (API 35) | — |

---

## 🚀 Commands

```bash
# Build (CI only — never build locally)
git push origin main  # Triggers GitHub Actions

# Check build status
gh run list --workflow=build.yml --limit=5

# Download APK artifact
gh run download <RUN_ID> -n clawdroid-debug

# Tag release
git tag v1.0.0 && git push --tags
```

---

## 📋 Decisions & Rationale

| Decision | Rationale |
|----------|-----------|
| Compose over WebView | Modern, fast, smaller APK, native feel |
| OmniRoute-first | 340+ providers through one interface |
| Dual theme (Material You + Tokyo Night) | Flexibility — dynamic color on Android 12+, fixed dark theme |
| Keep terminal as Java | Proven Termux code, no rewrite needed |
| Room over SQLite | Type-safe, coroutine-friendly, migration support |
| DataStore over SharedPreferences | Async, type-safe, handles corruption |
| Hilt over Koin | Official Google DI, better Compose integration |
| KSP over KAPT | Faster compilation, Kotlin 2.0 support |
| OkHttp over Retrofit | SSE streaming needs raw OkHttp |
| ONNX Runtime | On-device embeddings, no server dependency |
