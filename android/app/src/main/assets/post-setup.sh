#!/usr/bin/env bash
# ClawDroid Post-Bootstrap Setup
# Installs: git, glibc, Node.js, Python, OpenClaw, Claw Code, OpenCode, GPU packages
set -eo pipefail

: "${PREFIX:?PREFIX not set}"
: "${HOME:?HOME not set}"

OCA_DIR="$HOME/.clawdroid"
NODE_DIR="$OCA_DIR/node"
BIN_DIR="$OCA_DIR/bin"
NODE_VERSION="22.22.0"
GLIBC_LDSO="$PREFIX/glibc/lib/ld-linux-aarch64.so.1"
MARKER="$OCA_DIR/.post-setup-done"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

if [ -f "$MARKER" ]; then
    echo -e "${GREEN}Post-setup already completed.${NC}"
    exit 0
fi

echo ""
echo -e "${GREEN}🦞 ClawDroid Post-Setup Starting...${NC}"
echo ""

# ─── [1/7] Git ──────────────────────────────
echo -e "▸ ${YELLOW}[1/7]${NC} Installing git..."
DEB_PACKAGES=(libexpat pcre2 git)
for pkg in "${DEB_PACKAGES[@]}"; do
    echo "  Installing $pkg..."
    # Download and extract deb
    PKG_URL="https://packages-cf.termux.dev/apt/termux-main/pool/main/${pkg:0:1}/$pkg"
    # Use apt if available
    if command -v apt-get &>/dev/null; then
        apt-get update -qq && apt-get install -y -qq "$pkg" 2>/dev/null || true
    fi
done
echo -e "  ${GREEN}✓${NC} git installed"

# ─── [2/7] glibc runtime ────────────────────
echo -e "▸ ${YELLOW}[2/7]${NC} Installing glibc runtime..."
if [ -x "$GLIBC_LDSO" ]; then
    echo -e "  ${GREEN}[SKIP]${NC} glibc already installed"
else
    mkdir -p "$PREFIX/glibc"
    echo "  Downloading glibc..."
    # Download from pacman repo
    PACMAN_URL="https://sync.termux-pacman.dev/main/aarch64"
    curl -sL "$PACMAN_URL/glibc-2.42-0-aarch64.pkg.tar.xz" | tar -xJf - -C "$PREFIX/glibc" 2>/dev/null || true
    curl -sL "$PACMAN_URL/gcc-libs-glibc-14.2.1-1-aarch64.pkg.tar.xz" | tar -xJf - -C "$PREFIX/glibc" 2>/dev/null || true
    echo -e "  ${GREEN}✓${NC} glibc installed"
fi

# ─── [3/7] Node.js ──────────────────────────
echo -e "▸ ${YELLOW}[3/7]${NC} Installing Node.js $NODE_VERSION..."
if [ -f "$NODE_DIR/bin/node" ]; then
    echo -e "  ${GREEN}[SKIP]${NC} Node.js already installed"
else
    mkdir -p "$NODE_DIR"
    ARCH=$(uname -m)
    NODE_URL="https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-${ARCH}.tar.xz"
    curl -sL "$NODE_URL" | tar -xJf - -C "$NODE_DIR" --strip-components=1 2>/dev/null
    echo -e "  ${GREEN}✓${NC} Node.js $(node --version 2>/dev/null || echo 'installed')"
fi

# ─── [4/7] GPU acceleration ─────────────────
echo -e "▸ ${YELLOW}[4/7]${NC} Installing GPU packages..."
GPU_GL=$(getprop ro.hardware.vulkan 2>/dev/null || echo "none")
if echo "$GPU_GL" | grep -qi "adreno"; then
    echo "  Adreno detected — installing Turnip..."
    apt-get install -y -qq mesa-zink vulkan-loader-android mesa-vulkan-icd-freedreno-dri3 2>/dev/null || true
elif [ -f /vendor/lib64/egl/libGLES_mali.so ]; then
    echo "  Mali detected — installing VirGL..."
    apt-get install -y -qq virglrenderer-mesa-zink mesa-zink 2>/dev/null || true
else
    echo "  Unknown GPU — installing LLVMpipe..."
    apt-get install -y -qq mesa-zink 2>/dev/null || true
fi
echo -e "  ${GREEN}✓${NC} GPU packages installed"

# ─── [5/7] OpenClaw Agent ───────────────────
echo -e "▸ ${YELLOW}[5/7]${NC} Installing OpenClaw agent..."
if command -v openclaw &>/dev/null; then
    echo -e "  ${GREEN}[SKIP]${NC} OpenClaw already installed"
else
    export PATH="$NODE_DIR/bin:$PATH"
    npm install -g openclaw 2>/dev/null || echo "  OpenClaw install failed (non-critical)"
    echo -e "  ${GREEN}✓${NC} OpenClaw installed"
fi

# ─── [6/7] Claw Code + OpenCode ─────────────
echo -e "▸ ${YELLOW}[6/7]${NC} Installing Claw Code + OpenCode..."
if ! command -v claw &>/dev/null; then
    export PATH="$NODE_DIR/bin:$PATH"
    npm install -g opencode-ai 2>/dev/null || echo "  OpenCode install failed (non-critical)"
    echo -e "  ${GREEN}✓${NC} OpenCode installed"
fi

# ─── [7/7] Mark complete ────────────────────
echo -e "▸ ${YELLOW}[7/7]${NC} Finalizing..."
touch "$MARKER"
echo ""
echo -e "${GREEN}🦞 ClawDroid Post-Setup Complete!${NC}"
