package com.example.model

enum class TerminalLineType {
    INPUT,
    OUTPUT,
    ERROR,
    SYSTEM,
    SUCCESS
}

data class TerminalLine(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT,
    val timestamp: Long = System.currentTimeMillis()
)

data class TerminalTabSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "bash",
    val shellType: String = "bash", // bash, python, node, aider, proot
    val lines: List<TerminalLine> = emptyList(),
    val prompt: String = "~/projects $ ",
    val commandInput: String = "",
    val commandHistory: List<String> = emptyList(),
    val historyIndex: Int = -1,
    val isRunning: Boolean = false,
    val workingDir: String = "~/projects"
)
