#!/bin/bash
# Install GPU acceleration packages based on detected GPU
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GPU_MODE=$("$SCRIPT_DIR/detect-gpu.sh")

echo "Detected GPU mode: $GPU_MODE"

case "$GPU_MODE" in
    turnip)
        echo "Installing Turnip (Adreno 610+)..."
        pkg install -y mesa-zink vulkan-loader-android mesa-vulkan-icd-freedreno-dri3 2>/dev/null || true
        ;;
    virgl)
        echo "Installing VirGL (Mali)..."
        pkg install -y mesa-zink virglrenderer-mesa-zink vulkan-loader-android virglrenderer-android 2>/dev/null || true
        ;;
    llvmpipe)
        echo "Installing LLVMpipe (fallback)..."
        pkg install -y mesa-zink 2>/dev/null || true
        ;;
esac

echo "GPU packages installed for: $GPU_MODE"
