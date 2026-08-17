package com.clawdroid.android

import android.content.Context
import java.io.File

object EnvironmentBuilder {
    fun build(context: Context): Map<String, String> = build(context.filesDir)

    fun build(filesDir: File): Map<String, String> {
        val prefix = File(filesDir, "usr")
        val home = File(filesDir, "home")
        val tmp = File(filesDir, "tmp")

        return buildMap {
            put("PREFIX", prefix.absolutePath)
            put("HOME", home.absolutePath)
            put("TMPDIR", tmp.absolutePath)
            val nodeBin = "${home.absolutePath}/.openclaw-android/node/bin"
            val localBin = "${home.absolutePath}/.local/bin"
            val prefixBin = "${prefix.absolutePath}/bin"
            put("PATH", "$nodeBin:$localBin:$prefixBin:$prefixBin/applets")
            put("LD_LIBRARY_PATH", "${prefix.absolutePath}/lib")

            val termuxExecLib = File(prefix, "lib/libtermux-exec.so")
            if (termuxExecLib.exists()) {
                put("LD_PRELOAD", termuxExecLib.absolutePath)
            }
            put("TERMUX__PREFIX", prefix.absolutePath)
            put("TERMUX_PREFIX", prefix.absolutePath)
            put("TERMUX__ROOTFS", filesDir.absolutePath)
            val appDataDir = filesDir.parentFile?.absolutePath ?: filesDir.absolutePath
            put("TERMUX_APP__DATA_DIR", appDataDir)
            put("TERMUX_APP__LEGACY_DATA_DIR", "/data/data/com.termux")
            put("APT_CONFIG", "${prefix.absolutePath}/etc/apt/apt.conf")
            put("DPKG_ADMINDIR", "${prefix.absolutePath}/var/lib/dpkg")
            put("DPKG_ROOT", prefix.absolutePath)
            put("SSL_CERT_FILE", "${prefix.absolutePath}/etc/tls/cert.pem")
            put("CURL_CA_BUNDLE", "${prefix.absolutePath}/etc/tls/cert.pem")
            put("GIT_SSL_CAINFO", "${prefix.absolutePath}/etc/tls/cert.pem")
            put("GIT_CONFIG_NOSYSTEM", "1")
            put("GIT_EXEC_PATH", "${prefix.absolutePath}/libexec/git-core")
            put("GIT_TEMPLATE_DIR", "${prefix.absolutePath}/share/git-core/templates")
            put("LANG", "en_US.UTF-8")
            put("TERM", "xterm-256color")
            put("ANDROID_DATA", "/data")
            put("ANDROID_ROOT", "/system")
            put("CLAWDROID_WORKDIR", "${home.absolutePath}/.clawdroid/workspace")
        }
    }
}
