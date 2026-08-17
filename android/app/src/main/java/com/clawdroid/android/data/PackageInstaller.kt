package com.clawdroid.android.data

import com.clawdroid.android.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageInstaller @Inject constructor() {

    data class Pkg(val name: String, val cmd: String, val category: String)

    val CORE_PACKAGES = listOf(
        Pkg("git", "pkg install -y git", "core"),
        Pkg("curl", "pkg install -y curl", "core"),
        Pkg("wget", "pkg install -y wget", "core"),
        Pkg("tar", "pkg install -y tar", "core"),
        Pkg("unzip", "pkg install -y unzip", "core"),
    )

    val DEV_PACKAGES = listOf(
        Pkg("nodejs", "pkg install -y nodejs", "dev"),
        Pkg("python", "pkg install -y python", "dev"),
        Pkg("build-essential", "pkg install -y build-essential", "dev"),
        Pkg("cmake", "pkg install -y cmake", "dev"),
    )

    val AI_PACKAGES = listOf(
        Pkg("npm-global", "npm install -g npm", "ai"),
        Pkg("pip", "pkg install -y python-pip", "ai"),
    )

    val ALL_PACKAGES = CORE_PACKAGES + DEV_PACKAGES + AI_PACKAGES

    fun getInstallScript(): String {
        val cmds = ALL_PACKAGES.joinToString("\n") { "# ${it.name}\n${it.cmd}" }
        return """
#!/bin/bash
set -e
echo "Installing packages..."
$cmds
echo "Package installation complete!"
""".trimIndent()
    }

    fun getCoreInstallScript(): String {
        val cmds = CORE_PACKAGES.joinToString("\n") { "${it.cmd}" }
        return cmds
    }

    fun isPackageInstalled(prefix: String, pkg: String): Boolean {
        return java.io.File("$prefix/bin/$pkg").exists()
    }

    fun getInstalledCount(prefix: String): Int {
        return ALL_PACKAGES.count { isPackageInstalled(prefix, it.name) }
    }
}
