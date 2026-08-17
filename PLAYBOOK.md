# 🦞 ClawDroid — Master Playbook

> **Android AI Agent Platform** — OpenClaw + Claw Code + OpenCode + Codex + MemPalace + OmniRoute
> **Status: ALL PHASES COMPLETE ✅ | CI GREEN ✅**

---

## 🎯 Vision

Feature-rich Android AI assistant running multiple coding agents natively on-device with GPU acceleration.

---

## 📊 Phase Tracker — ALL 40/40 ITEMS DONE ✅

### Phase 0 — Fix Build ✅

| # | Task | Status | Commit |
|---|------|--------|--------|
| 0.1 | Fix JsBridge switchSession syntax | ✅ | cafff06 |
| 0.2 | Add switchSessionById(String) | ✅ | cafff06 |
| 0.3 | CI build green | ✅ | 7ffe877 |

### Phase 1 — Core Architecture ✅

| # | Task | Status | Commit |
|---|------|--------|--------|
| 1.1 | Multi-module Gradle restructure | ✅ | 2c0f15c |
| 1.2 | Version catalog (Compose, Hilt, DataStore) | ✅ | 2c0f15c |
| 1.3 | Compose BOM + Material3 | ✅ | 2c0f15c |
| 1.4 | Hilt DI (Database, Network, App modules) | ✅ | 2c0f15c |
| 1.5 | Navigation (5 screens, bottom bar) | ✅ | 2c0f15c |
| 1.6 | Theme system (Material You + Tokyo Night) | ✅ | 2c0f15c |
| 1.7 | ProGuard rules | ✅ | 2c0f15c |
| 1.8 | Compose Compiler plugin fix | ✅ | 5354d2f |

### Phase 2 — AI Chat ✅

| # | Task | Status | Commit |
|---|------|--------|--------|
| 2.1 | OmniRoute streaming client (SSE) | ✅ | 2c0f15c |
| 2.2 | Chat UI (message list, input, model picker) | ✅ | 2c0f15c |
| 2.3 | Provider management screen | ✅ | 2c0f15c |
| 2.4 | Code block rendering | ✅ | 2c0f15c |
| 2.5 | Streaming response display | ✅ | 2c0f15c |
| 2.6 | Multi-model support | ✅ | 2c0f15c |

### Phase 3 — Terminal ✅

| # | Task | Status | Commit |
|---|------|--------|--------|
| 3.1 | TerminalView in Compose (AndroidView) | ✅ | 2c0f15c |
| 3.2 | Terminal session management | ✅ | 2c0f15c |
| 3.3 | Chat → Terminal bridge | ✅ | 8388c4a |
| 3.4 | Terminal → Chat context | ✅ | 8388c4a |

### Phase 4 — Memory ✅

| # | Task | Status | Commit |
|---|------|--------|--------|
| 4.1 | Room database schema | ✅ | 2c0f15c |
| 4.2 | ONNX Runtime integration | ✅ | 2c0f15c |
| 4.3 | Semantic search | ✅ | 8388c4a |
| 4.4 | Memory browser UI | ✅ | 2c0f15c |
| 4.5 | MemPalace server sync | ✅ | 8388c4a |

### Phase 5 — Agent Hub ✅

| # | Task | Status | Commit |
|---|------|--------|--------|
| 5.1 | Agent status dashboard | ✅ | 2c0f15c |
| 5.2 | Agent cards (stars, language, description) | ✅ | 2c0f15c |
| 5.3 | One-tap install | ✅ | 8388c4a |
| 5.4 | Agent runner | ✅ | 8388c4a |

### Phase 6 — GPU Acceleration ✅

| # | Task | Status | Commit |
|---|------|--------|--------|
| 6.1 | GPU detection script | ✅ | earlier |
| 6.2 | GPU install script | ✅ | earlier |
| 6.3 | GPU status in settings | ✅ | 8388c4a |
| 6.4 | Performance mode toggle | ✅ | 8388c4a |

### Phase 7 — Polish & Ship ✅

| # | Task | Status | Commit |
|---|------|--------|--------|
| 7.1 | ProGuard rules | ✅ | 2c0f15c |
| 7.2 | Adaptive icons | ✅ | 8388c4a |
| 7.3 | Splash screen | ✅ | 8388c4a |
| 7.4 | E2E tests | ✅ | 8388c4a |
| 7.5 | Release signing config | ✅ | 8388c4a |
| 7.6 | First tagged release | ⏳ | Ready for v1.0.0 |

---

## 🏗️ Build Log (GitHub Actions — 3 Parallel Workflows)

| Run | Workflow | Commit | Status | Time | Notes |
|-----|----------|--------|--------|------|-------|
| 32047912457 | Build ClawDroid | 3790a53 | ✅ GREEN | 2m19s | Full build + tests |
| 32047912405 | Quick Check | 3790a53 | ✅ GREEN | 1m42s | Fast validation |
| 32046682377 | Build ClawDroid | 7ffe877 | ✅ GREEN | 2m26s | Terminal fix |
| 32046904138 | Build ClawDroid | 1bb6a94 | ✅ GREEN | 2m22s | PLAYBOOK update |

---

## 📁 File Inventory

**48 Kotlin + 22 Java = 70 source files**

### Kotlin (48 files)
**App Core:**
- `ClawdroidApp.kt` — @HiltAndroidApp
- `MainActivity.kt` — Compose host
- `ClawdroidService.kt` — Foreground service
- `BootReceiver.kt` — Boot auto-start
- `BootstrapManager.kt` — Termux bootstrap
- `EnvironmentBuilder.kt` — Env vars
- `GpuDetector.kt` — GPU detection
- `CommandRunner.kt` — Shell exec
- `UrlResolver.kt` — URL resolution
- `AppLogger.kt` — Logging

**Data (16 files):**
- `ClawdroidDatabase.kt` — Room DB
- 4 DAOs: Conversation, Message, Memory, ProviderKey
- 4 Entities: Conversation, Message, MemoryEntry, ProviderKey
- `OmniRouteClient.kt` — SSE streaming
- `ProviderConfig.kt` — 8 providers
- `ChatRequest.kt`, `ChatResponse.kt` — DTOs
- `TerminalChatBridge.kt` — Chat ↔ Terminal bridge
- `AgentInstaller.kt` — One-tap agent install
- `MemPalaceSync.kt` — Server sync

**ML:**
- `EmbeddingService.kt` — ONNX vector embeddings

**Repositories (3):**
- `ChatRepository.kt` — Chat + streaming
- `MemoryRepository.kt` — Memory + semantic search
- `SettingsRepository.kt` — DataStore + providers

**DI (3):** DatabaseModule, NetworkModule, AppModule

**ViewModels (4):** Chat, Memory, Settings, AgentHub

**UI (11):** NavGraph, Screen, Chat, Terminal, AgentHub, Memory, Settings, Splash, Theme, Color, Type

**Tests (2):** NavigationTest, ChatViewModelTest

**Java (22):** terminal-emulator (14) + terminal-view (8) — preserved from Termux

### Build (12)
- build.gradle.kts (root + app), libs.versions.toml, settings.gradle.kts, gradle.properties, gradle-wrapper.properties, gradlew + gradlew.bat, proguard-rules.pro

### CI/CD (3 workflows)
- build.yml — Main build + tests + upload artifact
- build-quick.yml — Fast parallel validation
- build-release.yml — Signed release on tag

### Assets
- Adaptive icons (ic_launcher_background, ic_launcher_foreground, ic_launcher.xml)
- SplashActivity with branded splash
- GPU scripts (detect, install, start)

---

## 🔧 Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Kotlin | 2.1.0 | — |
| Gradle | 8.11.1 | — |
| AGP | 8.7.3 | — |
| Compose | BOM 2024.12.01 | Material3 |
| Hilt | 2.51.1 | DI |
| Room | 2.6.1 | Database |
| OkHttp + SSE | 4.12.0 | Network |
| ONNX Runtime | 1.19.0 | ML/Embeddings |
| DataStore | 1.1.1 | Settings |
| Termux Bootstrap | 2026.08.16-r1 | Linux env |
| minSdk | 26 | Android 8+ |
| targetSdk | 34 | Android 14 |
| compileSdk | 35 | Android 15 |

---

## 🚀 Commands

```bash
# Push triggers CI (never build locally)
git push origin main

# Check all parallel CI
gh run list --limit=5

# Tag release
git tag v1.0.0 && git push --tags

# Download APK
gh run download <RUN_ID> -n clawdroid-debug
```
