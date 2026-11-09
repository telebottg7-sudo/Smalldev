package com.example

import com.example.model.CodeLanguage
import com.example.model.ProjectFile
import com.example.ui.editor.SyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCodeLanguageDetection() {
        assertEquals(CodeLanguage.PYTHON, CodeLanguage.fromFileName("main.py"))
        assertEquals(CodeLanguage.JAVASCRIPT, CodeLanguage.fromFileName("server.js"))
        assertEquals(CodeLanguage.HTML, CodeLanguage.fromFileName("index.html"))
        assertEquals(CodeLanguage.SHELL, CodeLanguage.fromFileName("agent.sh"))
        assertEquals(CodeLanguage.RUST, CodeLanguage.fromFileName("lib.rs"))
    }

    @Test
    fun testSyntaxHighlighter() {
        val pythonCode = "def test_func():\n    return 'Hello World'"
        val highlighted = SyntaxHighlighter.highlight(pythonCode, CodeLanguage.PYTHON)
        assertNotNull(highlighted)
        assertEquals(pythonCode, highlighted.text)
        assertTrue(highlighted.spanStyles.isNotEmpty())
    }

    @Test
    fun testProjectFileCreation() {
        val file = ProjectFile(
            id = "/main.py",
            name = "main.py",
            path = "/main.py",
            isDirectory = false,
            content = "print('hello')",
            language = CodeLanguage.PYTHON
        )
        assertEquals("main.py", file.name)
        assertEquals(CodeLanguage.PYTHON, file.language)
    }

    @Test
    fun testFileHierarchyParentPaths() {
        fun getParentPath(path: String): String {
            val trimmed = path.trimEnd('/')
            val lastSlash = trimmed.lastIndexOf('/')
            return if (lastSlash <= 0) "/" else trimmed.substring(0, lastSlash)
        }

        assertEquals("/", getParentPath("/main.py"))
        assertEquals("/", getParentPath("/scripts"))
        assertEquals("/scripts", getParentPath("/scripts/agent.sh"))
        assertEquals("/scripts", getParentPath("/scripts/sub"))
        assertEquals("/scripts/sub", getParentPath("/scripts/sub/nested.txt"))
    }
}
