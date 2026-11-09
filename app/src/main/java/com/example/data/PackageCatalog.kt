package com.example.data

import com.example.model.PackageEcosystem
import com.example.model.PackageItem

object PackageCatalog {
    val defaultPackages: List<PackageItem> = listOf(
        // APT (Debian/Ubuntu PRoot)
        PackageItem(
            name = "python3",
            ecosystem = PackageEcosystem.APT,
            version = "3.12.3-1",
            description = "Interactive high-level object-oriented language with pip and venv support.",
            isInstalled = true,
            installSize = "34 MB",
            tags = listOf("python", "runtime", "ai")
        ),
        PackageItem(
            name = "nodejs",
            ecosystem = PackageEcosystem.APT,
            version = "20.14.0-1",
            description = "Evented I/O for V8 JavaScript, required for Claude Code and modern web tooling.",
            isInstalled = true,
            installSize = "42 MB",
            tags = listOf("javascript", "node", "runtime")
        ),
        PackageItem(
            name = "git",
            ecosystem = PackageEcosystem.APT,
            version = "2.43.0",
            description = "Fast, scalable, distributed revision control system used by Aider and dev agents.",
            isInstalled = true,
            installSize = "22 MB",
            tags = listOf("vcs", "git", "tools")
        ),
        PackageItem(
            name = "curl",
            ecosystem = PackageEcosystem.APT,
            version = "8.5.0",
            description = "Command line tool for transferring data with URL syntax.",
            isInstalled = true,
            installSize = "4.2 MB",
            tags = listOf("network", "tools")
        ),
        PackageItem(
            name = "build-essential",
            ecosystem = PackageEcosystem.APT,
            version = "12.10",
            description = "Informational list of build-essential packages (gcc, g++, make, libc6-dev).",
            isInstalled = false,
            installSize = "118 MB",
            tags = listOf("c", "c++", "compiler")
        ),
        PackageItem(
            name = "ripgrep",
            ecosystem = PackageEcosystem.APT,
            version = "14.1.0",
            description = "Recursively searches directories for a regex pattern, ultra-fast code indexing.",
            isInstalled = true,
            installSize = "7.5 MB",
            tags = listOf("search", "indexer")
        ),
        PackageItem(
            name = "neovim",
            ecosystem = PackageEcosystem.APT,
            version = "0.9.5",
            description = "Vim-fork focused on extensibility and usability with Lua scripting.",
            isInstalled = false,
            installSize = "28 MB",
            tags = listOf("editor", "vim")
        ),
        PackageItem(
            name = "tmux",
            ecosystem = PackageEcosystem.APT,
            version = "3.4",
            description = "Terminal multiplexer to run background processes and persistent AI agents.",
            isInstalled = false,
            installSize = "3.8 MB",
            tags = listOf("terminal", "tools")
        ),

        // PKG (Termux)
        PackageItem(
            name = "termux-tools",
            ecosystem = PackageEcosystem.PKG,
            version = "1.42.0",
            description = "Standard Termux utilities and shell integration scripts.",
            isInstalled = true,
            installSize = "1.2 MB",
            tags = listOf("termux", "system")
        ),
        PackageItem(
            name = "clang",
            ecosystem = PackageEcosystem.PKG,
            version = "18.1.8",
            description = "C language family frontend for LLVM in Android user-space.",
            isInstalled = false,
            installSize = "85 MB",
            tags = listOf("llvm", "c", "c++")
        ),
        PackageItem(
            name = "rust",
            ecosystem = PackageEcosystem.PKG,
            version = "1.79.0",
            description = "Systems programming language focused on safety, speed, and concurrency.",
            isInstalled = false,
            installSize = "145 MB",
            tags = listOf("rust", "compiler")
        ),

        // PIP (Python)
        PackageItem(
            name = "aider-chat",
            ecosystem = PackageEcosystem.PIP,
            version = "0.54.0",
            description = "AI pair programming in your terminal. Edits code in your local git repo.",
            isInstalled = true,
            installSize = "18 MB",
            tags = listOf("ai", "coding-agent", "terminal")
        ),
        PackageItem(
            name = "google-generativeai",
            ecosystem = PackageEcosystem.PIP,
            version = "0.8.3",
            description = "Google Gemini SDK for Python. Multimodal reasoning and code synthesis.",
            isInstalled = true,
            installSize = "9.4 MB",
            tags = listOf("gemini", "ai", "sdk")
        ),
        PackageItem(
            name = "fastapi",
            ecosystem = PackageEcosystem.PIP,
            version = "0.111.0",
            description = "Modern, high-performance web framework for building APIs with Python 3.8+.",
            isInstalled = false,
            installSize = "12 MB",
            tags = listOf("web", "api", "python")
        ),
        PackageItem(
            name = "langchain",
            ecosystem = PackageEcosystem.PIP,
            version = "0.2.14",
            description = "Framework for developing applications powered by large language models.",
            isInstalled = false,
            installSize = "32 MB",
            tags = listOf("llm", "agents")
        ),

        // NPM (Node.js)
        PackageItem(
            name = "@anthropic-ai/claude-code",
            ecosystem = PackageEcosystem.NPM,
            version = "1.0.12",
            description = "Claude Code: Anthropic's agentic coding assistant CLI.",
            isInstalled = false,
            installSize = "15 MB",
            tags = listOf("claude", "agent", "cli")
        ),
        PackageItem(
            name = "typescript",
            ecosystem = PackageEcosystem.NPM,
            version = "5.5.4",
            description = "TypeScript is a language for application-scale JavaScript development.",
            isInstalled = false,
            installSize = "21 MB",
            tags = listOf("compiler", "types")
        ),
        PackageItem(
            name = "vite",
            ecosystem = PackageEcosystem.NPM,
            version = "5.4.2",
            description = "Next Generation Frontend Tooling with instant HMR and web server.",
            isInstalled = false,
            installSize = "16 MB",
            tags = listOf("frontend", "web", "dev-server")
        ),

        // Cargo (Rust)
        PackageItem(
            name = "bat",
            ecosystem = PackageEcosystem.CARGO,
            version = "0.24.0",
            description = "A cat clone with syntax highlighting and Git integration.",
            isInstalled = false,
            installSize = "6.8 MB",
            tags = listOf("cli", "viewer")
        )
    )
}
