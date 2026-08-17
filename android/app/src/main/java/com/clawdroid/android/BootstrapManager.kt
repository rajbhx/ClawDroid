package com.clawdroid.android

import android.content.Context
import android.system.Os
import com.clawdroid.android.data.PackageInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.zip.ZipInputStream

class BootstrapManager(private val context: Context) {
    companion object {
        private const val TAG = "BootstrapManager"
        private const val ELF_MAGIC_SIZE = 4
        private val ELF_SIGNATURE = byteArrayOf(0x7f, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte())
        private const val SYMLINK_SEPARATOR = "\u2190"
        private const val SYMLINK_PARTS_COUNT = 2
    }

    val prefixDir = File(context.filesDir, "usr")
    val homeDir = File(context.filesDir, "home")
    val tmpDir = File(context.filesDir, "tmp")
    val wwwDir = File(prefixDir, "share/clawdroid/www")
    private val stagingDir = File(context.filesDir, "usr-staging")

    fun isInstalled(): Boolean = prefixDir.resolve("bin/sh").exists()

    fun needsPostSetup(): Boolean {
        val marker = File(homeDir, ".clawdroid/.post-setup-done")
        return isInstalled() && !marker.exists()
    }

    val postSetupScript: File get() = File(homeDir, ".clawdroid/post-setup.sh")

    suspend fun startSetup(onProgress: (Float, String) -> Unit) = withContext(Dispatchers.IO) {
        try {
            // Step 0: Clean up any incomplete previous attempt
            if (stagingDir.exists()) safeDelete(stagingDir)
            if (isInstalled()) safeDelete(prefixDir)

            // Step 1: Get bootstrap stream
            onProgress(0.05f, "Preparing bootstrap...")
            val zipStream = getBootstrapStream(onProgress)

            // Step 2: Extract bootstrap
            onProgress(0.30f, "Extracting bootstrap...")
            extractBootstrap(zipStream)

            // Step 3: Fix paths and configure
            onProgress(0.60f, "Configuring environment...")
            fixTermuxPaths(stagingDir)
            configureApt(stagingDir)

            // Step 4: Atomic rename staging → prefix
            onProgress(0.70f, "Finalizing...")
            if (!stagingDir.renameTo(prefixDir)) {
                // Fallback: copy then delete
                stagingDir.copyRecursively(prefixDir, overwrite = true)
                safeDelete(stagingDir)
            }

            // Step 5: Setup directories and scripts
            setupDirectories()
            copyAssetScripts()
            syncWwwFromAssets()
            setupTermuxExec()

            // Step 6: Install core packages
            onProgress(0.80f, "Installing core packages...")
            installCorePackages()

            onProgress(1f, "Setup complete")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Bootstrap setup failed", e)
            // Clean up on failure
            try { safeDelete(stagingDir) } catch (_: Exception) {}
            throw e
        }
    }

    private fun safeDelete(dir: File) {
        try {
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { safeDelete(it) }
            }
            dir.delete()
        } catch (_: Exception) {}
    }

    private fun installCorePackages() {
        try {
            val installer = PackageInstaller()
            val script = installer.getCoreInstallScript()
            val scriptFile = File(tmpDir, "install-packages.sh")
            scriptFile.parentFile?.mkdirs()
            scriptFile.writeText(script)
            scriptFile.setExecutable(true)
            val process = Runtime.getRuntime().exec(arrayOf(
                "${prefixDir.absolutePath}/bin/bash",
                scriptFile.absolutePath,
            ))
            process.waitFor()
        } catch (e: Exception) {
            AppLogger.w(TAG, "Package install failed (non-fatal)", e)
        }
    }

    private suspend fun getBootstrapStream(onProgress: (Float, String) -> Unit): InputStream {
        // Try assets first
        try {
            return context.assets.open("bootstrap-aarch64.zip")
        } catch (_: Exception) {
            // Assets not available, download
        }

        onProgress(0.10f, "Downloading bootstrap...")
        val url = UrlResolver(context).getBootstrapUrl()
        return URL(url).openStream()
    }

    private fun extractBootstrap(inputStream: InputStream) {
        safeDelete(stagingDir)
        stagingDir.mkdirs()

        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                processZipEntry(zip, entry)
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    private fun processZipEntry(zip: ZipInputStream, entry: java.util.zip.ZipEntry) {
        if (entry.name == "SYMLINKS.txt") {
            processSymlinks(zip, stagingDir)
        } else if (!entry.isDirectory) {
            val file = File(stagingDir, entry.name)
            // Security: prevent path traversal
            if (!file.canonicalPath.startsWith(stagingDir.canonicalPath)) return
            file.parentFile?.mkdirs()
            file.outputStream().use { out -> zip.copyTo(out) }
            markExecutableIfNeeded(file, entry.name)
        }
    }

    private fun markExecutableIfNeeded(file: File, name: String) {
        val knownExecutable = name.startsWith("bin/") || name.startsWith("libexec/") ||
            name.startsWith("lib/apt/") || name.startsWith("lib/bash/") ||
            name.endsWith(".so") || name.contains(".so.")
        if (knownExecutable) {
            try { file.setExecutable(true) } catch (_: Exception) {}
        } else if (file.length() > ELF_MAGIC_SIZE && isElfBinary(file)) {
            try { file.setExecutable(true) } catch (_: Exception) {}
        }
    }

    private fun isElfBinary(file: File): Boolean = try {
        file.inputStream().use { fis ->
            val magic = ByteArray(ELF_MAGIC_SIZE)
            fis.read(magic) == ELF_MAGIC_SIZE && magic.contentEquals(ELF_SIGNATURE)
        }
    } catch (_: Exception) { false }

    /**
     * Process SYMLINKS.txt: each line is "target\u2190linkpath"
     * Replace com.termux paths with our package name.
     */
    private fun processSymlinks(zip: ZipInputStream, targetDir: File) {
        val content = zip.bufferedReader().readText()
        val ourPackage = context.packageName
        content.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split(SYMLINK_SEPARATOR)
                if (parts.size == SYMLINK_PARTS_COUNT) parts else null
            }
            .forEach { parts ->
                val symlinkTarget = parts[0].trim().replace("com.termux", ourPackage)
                val symlinkPath = parts[1].trim()
                val linkFile = File(targetDir, symlinkPath)
                linkFile.parentFile?.mkdirs()
                try {
                    if (linkFile.exists()) linkFile.delete()
                    Os.symlink(symlinkTarget, linkFile.absolutePath)
                } catch (e: Exception) {
                    AppLogger.w(TAG, "Failed to create symlink: $symlinkPath -> $symlinkTarget", e)
                }
            }
    }

    private fun fixTermuxPaths(dir: File) {
        val ourPackage = context.packageName
        val oldPrefix = "/data/data/com.termux/files/usr"
        val newPrefix = prefixDir.absolutePath

        // Fix dpkg status database
        fixTextFile(dir.resolve("var/lib/dpkg/status"), oldPrefix, newPrefix)

        // Fix dpkg info files
        val dpkgInfoDir = dir.resolve("var/lib/dpkg/info")
        if (dpkgInfoDir.isDirectory) {
            dpkgInfoDir.listFiles()?.filter { it.name.endsWith(".list") }?.forEach { file ->
                fixTextFile(file, "com.termux", ourPackage)
            }
        }

        // Fix git scripts shebangs
        val gitCoreDir = dir.resolve("libexec/git-core")
        if (gitCoreDir.isDirectory) {
            gitCoreDir.listFiles()?.forEach { file ->
                if (file.isFile && !file.name.contains(".")) {
                    fixTextFile(file, oldPrefix, newPrefix)
                }
            }
        }
    }

    private fun fixTextFile(file: File, oldText: String, newText: String) {
        if (!file.exists() || !file.isFile) return
        try {
            val content = file.readText()
            if (content.contains(oldText)) {
                file.writeText(content.replace(oldText, newText))
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to fix paths in ${file.name}", e)
        }
    }

    private fun configureApt(dir: File) {
        val aptConf = File(dir, "etc/apt/apt.conf")
        aptConf.parentFile?.mkdirs()
        aptConf.writeText(
            "APT::Install-Recommends \"false\";\n" +
            "APT::Install-Suggests \"false\";\n" +
            "Dir::Cache::Archives \"${tmpDir.absolutePath}/\";\n"
        )
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
}
