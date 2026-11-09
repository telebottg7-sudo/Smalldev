package com.example.model

enum class ProjectTemplate(
    val id: String,
    val title: String,
    val description: String,
    val iconTag: String,
    val primaryLanguage: CodeLanguage
) {
    AI_AGENT(
        id = "ai_agent",
        title = "AI Coding Agent Workspace",
        description = "Python 3.12 + Aider / Gemini CLI workspace with agent tasks, AST patch scripts, and CLI tooling.",
        iconTag = "AI",
        primaryLanguage = CodeLanguage.PYTHON
    ),
    WEB_FULLSTACK(
        id = "web_fullstack",
        title = "Fullstack Web & API",
        description = "HTML5 modern frontend with Node.js Express backend and live preview support.",
        iconTag = "WEB",
        primaryLanguage = CodeLanguage.JAVASCRIPT
    ),
    PYTHON_FASTAPI(
        id = "python_fastapi",
        title = "Python FastAPI Backend",
        description = "High-performance REST API with Pydantic models, uvicorn server, and unit tests.",
        iconTag = "PY",
        primaryLanguage = CodeLanguage.PYTHON
    ),
    RUST_CLI(
        id = "rust_cli",
        title = "Rust Systems Project",
        description = "Cargo project with fast command-line utilities and algorithmic modules.",
        iconTag = "RS",
        primaryLanguage = CodeLanguage.RUST
    ),
    BLANK(
        id = "blank",
        title = "Empty Workspace",
        description = "Clean project root ready for custom scratchpad or imported files.",
        iconTag = "DIR",
        primaryLanguage = CodeLanguage.TEXT
    )
}

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val template: ProjectTemplate = ProjectTemplate.AI_AGENT,
    val fileCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)
