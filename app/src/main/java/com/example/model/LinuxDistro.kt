package com.example.model

enum class DistroStatus {
    NOT_INSTALLED,
    INSTALLING,
    INSTALLED,
    RUNNING,
    STOPPED
}

enum class DistroType(
    val id: String,
    val displayName: String,
    val defaultVersion: String,
    val description: String,
    val defaultPkgManager: String,
    val rootfsSizeMb: Int
) {
    UBUNTU(
        id = "ubuntu",
        displayName = "Ubuntu 24.04 LTS",
        defaultVersion = "24.04 Noble Numbat",
        description = "Standard Linux environment with full apt support, glibc, and Python 3.12. Ideal for AI coding agents.",
        defaultPkgManager = "apt",
        rootfsSizeMb = 320
    ),
    DEBIAN(
        id = "debian",
        displayName = "Debian 12 Bookworm",
        defaultVersion = "12 (Stable)",
        description = "Rock-solid stability, minimal footprint, excellent compatibility with Node.js and C/C++ builds.",
        defaultPkgManager = "apt",
        rootfsSizeMb = 240
    ),
    ALPINE(
        id = "alpine",
        displayName = "Alpine Linux 3.20",
        defaultVersion = "3.20.2",
        description = "Ultra-lightweight musl-based distro (<50MB). Fast startup and low memory footprint for containers.",
        defaultPkgManager = "apk",
        rootfsSizeMb = 48
    ),
    ARCH(
        id = "arch",
        displayName = "Arch Linux",
        defaultVersion = "Rolling",
        description = "Bleeding edge packages via pacman. Latest compilers, Rust toolchain, and Python libraries.",
        defaultPkgManager = "pacman",
        rootfsSizeMb = 380
    ),
    KALI(
        id = "kali",
        displayName = "Kali Linux",
        defaultVersion = "Rolling",
        description = "Security, penetration testing, and network analysis environment with specialized CLI tooling.",
        defaultPkgManager = "apt",
        rootfsSizeMb = 450
    )
}

data class LinuxDistro(
    val id: String,
    val type: DistroType,
    val status: DistroStatus = DistroStatus.NOT_INSTALLED,
    val rootfsPath: String = "/data/data/com.example/files/distro/${type.id}",
    val defaultUser: String = "root",
    val architecture: String = "aarch64",
    val isDefault: Boolean = false,
    val installedPackagesCount: Int = 0,
    val memoryUsageMb: Int = 0
)
