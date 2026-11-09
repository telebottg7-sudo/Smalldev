package com.example.model

enum class AgentInstallStatus {
    NOT_INSTALLED,
    INSTALLING,
    INSTALLED,
    FAILED
}

data class AiAgentInfo(
    val id: String,
    val name: String,
    val tag: String,
    val description: String,
    val installCommand: String,
    val runCommand: String,
    val runtime: String, // "Python 3.12 (pip)", "Node.js (npm)", "Binary", "Built-in"
    val status: AgentInstallStatus = AgentInstallStatus.NOT_INSTALLED,
    val docsUrl: String = "",
    val defaultModel: String = "gemini-2.5-flash",
    val requiredDistro: String = "Ubuntu / Debian"
)

data class AiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user", "agent", "system"
    val text: String,
    val codeSnippet: String? = null,
    val codeLanguage: String? = null,
    val targetFilePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
