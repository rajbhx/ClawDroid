#!/bin/bash
# Start GPU acceleration server
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GPU_MODE=$("$SCRIPT_DIR/detect-gpu.sh")

case "$GPU_MODE" in
    turnip)
        export MESA_LOADER_DRIVER_OVERRIDE=zink
        export TU_DEBUG=noconform
        export ZINK_DESCRIPTORS=lazy
        echo "Turnip acceleration active (MESA_LOADER_DRIVER_OVERRIDE=zink)"
        ;;
    virgl)
        virgl_test_server --use-egl-surfaceless --use-gles &
        sleep 1
        export GALLIUM_DRIVER=virpipe
        export MESA_GL_VERSION_OVERRIDE=4.0
        echo "VirGL acceleration active (GALLIUM_DRIVER=virpipe)"
        ;;
    llvmpipe)
        export MESA_NO_ERROR=1
        export MESA_GLES_VERSION_OVERRIDE=3.2
        echo "LLVMpipe fallback active"
        ;;
esac

# Common Mesa overrides
export MESA_NO_ERROR=1
export MESA_GLES_VERSION_OVERRIDE=3.2
echo "GPU server started for: $GPU_MODE"
