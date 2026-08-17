package com.clawdroid.android

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.URL
import java.security.MessageDigest
import java.util.zip.ZipInputStream

class BootstrapManager(private val context: Context) {
    companion object {
        private const val TAG = "BootstrapManager"
        private val ELF_SIGNATURE = byteArrayOf(0x7f, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte())
        private const val SYMLINK_SEPARATOR = "\u2190"
        private const val SYMLINK_PARTS_COUNT = 2
    }

    val prefixDir = File(context.filesDir, "usr")
    val homeDir = File(context.filesDir, "home")
    val tmpDir = File(context.filesDir, "tmp")
    val wwwDir = File(prefixDir, "share/clawdroid/www")
    private val stagingDir = File(context.filesDir, "usr-staging")

    fun isInstalled(): Boolean = prefixDir.resolve("bin/bash").exists()

    fun needsPostSetup(): Boolean {
        val marker = File(homeDir, ".clawdroid/.post-setup-done")
        return isInstalled() && !marker.exists()
    }

    val postSetupScript: File get() = File(homeDir, ".clawdroid/post-setup.sh")

    suspend fun startSetup(onProgress: (Float, String) -> Unit) = withContext(Dispatchers.IO) {
        if (stagingDir.exists()) stagingDir.deleteRecursively()
        if (isInstalled()) prefixDir.deleteRecursively()

        onProgress(0.05f, "Preparing bootstrap...")
        val zipStream = getBootstrapStream(onProgress)

        onProgress(0.30f, "Extracting bootstrap...")
        extractBootstrap(zipStream)

        onProgress(0.60f, "Configuring environment...")
        fixTermuxPaths(stagingDir)
        configureApt(stagingDir)

        stagingDir.renameTo(prefixDir)
        setupDirectories()
        copyAssetScripts()
        syncWwwFromAssets()
        setupTermuxExec()

        onProgress(1f, "Setup complete")
    }

    private suspend fun getBootstrapStream(onProgress: (Float, String) -> Unit): InputStream {
        try { return context.assets.open("bootstrap-aarch64.zip") } catch (_: Exception) {}
        onProgress(0.10f, "Downloading bootstrap...")
        val url = UrlResolver(context).getBootstrapUrl()
        return URL(url).openStream()
    }

    private fun extractBootstrap(inputStream: InputStream) {
        stagingDir.deleteRecursively()
        stagingDir.mkdirs()
        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "SYMLINKS.txt") {
                    processSymlinks(zip, stagingDir)
                } else if (!entry.isDirectory) {
                    val file = File(stagingDir, entry.name)
                    file.parentFile?.mkdirs()
                    file.outputStream().use { out -> zip.copyTo(out) }
                    markExecutableIfNeeded(file, entry.name)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    private fun markExecutableIfNeeded(file: File, name: String) {
        val knownExecutable = name.startsWith("bin/") || name.startsWith("libexec/") ||
            name.startsWith("lib/apt/") || name.startsWith("lib/bash/") ||
            name.endsWith(".so") || name.contains(".so.")
        if (knownExecutable) {
            try { file.setExecutable(true, false) } catch (_: Exception) {}
        }
    }

    private fun processSymlinks(zip: ZipInputStream, targetDir: File) {
        val content = zip.bufferedReader().readText()
        val ourPackage = context.packageName
        content.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split(SYMLINK_SEPARATOR)
            if (parts.size == SYMLINK_PARTS_COUNT) parts else null
        }.forEach { parts ->
            val symlinkTarget = parts[0].trim().replace("com.termux", ourPackage)
            val symlinkPath = File(targetDir, parts[1].trim().replace("com.termux", ourPackage))
            symlinkPath.parentFile?.mkdirs()
            try {
                if (symlinkPath.exists()) symlinkPath.delete()
                android.system.Os.symlink(symlinkTarget, symlinkPath.absolutePath)
            } catch (_: Exception) {}
        }
    }

    private fun fixTermuxPaths(dir: File) {
        val binDir = File(dir, "bin")
        if (!binDir.exists()) return
        binDir.listFiles()?.forEach { file ->
            if (file.isFile && file.canExecute()) {
                try {
                    val firstBytes = file.inputStream().use { it.readNBytes(4) }
                    if (firstBytes.contentEquals(ELF_SIGNATURE)) {
                        file.setExecutable(true, false)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun configureApt(dir: File) {
        val aptConf = File(dir, "etc/apt/apt.conf")
        aptConf.parentFile?.mkdirs()
        aptConf.writeText("""
            APT::Install-Recommends "false";
            APT::Install-Suggests "false";
            Dir::Cache::Archives "${tmpDir.absolutePath}/";
        """.trimIndent())
    }

    private fun setupDirectories() {
        homeDir.mkdirs()
        tmpDir.mkdirs()
        File(homeDir, ".clawdroid").mkdirs()
        File(homeDir, ".clawdroid/patches").mkdirs()
    }

    private fun copyAssetScripts() {
        val ocaDir = File(homeDir, ".clawdroid")
        copyAssetFile("post-setup.sh", File(ocaDir, "post-setup.sh"))
        copyAssetFile("glibc-compat.js", File(ocaDir, "patches/glibc-compat.js"))
    }

    private fun copyAssetFile(assetName: String, target: File) {
        try {
            context.assets.open(assetName).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            target.setExecutable(true)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to copy $assetName", e)
        }
    }

    fun syncWwwFromAssets() {
        try {
            wwwDir.mkdirs()
            copyAssetDir("www", wwwDir)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to sync www", e)
        }
    }

    private fun copyAssetDir(assetPath: String, targetDir: File) {
        val entries = context.assets.list(assetPath) ?: return
        targetDir.mkdirs()
        for (entry in entries) {
            val children = context.assets.list("$assetPath/$entry")
            if (!children.isNullOrEmpty()) {
                copyAssetDir("$assetPath/$entry", File(targetDir, entry))
            } else {
                context.assets.open("$assetPath/$entry").use { input ->
                    File(targetDir, entry).outputStream().use { output -> input.copyTo(output) }
                }
            }
        }
    }

    private fun setupTermuxExec() {
        val termuxExecLib = File(prefixDir, "lib/libtermux-exec.so")
        if (termuxExecLib.exists()) {
            AppLogger.i(TAG, "libtermux-exec.so found")
        }
    }

    fun verifyBootstrapHash(expectedHash: String): Boolean {
        val bootstrapFile = File(context.cacheDir, "bootstrap-aarch64.zip")
        if (!bootstrapFile.exists()) return false
        val digest = MessageDigest.getInstance("SHA-256")
        bootstrapFile.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
        return actualHash == expectedHash
    }
}
