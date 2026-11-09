package com.example.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.model.CodeLanguage
import java.util.regex.Pattern

object SyntaxHighlighter {
    private val KEYWORD_COLOR = Color(0xFFCBA6F7) // Mauve
    private val STRING_COLOR = Color(0xFFA6E3A1)  // Green
    private val NUMBER_COLOR = Color(0xFFFAB387)  // Peach
    private val COMMENT_COLOR = Color(0xFF7F849C) // Overlay
    private val TYPE_COLOR = Color(0xFF89B4FA)    // Blue
    private val FUNCTION_COLOR = Color(0xFF89DCEB)// Sky
    private val OPERATOR_COLOR = Color(0xFFF38BA8)// Red

    private val KEYWORDS = setOf(
        "def", "class", "import", "from", "return", "if", "elif", "else", "while", "for", "in",
        "try", "except", "finally", "with", "as", "lambda", "yield", "async", "await", "pass",
        "function", "const", "let", "var", "new", "this", "export", "default", "extends",
        "typeof", "instanceof", "package", "val", "fun", "object", "interface", "override",
        "private", "public", "protected", "fn", "pub", "struct", "impl", "enum", "match",
        "mut", "use", "mod", "trait", "echo", "exit", "case", "esac", "fi", "then", "do", "done"
    )

    fun highlight(code: String, language: CodeLanguage): AnnotatedString {
        if (code.isEmpty()) return buildAnnotatedString { append("") }

        return buildAnnotatedString {
            append(code)

            try {
                // 1. Comments
                val commentPattern = when (language) {
                    CodeLanguage.PYTHON, CodeLanguage.SHELL, CodeLanguage.DOCKER ->
                        Pattern.compile("#.*")
                    CodeLanguage.HTML ->
                        Pattern.compile("<!--.*?-->", Pattern.DOTALL)
                    else ->
                        Pattern.compile("//.*|/\\*.*?\\*/", Pattern.DOTALL)
                }
                val commentMatcher = commentPattern.matcher(code)
                while (commentMatcher.find()) {
                    addStyle(
                        SpanStyle(color = COMMENT_COLOR, fontStyle = FontStyle.Italic),
                        commentMatcher.start(),
                        commentMatcher.end()
                    )
                }

                // 2. Strings
                val stringPattern = Pattern.compile("\".*?\"|'.*?'|`.*?`", Pattern.DOTALL)
                val stringMatcher = stringPattern.matcher(code)
                while (stringMatcher.find()) {
                    addStyle(
                        SpanStyle(color = STRING_COLOR),
                        stringMatcher.start(),
                        stringMatcher.end()
                    )
                }

                // 3. Numbers
                val numberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?\\b")
                val numberMatcher = numberPattern.matcher(code)
                while (numberMatcher.find()) {
                    addStyle(
                        SpanStyle(color = NUMBER_COLOR),
                        numberMatcher.start(),
                        numberMatcher.end()
                    )
                }

                // 4. Keywords
                val wordPattern = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
                val wordMatcher = wordPattern.matcher(code)
                while (wordMatcher.find()) {
                    val word = wordMatcher.group()
                    if (KEYWORDS.contains(word)) {
                        addStyle(
                            SpanStyle(color = KEYWORD_COLOR, fontWeight = FontWeight.SemiBold),
                            wordMatcher.start(),
                            wordMatcher.end()
                        )
                    } else if (word.firstOrNull()?.isUpperCase() == true) {
                        addStyle(
                            SpanStyle(color = TYPE_COLOR, fontWeight = FontWeight.Medium),
                            wordMatcher.start(),
                            wordMatcher.end()
                        )
                    }
                }

                // 5. Function calls: word followed by '('
                val funcPattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?=\\()")
                val funcMatcher = funcPattern.matcher(code)
                while (funcMatcher.find()) {
                    val funcName = funcMatcher.group(1) ?: continue
                    if (!KEYWORDS.contains(funcName)) {
                        addStyle(
                            SpanStyle(color = FUNCTION_COLOR),
                            funcMatcher.start(1),
                            funcMatcher.end(1)
                        )
                    }
                }

                // 6. HTML tags
                if (language == CodeLanguage.HTML) {
                    val tagPattern = Pattern.compile("</?[a-zA-Z0-9_-]+|/?>")
                    val tagMatcher = tagPattern.matcher(code)
                    while (tagMatcher.find()) {
                        addStyle(
                            SpanStyle(color = OPERATOR_COLOR, fontWeight = FontWeight.Bold),
                            tagMatcher.start(),
                            tagMatcher.end()
                        )
                    }
                }
            } catch (e: Exception) {
                // Graceful fallback to plain text if regex times out
            }
        }
    }
}
