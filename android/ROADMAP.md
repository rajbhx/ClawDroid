# 🦞 ClawDroid — Roadmap

## ✅ DONE
- [x] Termux bootstrap (download + extract on first boot)
- [x] Terminal emulation (Termux TerminalView, proper lifecycle)
- [x] Dashboard (system status, quick actions, free models)
- [x] AI Chat (OmniRoute streaming, 340+ providers)
- [x] Memory system (Room + ONNX embeddings)
- [x] Agent Hub (6 agents, install + run)
- [x] GPU acceleration (Turnip/VirGL/LLVMpipe)
- [x] Dual theme (Material You + Tokyo Night)
- [x] Preinstalled packages (git, curl, wget, nodejs, python)
- [x] GitHub Actions CI (3 parallel workflows)

## 🔄 IN PROGRESS
- [ ] Terminal stability (crash fixes, lifecycle)
- [ ] Theme switching (DataStore → Compose)
- [ ] Agent install progress (progress bar)
- [ ] Release APK (GitHub Releases)

## 📋 NEXT
- [ ] File browser (browse terminal filesystem)
- [ ] Code editor (syntax highlighting)
- [ ] Git integration (via terminal)
- [ ] Audio recording (voice input to AI)
- [ ] Camera integration (image to AI)
- [ ] Notification system (AI responses as notifications)
- [ ] Widget (home screen AI chat)
- [ ] F-Droid submission
- [ ] Play Store listing
- [ ] Wear OS companion
- [ ] Tablet UI (landscape, split view)

## 🏗️ Architecture
```
App → BootstrapManager → Termux Bootstrap → PackageInstaller
    → TerminalView ← TerminalSession ← EnvironmentBuilder
    → Dashboard ← ViewModel ← Repository ← Room/OmniRoute
```

## 🔧 Tech
- Kotlin 2.1, Compose Material3, Hilt, Room, OkHttp SSE, ONNX Runtime
- minSdk 26, compileSdk 35, AGP 8.7.3, Gradle 8.11.1
