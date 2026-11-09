package com.example.model

data class CodingAgentAction(
    val id: String,
    val title: String,
    val description: String,
    val command: String,
    val isInteractive: Boolean = false
)

data class CodingAgentGroup(
    val agentId: String,
    val name: String,
    val tag: String,
    val description: String,
    val defaultCli: String,
    val actions: List<CodingAgentAction>
)

object CodingAgentOptionsCatalog {
    val availableAgentGroups: List<CodingAgentGroup> = listOf(
        CodingAgentGroup(
            agentId = "antigravity",
            name = "Antigravity CLI",
            tag = "Google Gemini 2.5",
            description = "Google's autonomous coding agent with multimodal reasoning and project patch synthesis.",
            defaultCli = "antigravity",
            actions = listOf(
                CodingAgentAction(
                    id = "antigravity_interactive",
                    title = "Interactive Terminal Shell",
                    description = "Launch interactive Antigravity prompt in project directory",
                    command = "antigravity",
                    isInteractive = true
                ),
                CodingAgentAction(
                    id = "antigravity_review",
                    title = "Autonomous Code Review",
                    description = "Analyze syntax, dependencies, and fix project errors",
                    command = "antigravity \"review and fix workspace issues\""
                ),
                CodingAgentAction(
                    id = "antigravity_tests",
                    title = "Generate Unit Tests",
                    description = "Auto-generate test suite for active project files",
                    command = "antigravity --test"
                ),
                CodingAgentAction(
                    id = "antigravity_doc",
                    title = "Generate Project Docs",
                    description = "Write comprehensive README and architecture guide",
                    command = "antigravity \"generate documentation and diagrams\""
                )
            )
        ),
        CodingAgentGroup(
            agentId = "kiro",
            name = "Kiro CLI",
            tag = "AWS Cloud Agent",
            description = "AWS's autonomous engineering agent for cloud architectures, Lambda, and Node.js.",
            defaultCli = "kiro",
            actions = listOf(
                CodingAgentAction(
                    id = "kiro_interactive",
                    title = "Interactive Terminal Shell",
                    description = "Launch interactive Kiro session inside project",
                    command = "kiro",
                    isInteractive = true
                ),
                CodingAgentAction(
                    id = "kiro_audit",
                    title = "Cloud & Security Audit",
                    description = "Audit dependencies, permissions, and cloud posture",
                    command = "kiro --audit"
                ),
                CodingAgentAction(
                    id = "kiro_infra",
                    title = "Synthesize Cloud Deployment",
                    description = "Generate Dockerfile, serverless config, and build scripts",
                    command = "kiro \"synthesize deployment configuration\""
                )
            )
        ),
        CodingAgentGroup(
            agentId = "claude_code",
            name = "Claude Code",
            tag = "Anthropic Agent",
            description = "Agentic CLI with subagent dispatch, file editing, and automated terminal test execution.",
            defaultCli = "claude",
            actions = listOf(
                CodingAgentAction(
                    id = "claude_interactive",
                    title = "Interactive Agent Session",
                    description = "Start interactive Claude Code loop in workspace",
                    command = "claude",
                    isInteractive = true
                ),
                CodingAgentAction(
                    id = "claude_fix",
                    title = "Run Tests & Auto-Fix",
                    description = "Execute tests in PRoot and fix all failing assertions",
                    command = "claude \"run tests and resolve issues\""
                ),
                CodingAgentAction(
                    id = "claude_refactor",
                    title = "Full Workspace Refactor",
                    description = "Clean up code architecture, remove unused code, improve typing",
                    command = "claude \"refactor project for clean architecture\""
                )
            )
        ),
        CodingAgentGroup(
            agentId = "aider",
            name = "Aider AI",
            tag = "Pair Programmer",
            description = "Terminal pair-programming assistant that edits files directly in your git repository.",
            defaultCli = "aider",
            actions = listOf(
                CodingAgentAction(
                    id = "aider_interactive",
                    title = "Interactive Pair Session",
                    description = "Open Aider with Gemini 2.5 Flash in terminal",
                    command = "aider --model gemini/gemini-2.5-flash",
                    isInteractive = true
                ),
                CodingAgentAction(
                    id = "aider_commit",
                    title = "Auto-Commit Assistant",
                    description = "Stage modified files and write sensible git commit messages",
                    command = "aider --commit"
                ),
                CodingAgentAction(
                    id = "aider_architect",
                    title = "Architect New Feature",
                    description = "Plan and scaffold multi-file features in workspace",
                    command = "aider --architect"
                )
            )
        ),
        CodingAgentGroup(
            agentId = "gemini_cli",
            name = "Gemini Code Agent",
            tag = "Fast Reasoning",
            description = "High-speed developer coding agent powered by Google Gemini 2.5 Flash.",
            defaultCli = "gemini",
            actions = listOf(
                CodingAgentAction(
                    id = "gemini_fast_solve",
                    title = "Fast Code Reasoning",
                    description = "High-speed token reasoning and optimal algorithm design",
                    command = "gemini \"analyze and optimize workspace algorithms\""
                ),
                CodingAgentAction(
                    id = "gemini_typing",
                    title = "Add Strict Types & Linters",
                    description = "Synthesize type hints and docstrings for all functions",
                    command = "gemini \"add typing and docstrings\""
                )
            )
        ),
        CodingAgentGroup(
            agentId = "python_repl",
            name = "Python 3 Runtime",
            tag = "Python 3.12 (PRoot)",
            description = "Shared Python 3.12 environment in Ubuntu PRoot with pip packages.",
            defaultCli = "python3",
            actions = listOf(
                CodingAgentAction(
                    id = "py_repl",
                    title = "Interactive Python REPL",
                    description = "Launch Python 3.12 interactive shell",
                    command = "python3",
                    isInteractive = true
                ),
                CodingAgentAction(
                    id = "py_run",
                    title = "Execute Script in Workspace",
                    description = "Run python on the active file in project",
                    command = "python generate_session.py"
                ),
                CodingAgentAction(
                    id = "py_pip_list",
                    title = "List Installed Pip Packages",
                    description = "Inspect packages in shared app root",
                    command = "pip list"
                )
            )
        ),
        CodingAgentGroup(
            agentId = "node_repl",
            name = "Node.js Runtime",
            tag = "Node.js v20 (PRoot)",
            description = "Shared Node.js v20 environment with npm and npx tools.",
            defaultCli = "node",
            actions = listOf(
                CodingAgentAction(
                    id = "node_repl",
                    title = "Interactive Node.js REPL",
                    description = "Launch Node.js v20 interactive shell",
                    command = "node",
                    isInteractive = true
                ),
                CodingAgentAction(
                    id = "node_server",
                    title = "Start Local Server",
                    description = "Run server.js on port 3000 in workspace",
                    command = "node server.js"
                )
            )
        )
    )
}
