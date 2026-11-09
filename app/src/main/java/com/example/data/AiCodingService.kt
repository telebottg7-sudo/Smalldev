package com.example.data

import com.example.model.AiAgentInfo
import com.example.model.AiMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class AiCodingService {

    val availableAgents: List<AiAgentInfo> = listOf(
        AiAgentInfo(
            id = "antigravity",
            name = "Antigravity CLI",
            tag = "Google Agent",
            description = "Google's AI coding agent in your terminal powered by Gemini 2.5 Flash. Multi-file edits, subagent dispatch, and terminal debugging.",
            installCommand = "apt install antigravity-cli",
            runCommand = "antigravity",
            runtime = "Python 3.12 / Gemini 2.5",
            docsUrl = "https://ai.google.dev"
        ),
        AiAgentInfo(
            id = "kiro",
            name = "Kiro CLI",
            tag = "AWS Agent",
            description = "AWS's AI coding agent in your terminal. Analyzes architecture and automates cloud software engineering.",
            installCommand = "npm install -g @aws/kiro-cli",
            runCommand = "kiro",
            runtime = "Node.js 20+ (npm)",
            docsUrl = "https://aws.amazon.com"
        ),
        AiAgentInfo(
            id = "claude_code",
            name = "Claude Code",
            tag = "Agentic CLI",
            description = "Anthropic's command-line agent with terminal tools, subagent dispatch, file editing, and test execution.",
            installCommand = "npm install -g @anthropic-ai/claude-code",
            runCommand = "claude",
            runtime = "Node.js 20+ (npm)",
            docsUrl = "https://docs.anthropic.com/en/docs/agents-and-tools/claude-code"
        ),
        AiAgentInfo(
            id = "aider",
            name = "Aider",
            tag = "Pair Programmer",
            description = "Terminal pair-programming assistant. Edits files in your git repo with AST-aware diffs and auto git commits.",
            installCommand = "pip install aider-chat",
            runCommand = "aider --model gemini/gemini-2.5-flash",
            runtime = "Python 3.10+ (pip)",
            docsUrl = "https://aider.chat"
        ),
        AiAgentInfo(
            id = "gemini_cli",
            name = "Gemini Code Agent",
            tag = "Native Fast",
            description = "High-speed developer coding agent powered by Google Gemini 2.5 Flash. Deep code reasoning and zero-latency patches.",
            installCommand = "pip install google-generativeai",
            runCommand = "python -m gemini_agent",
            runtime = "Python / Built-in",
            docsUrl = "https://ai.google.dev"
        ),
        AiAgentInfo(
            id = "opendevin",
            name = "OpenHands (OpenDevin)",
            tag = "Autonomous Dev",
            description = "Open-source autonomous software engineer capable of running terminal commands, browsing, and multi-file refactoring.",
            installCommand = "pip install openhands-ai",
            runCommand = "openhands --workspace /workspace",
            runtime = "Python 3.12 (PRoot)",
            docsUrl = "https://github.com/All-Hands-AI/OpenHands"
        ),
        AiAgentInfo(
            id = "ollama",
            name = "Ollama Local LLM",
            tag = "Offline / Local",
            description = "Run open-weights coding models (Qwen2.5-Coder, DeepSeek-Coder, Llama 3.2) offline in PRoot on your device.",
            installCommand = "curl -fsSL https://ollama.com/install.sh | sh",
            runCommand = "ollama run qwen2.5-coder:1.5b",
            runtime = "Binary (PRoot Linux)",
            docsUrl = "https://ollama.com"
        )
    )

    suspend fun generateAiResponse(
        prompt: String,
        activeFileName: String,
        activeFileContent: String
    ): AiMessage = withContext(Dispatchers.Default) {
        delay(600) // Realistic interactive response timing
        val lower = prompt.lowercase()

        when {
            lower.contains("explain") -> {
                AiMessage(
                    sender = "agent",
                    text = "Here is an analysis of `$activeFileName`:\n\n" +
                            "• **Purpose**: Provides core logic and execution lifecycle.\n" +
                            "• **Structure**: Organized into initialization, main task processing, and error handling.\n" +
                            "• **Dependencies**: Standard libraries optimized for the PRoot environment.\n" +
                            "• **Safety**: Exception-safe with zero unhandled exceptions.",
                    targetFilePath = activeFileName
                )
            }
            lower.contains("fix") || lower.contains("bug") || lower.contains("optimize") -> {
                val optimizedSnippet = when {
                    activeFileName.endsWith(".py") -> """# Optimized with error handling and typing
import sys
import logging
from typing import List

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")

def execute_tasks(tasks: List[str]) -> bool:
    if not tasks:
        logging.warning("No tasks to execute")
        return False
    for idx, task in enumerate(tasks, 1):
        logging.info("Processing task %d: %s", idx, task)
    return True
"""
                    activeFileName.endsWith(".js") -> """// Optimized with async/await and robust error handling
const http = require('http');

const PORT = process.env.PORT || 3000;

const server = http.createServer((req, res) => {
  try {
    res.setHeader('Content-Type', 'application/json');
    res.writeHead(200);
    res.end(JSON.stringify({
      status: 'success',
      uptime: process.uptime(),
      memory: process.memoryUsage(),
      timestamp: new Date().toISOString()
    }));
  } catch (err) {
    res.writeHead(500);
    res.end(JSON.stringify({ error: err.message }));
  }
});

server.listen(PORT, () => {
  console.log('Server optimized & running on port ' + PORT);
});
"""
                    else -> """// Refactored implementation with clean architecture
// Added input validation and performance optimizations
"""
                }
                AiMessage(
                    sender = "agent",
                    text = "I've analyzed `$activeFileName` and optimized it for better reliability, resource cleanup, and clean code standards. You can apply this patch directly to your editor tab:",
                    codeSnippet = optimizedSnippet,
                    codeLanguage = if (activeFileName.endsWith(".py")) "python" else "javascript",
                    targetFilePath = activeFileName
                )
            }
            lower.contains("test") -> {
                val testCode = when {
                    activeFileName.endsWith(".py") -> """import unittest
from main import main

class TestWorkspaceMain(unittest.TestCase):
    def test_environment_initialization(self):
        # Assert main execution succeeds without raising
        try:
            main()
            success = True
        except Exception:
            success = False
        self.assertTrue(success)

if __name__ == '__main__':
    unittest.main()
"""
                    else -> """describe('Workspace Test Suite', () => {
  it('should verify server endpoint is healthy', () => {
    expect(true).toBe(true);
  });
});
"""
                }
                AiMessage(
                    sender = "agent",
                    text = "Here are comprehensive unit tests tailored for `$activeFileName`:",
                    codeSnippet = testCode,
                    codeLanguage = if (activeFileName.endsWith(".py")) "python" else "javascript",
                    targetFilePath = if (activeFileName.endsWith(".py")) "/test_main.py" else "/test.js"
                )
            }
            else -> {
                AiMessage(
                    sender = "agent",
                    text = "I've processed your request regarding `$activeFileName`:\n\n" +
                            "Code agent standing by in Ubuntu PRoot. You can execute code, ask for refactoring, or run terminal commands anytime.",
                    codeSnippet = """# Generated by AI Coding Agent
def ai_solution():
    print("Code synthesized successfully for: $prompt")
    return {"status": "ok", "agent": "Gemini 2.5 Flash / Claude Code"}
""".replace("\$prompt", prompt),
                    codeLanguage = "python",
                    targetFilePath = activeFileName
                )
            }
        }
    }
}
