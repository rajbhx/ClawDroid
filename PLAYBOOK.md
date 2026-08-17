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
| `Yeachan-Heo/oh-my-codex` | ★32K | TypeScript | OmX workflow layer |
| `code-yeongyu/oh-my-openagent` | — | TypeScript | Multi-agent coordination |
| `code-yeongyu/lazycodex` | ★3.2K | TypeScript | Production agent harness |
| `Yeachan-Heo/gajae-code` | ★2.5K | — | Gajae Code MVP |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    ClawDroid Android App                      │
│                                                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐  │
│  │ Chat UI  │ │ Terminal │ │ Memory   │ │ Settings      │  │
│  │ (React)  │ │ (Termux) │ │ (React)  │ │ (React)       │  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └───────┬───────┘  │
│       │            │            │                │           │
│  ┌────▼────────────▼────────────▼────────────────▼───────┐  │
│  │         EventBridge (Kotlin ↔ WebView ↔ Node.js)      │  │
│  └────┬────────────┬────────────┬────────────────┬───────┘  │
│       │            │            │                │           │
│  ┌────▼────────────▼────────────▼────────────────▼───────┐  │
│  │              Node.js Runtime (glibc bootstrap)         │  │
│  │                                                        │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐  │  │
│  │  │ OpenClaw     │ │ Claw Code    │ │ OpenCode     │  │  │
│  │  │ (651+ files) │ │ (3 components)│ │ (20+ tools)  │  │  │
│  │  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘  │  │
│  │         │                │                 │           │  │
│  │  ┌──────▼────────────────▼─────────────────▼───────┐  │  │
│  │  │  OmniRoute Provider Router (340+ providers)     │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  MemPalace Memory (Kotlin Room + ONNX — native)        │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Termux Terminal (Java — GPU: Turnip/VirGL/Mesa)       │  │
│  └────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Problems & Solutions

### P1: Bootstrap Compatibility
**Problem**: Android uses Bionic libc, but Node.js/Python packages need glibc
**Solution**: Use glibc linker from termux-packages (ld-linux-aarch64.so.1) + glibc-compat.js shim
**Status**: ✅ Solved by openclaw-android

### P2: Termux Path Hardcoding
**Problem**: Termux packages have hardcoded `/data/data/com.termux/` paths
**Solution**: libtermux-exec intercepts execve, rewrites paths. SYMLINKS.txt replaces com.termux → app package.
**Status**: ✅ Solved by openclaw-android

### P3: GPU Acceleration
**Problem**: Android apps can't directly access GPU for Linux binaries
**Solution**: Turnip (Adreno 610+), VirGL (Mali), LLVMpipe (fallback) via Mesa
**Performance**: GLMARK2 — Turnip: 198, LLVMpipe: 93, VirGL: 75
**Status**: 🔧 To implement

### P4: Multi-Agent Coordination
**Problem**: Multiple agents need to coordinate without context window pollution
**Solution**: clawhip (event router) keeps monitoring outside agent context. OmO provides Architect/Executor/Reviewer pattern.
**Status**: 🔧 To integrate

### P5: Memory Persistence
**Problem**: Agent conversations need long-term memory
**Solution**: MemPalace pattern — wing→room→drawer hierarchy with semantic search. Rewrite as Kotlin Room + ONNX.
**Status**: 🔧 To implement

### P6: Provider Routing
**Problem**: Single provider failures break the experience
**Solution**: OmniRoute pattern — 340+ providers with auto-fallback, quota tracking, token compression
**Status**: 🔧 To implement

### P7: Safe Tool Execution
**Problem**: Agent tools can be destructive
**Solution**: Codex guardian pattern — approval flow for dangerous operations. Claw Code PermissionMode for read-only defaults.
**Status**: 🔧 To implement

### P8: RAG for Code + Life
**Problem**: Agents need context beyond current conversation
**Solution**: Claw Code claw-rag-service — SQLite + embeddings, HTTP API. Work RAG + Personal RAG separation.
**Status**: 🔧 To integrate

### P9: Proactive AI
**Problem**: Agents are reactive, not proactive
**Solution**: OmX-style scheduled loops — check inbox, extract tasks, draft responses, post digest
**Status**: 🔧 Future

### P10: Voice Interface
**Problem**: Terminal-only interaction limits mobile use
**Solution**: STT input + TTS output via Android APIs. Chat bridge for Discord/Telegram.
**Status**: 🔧 Future

---

## 🗺️ Roadmap

### Phase 1 — Foundation (CURRENT)
- [ ] Fork openclaw-android → ClawDroid
- [ ] Update bootstrap to termux-packages 2026.08.16-r1
- [ ] Set up project structure (Gradle, modules)
- [ ] Install OpenClaw agent in bootstrap
- [ ] Install Claw Code (3 components) in bootstrap
- [ ] Install OpenCode tools in bootstrap
- [ ] Ship OpenAI Codex binary
- [ ] Build AgentBridge (Kotlin ↔ WebView ↔ Node.js)
- [ ] Build ChatScreen (React) with streaming
- [ ] Build MemoryStore (Kotlin Room + ONNX)
- [ ] GitHub Actions CI
- [ ] Verify: all agents work in terminal

### Phase 2 — GPU + Multi-Agent
- [ ] GPU detection (Adreno/Mali/other)
- [ ] GPU package install (Turnip/VirGL/Mesa)
- [ ] GPU server lifecycle
- [ ] OmX workflow integration
- [ ] clawhip event router
- [ ] OmO Architect/Executor/Reviewer
- [ ] Claw Code RAG service integration

### Phase 3 — Features + Polish
- [ ] ProviderRouter (OmniRoute pattern)
- [ ] Tool approval flow
- [ ] Dark mode, animations
- [ ] Voice input/output
- [ ] Chat bridge (Discord/Telegram)
- [ ] Plugin marketplace
- [ ] ProGuard, performance

### Phase 4 — Advanced
- [ ] Proactive AI loops
- [ ] Multi-modal (images/PDF)
- [ ] Accessibility integration
- [ ] WearOS support
- [ ] Play Store release

---

## 📊 Key Metrics

| Metric | Target |
|--------|--------|
| APK size | < 30MB (bootstrap downloaded at runtime) |
| Bootstrap size | 31MB (official Termux) |
| Agent count | 5 (OpenClaw, Claw Code, OpenCode, Codex, Claw Code components) |
| Provider count | 340+ (via OmniRoute) |
| Memory storage | Local SQLite + ONNX embeddings |
| GPU acceleration | Turnip/VirGL/Mesa |
| minSdk | 26 (Android 8) |
| Build system | GitHub Actions |

---

## 📚 References

- OpenClaw: https://github.com/openclaw/openclaw
- Claw Code: https://github.com/instructkr/claw-code
- Claw Code Website: https://claw-code.codes/
- OpenCode: https://github.com/anomalyco/opencode
- OpenAI Codex: https://github.com/openai/codex
- MemPalace: https://github.com/MemPalace/mempalace
- OmniRoute: https://github.com/diegosouzapw/OmniRoute
- OpenClaw Android: https://github.com/AidanPark/openclaw-android
- Termux: https://github.com/termux
- Termux Packages: https://github.com/termux/termux-packages
- Termux API: https://github.com/termux/termux-api
- Hardware Acceleration: https://github.com/LinuxDroidMaster/Termux-Desktops/blob/main/Documentation/HardwareAcceleration.md
- OmX: https://github.com/Yeachan-Heo/oh-my-codex
- OmO: https://github.com/code-yeongyu/oh-my-openagent
- LazyCodex: https://github.com/code-yeongyu/lazycodex

