# 🦞 ClawDroid — Master Playbook

> **Android AI Agent Platform** — Termux Bootstrap + AI Agents + GPU Acceleration
> **Core: Termux bootstrap first boot → Terminal → AI agents**

---

## 🎯 Architecture

```
First Boot:  App → BootstrapScreen → Download 31MB → Extract → Install Packages → Dashboard
Normal Boot: App → Dashboard → Terminal / Agents / Memory / Settings
Terminal:    TerminalView → TerminalSession → Termux Shell (bash/sh)
AI:          OmniRoute → 340+ providers → SSE streaming
Memory:      Room DB → ONNX embeddings → Semantic search
```

---

## 📊 Phase Tracker

### Phase 1 — Core (DONE ✅)
- [x] Termux bootstrap download + extract on first boot
- [x] BootstrapScreen with animated progress bar
- [x] TerminalView with proper lifecycle (DisposableEffect)
- [x] Session cleanup on dispose
- [x] Input bar for typing commands
- [x] EnvironmentBuilder (PREFIX, HOME, PATH, LD_LIBRARY_PATH)
- [x] PackageInstaller (git, curl, wget, nodejs, python, cmake)

### Phase 2 — AI Chat (DONE ✅)
- [x] OmniRoute streaming client (SSE, 340+ providers)
- [x] Chat UI with model picker (8 providers, 30+ models)
- [x] Free models: Groq, Gemini, Ollama, DeepSeek
- [x] Conversation history (Room DB)
- [x] Provider management

### Phase 3 — Dashboard (DONE ✅)
- [x] System status (GPU, bootstrap, agents, memory)
- [x] Quick action cards
- [x] Free models list
- [x] Recent activity

### Phase 4 — Memory (DONE ✅)
- [x] Room database (conversations, messages, memories, provider_keys)
- [x] ONNX Runtime embeddings
- [x] Semantic search (cosine similarity)
- [x] Memory browser with categories
- [x] MemPalace server sync

### Phase 5 — Agent Hub (DONE ✅)
- [x] 6 agents (OpenClaw, Claw Code, OpenCode, Codex, MemPalace, OmniRoute)
- [x] One-tap install with progress bar
- [x] Agent runner with terminal output
- [x] Install steps display

### Phase 6 — GPU (DONE ✅)
- [x] GPU detection (Turnip/VirGL/LLVMpipe)
- [x] GPU status in settings
- [x] Acceleration toggle

### Phase 7 — Polish (DONE ✅)
- [x] Adaptive icons
- [x] Splash screen
- [x] ProGuard rules
- [x] Dual theme (Material You + Tokyo Night)
- [x] Theme switching (DataStore → Compose)
- [x] CI: 3 parallel workflows

---

## 🔧 Tech Stack

| Component | Version |
|-----------|---------|
| Kotlin | 2.1.0 |
| Compose | BOM 2024.12.01 |
| Hilt | 2.51.1 |
| Room | 2.6.1 |
| OkHttp SSE | 4.12.0 |
| ONNX Runtime | 1.19.0 |
| Termux Bootstrap | 2026.08.16-r1 |
| minSdk | 26 (Android 8+) |
| compileSdk | 35 |

---

## 🚀 Commands

```bash
git push origin main          # triggers CI
gh run list --limit=5         # check CI
git tag v1.0.0 && git push --tags  # release
```
