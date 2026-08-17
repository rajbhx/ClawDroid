# 🦞 ClawDroid

> Android AI Agent Platform — Multiple coding agents running natively on your phone

[![Build](https://github.com/rajbhx/ClawDroid/actions/workflows/build.yml/badge.svg)](https://github.com/rajbhx/ClawDroid/actions)
[![License](https://img.shields.io/github/license/rajbhx/ClawDroid)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8%2B-brightgreen)](https://developer.android.com/about/versions/oreo)

## What is ClawDroid?

ClawDroid is a feature-rich Android app that runs multiple AI coding agents natively on your device, powered by a Termux bootstrap with GPU acceleration.

### Included Agents

| Agent | Stars | What It Does |
|-------|-------|-------------|
| [OpenClaw](https://github.com/openclaw/openclaw) | ★386K | Personal AI assistant with agents, tools, skills, memory, MCP |
| [Claw Code](https://github.com/instructkr/claw-code) | ★48K | Multi-agent coding harness with RAG service |
| [OpenCode](https://github.com/anomalyco/opencode) | ★198K | 20+ coding tools (shell, read, write, edit, grep) |
| [OpenAI Codex](https://github.com/openai/codex) | — | Sandboxed coding agent with guardian approval |
| [MemPalace](https://github.com/MemPalace/mempalace) | ★58K | AI memory system (local-first, semantic search) |
| [OmniRoute](https://github.com/diegosouzapw/OmniRoute) | ★49K | 340+ provider AI gateway with auto-fallback |

### Hardware Acceleration

- **Turnip** (Adreno 610+) — Best performance (GLMARK2: 198)
- **VirGL** (Mali) — Good performance (GLMARK2: 75)
- **LLVMpipe** (fallback) — Software rendering (GLMARK2: 93)

## Quick Start

```bash
# Clone the repo
git clone https://github.com/rajbhx/ClawDroid.git
cd ClawDroid

# Build the APK
cd android
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Architecture

```
ClawDroid Android App
├── Terminal (Termux) — GPU-accelerated shell
├── AI Chat (React) — Multi-provider streaming
├── Memory (Room + ONNX) — Local-first semantic search
├── Agent Bridge (Kotlin ↔ WebView ↔ Node.js)
└── GPU (Turnip/VirGL/Mesa) — Hardware acceleration
```

## Project Structure

```
ClawDroid/
├── android/
│   ├── app/                    # Main app module
│   ├── terminal-emulator/      # Termux terminal (forked)
│   └── terminal-view/          # Terminal renderer (forked)
├── scripts/                    # GPU + build scripts
├── patches/                    # Android compatibility patches
├── .github/workflows/          # CI/CD
├── PLAYBOOK.md                 # Problems, solutions, roadmap
└── README.md
```

## Development

See [PLAYBOOK.md](PLAYBOOK.md) for the complete playbook including:
- Source repo inventory
- Architecture decisions
- Problems & solutions
- Roadmap

## License

MIT License — See [LICENSE](LICENSE) for details.

## Credits

Built on the work of:
- [OpenClaw](https://github.com/openclaw/openclaw) by Peter Steinberger and community
- [Claw Code](https://github.com/instructkr/claw-code) by Sigrid Jin
- [OpenCode](https://github.com/anomalyco/opencode) by Anomaly
- [Termux](https://github.com/termux) community
- [MemPalace](https://github.com/MemPalace/mempalace) team
- [OmniRoute](https://github.com/diegosouzapw/OmniRoute) by Diego

