package com.clawdroid.android

import android.os.Build

object GpuDetector {
    enum class GpuType { ADRENO, MALI, POWERVR, UNKNOWN }

    fun detect(): GpuType {
        val glRenderer = getGlRenderer().lowercase()
        return when {
            glRenderer.contains("adreno") -> GpuType.ADRENO
            glRenderer.contains("mali") -> GpuType.MALI
            glRenderer.contains("powervr") || glRenderer.contains("sgx") -> GpuType.POWERVR
            else -> GpuType.UNKNOWN
        }
    }

    fun getAccelerationMethod(): String {
        val gpu = detect()
        return when (gpu) {
            GpuType.ADRENO -> "turnip"
            GpuType.MALI -> "virgl"
            else -> "llvmpipe"
        }
    }

    fun getGlesVersion(): String {
        return Build.VERSION.SDK_INT.toString()
    }

    private fun getGlRenderer(): String {
        return try {
            val clazz = Class.forName("android.opengl.GLES20")
            val method = clazz.getMethod("glGetString", Int::class.javaPrimitiveType)
            val renderer = method.invoke(null, 0x1F01) as? String
            renderer ?: "unknown"
        } catch (_: Exception) {
            "unknown"
        }
    }
}
