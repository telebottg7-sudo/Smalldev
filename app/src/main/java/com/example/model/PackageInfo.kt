package com.example.model

enum class PackageEcosystem(val displayName: String, val cliCommand: String, val iconLabel: String) {
    APT("APT (PRoot)", "apt-get", "DEB"),
    PKG("PKG (Termux)", "pkg", "TMX"),
    PIP("PIP (Python)", "pip", "PY"),
    NPM("NPM (Node.js)", "npm", "JS"),
    CARGO("Cargo (Rust)", "cargo", "RS")
}

data class PackageItem(
    val name: String,
    val ecosystem: PackageEcosystem,
    val version: String,
    val description: String,
    val isInstalled: Boolean = false,
    val installSize: String = "15 MB",
    val tags: List<String> = emptyList()
)
