package com.example.model

enum class CodeLanguage(val extension: String, val displayName: String) {
    PYTHON("py", "Python"),
    JAVASCRIPT("js", "JavaScript"),
    TYPESCRIPT("ts", "TypeScript"),
    HTML("html", "HTML"),
    CSS("css", "CSS"),
    JSON("json", "JSON"),
    KOTLIN("kt", "Kotlin"),
    C("c", "C"),
    CPP("cpp", "C++"),
    RUST("rs", "Rust"),
    SHELL("sh", "Shell Script"),
    MARKDOWN("md", "Markdown"),
    DOCKER("dockerfile", "Dockerfile"),
    TEXT("txt", "Plain Text");

    companion object {
        fun fromFileName(fileName: String): CodeLanguage {
            val lower = fileName.lowercase()
            return when {
                lower.endsWith(".py") -> PYTHON
                lower.endsWith(".js") -> JAVASCRIPT
                lower.endsWith(".ts") -> TYPESCRIPT
                lower.endsWith(".html") || lower.endsWith(".htm") -> HTML
                lower.endsWith(".css") -> CSS
                lower.endsWith(".json") -> JSON
                lower.endsWith(".kt") || lower.endsWith(".kts") -> KOTLIN
                lower.endsWith(".c") || lower.endsWith(".h") -> C
                lower.endsWith(".cpp") || lower.endsWith(".hpp") || lower.endsWith(".cc") -> CPP
                lower.endsWith(".rs") -> RUST
                lower.endsWith(".sh") || lower.endsWith(".bash") -> SHELL
                lower.endsWith(".md") -> MARKDOWN
                lower.endsWith("dockerfile") -> DOCKER
                else -> TEXT
            }
        }
    }
}

data class ProjectFile(
    val id: String,
    val name: String,
    val path: String,
    val isDirectory: Boolean = false,
    val content: String = "",
    val language: CodeLanguage = CodeLanguage.TEXT,
    val isModified: Boolean = false,
    val children: List<ProjectFile> = emptyList(),
    val sizeBytes: Long = 0L,
    val lastModified: Long = System.currentTimeMillis()
)
