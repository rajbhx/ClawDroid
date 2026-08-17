#!/bin/bash
# Detect GPU type for hardware acceleration
GPU_GL=$(getprop ro.hardware.vulkan 2>/dev/null || echo "none")
GPU_CHIP=$(getprop ro.hardware.chipname 2>/dev/null || echo "unknown")

if echo "$GPU_GL" | grep -qi "adreno"; then
    echo "turnip"
elif [ -f /vendor/lib64/egl/libGLES_mali.so ] || echo "$GPU_CHIP" | grep -qi "mali"; then
    echo "virgl"
else
    echo "llvmpipe"
fi
