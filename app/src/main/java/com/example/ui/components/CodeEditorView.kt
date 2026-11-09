package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodeLanguage
import com.example.model.ProjectFile
import com.example.ui.editor.SyntaxHighlighter
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCrust
import com.example.ui.theme.IdeEditorBg
import com.example.ui.theme.IdeError
import com.example.ui.theme.IdeKeyBarBg
import com.example.ui.theme.IdeKeyBg
import com.example.ui.theme.IdePrimary
import com.example.ui.theme.IdeSecondary
import com.example.ui.theme.IdeSuccess
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeSurfaceElevated
import com.example.ui.theme.IdeText
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextSubtle
import com.example.ui.theme.IdeWarning

@Composable
fun CodeEditorView(
    openTabs: List<String>,
    activeFilePath: String?,
    activeContent: String,
    isFileModified: Boolean,
    activeLanguage: CodeLanguage,
    files: List<ProjectFile>,
    isFindReplaceOpen: Boolean,
    findQuery: String,
    replaceWith: String,
    onTabSelected: (String) -> Unit,
    onTabClosed: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onSaveRequested: () -> Unit,
    onRunRequested: () -> Unit,
    onAiExplainRequested: () -> Unit,
    onToggleFindReplace: () -> Unit,
    onFindQueryChanged: (String) -> Unit,
    onReplaceWithChanged: (String) -> Unit,
    onReplaceAllRequested: () -> Unit,
    onQuickKeyInserted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(activeFilePath) {
        mutableStateOf(TextFieldValue(activeContent, TextRange(0)))
    }

    // Keep textFieldValue in sync when activeContent changes externally (e.g. AI patch applied or opened file)
    LaunchedEffect(activeContent) {
        if (textFieldValue.text != activeContent) {
            textFieldValue = textFieldValue.copy(text = activeContent)
        }
    }

    val linesCount by remember(textFieldValue.text) {
        derivedStateOf {
            val count = textFieldValue.text.count { it == '\n' } + 1
            maxOf(count, 1)
        }
    }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IdeEditorBg)
            .testTag("code_editor_view")
    ) {
        // 1. Editor Tabs Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeCrust)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            openTabs.forEach { tabPath ->
                val isActive = tabPath == activeFilePath
                val fileName = tabPath.substringAfterLast("/")
                val file = files.firstOrNull { it.path == tabPath }
                val tabBg = if (isActive) IdeEditorBg else IdeCrust
                val textColor = if (isActive) IdeText else IdeTextMuted

                Row(
                    modifier = Modifier
                        .background(tabBg)
                        .clickable { onTabSelected(tabPath) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("editor_tab_$fileName"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (file != null) {
                        FileLanguageIcon(file = file, modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = fileName,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1
                    )

                    if (isActive && isFileModified) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(IdeWarning)
                        )
                    }

                    IconButton(
                        onClick = { onTabClosed(tabPath) },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tab",
                            tint = IdeTextSubtle,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // 2. Editor Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeSurface)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(IdeSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = activeLanguage.displayName,
                        color = IdePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isFileModified) {
                    Text(
                        text = "● Unsaved edits",
                        color = IdeWarning,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Find & Replace
                IconButton(
                    onClick = onToggleFindReplace,
                    modifier = Modifier.size(32.dp).testTag("find_replace_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FindReplace,
                        contentDescription = "Find & Replace",
                        tint = if (isFindReplaceOpen) IdePrimary else IdeTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // AI Explain
                IconButton(
                    onClick = onAiExplainRequested,
                    modifier = Modifier.size(32.dp).testTag("ai_explain_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = IdeSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Save
                IconButton(
                    onClick = onSaveRequested,
                    modifier = Modifier.size(32.dp).testTag("save_file_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save File",
                        tint = if (isFileModified) IdeSuccess else IdeTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Run / Preview
                IconButton(
                    onClick = onRunRequested,
                    modifier = Modifier.size(32.dp).testTag("run_preview_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run or Preview Code",
                        tint = IdeSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 3. Find & Replace Panel (Collapsible)
        AnimatedVisibility(visible = isFindReplaceOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IdeSurfaceElevated)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = findQuery,
                        onValueChange = onFindQueryChanged,
                        placeholder = { Text("Find text...", fontSize = 12.sp, color = IdeTextSubtle) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = IdeText,
                            unfocusedTextColor = IdeText,
                            focusedBorderColor = IdePrimary,
                            unfocusedBorderColor = IdeBorder
                        ),
                        modifier = Modifier.weight(1f).height(44.dp)
                    )
                    OutlinedTextField(
                        value = replaceWith,
                        onValueChange = onReplaceWithChanged,
                        placeholder = { Text("Replace with...", fontSize = 12.sp, color = IdeTextSubtle) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = IdeText,
                            unfocusedTextColor = IdeText,
                            focusedBorderColor = IdePrimary,
                            unfocusedBorderColor = IdeBorder
                        ),
                        modifier = Modifier.weight(1f).height(44.dp)
                    )
                    TextButton(
                        onClick = onReplaceAllRequested,
                        modifier = Modifier.testTag("replace_all_button")
                    ) {
                        Text("Replace All", color = IdePrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Editor Canvas (Line Numbers Gutter + Monospace Text Editor)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(IdeEditorBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
            ) {
                // Line numbers gutter
                Column(
                    modifier = Modifier
                        .width(42.dp)
                        .background(IdeCrust)
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..linesCount) {
                        Text(
                            text = "$i",
                            color = IdeTextSubtle,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Field with Syntax Highlighting Visual Transformation
                val syntaxTransformation = remember(activeLanguage) {
                    VisualTransformation { unstyled ->
                        androidx.compose.ui.text.input.TransformedText(
                            text = SyntaxHighlighter.highlight(unstyled.text, activeLanguage),
                            offsetMapping = androidx.compose.ui.text.input.OffsetMapping.Identity
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            if (newValue.text != activeContent) {
                                onContentChanged(newValue.text)
                            }
                        },
                        textStyle = TextStyle(
                            color = IdeText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(IdePrimary),
                        visualTransformation = syntaxTransformation,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("code_editor_text_input")
                    )
                }
            }
        }

        // 5. Quick Developer Keyboard Accessory Bar
        val quickKeys = listOf(
            "Tab", "{", "}", "(", ")", "[", "]", "<", ">",
            "\"", "'", ":", ";", "=", "+", "-", "*", "/",
            "\\", "$", "#", "!", "&", "|", "->", "=>"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeKeyBarBg)
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickKeys.forEach { key ->
                val actualInsert = if (key == "Tab") "    " else key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(IdeKeyBg)
                        .clickable {
                            val cursor = textFieldValue.selection.start
                            val current = textFieldValue.text
                            val newText = StringBuilder(current).insert(cursor, actualInsert).toString()
                            val newCursor = cursor + actualInsert.length
                            textFieldValue = TextFieldValue(newText, TextRange(newCursor))
                            onContentChanged(newText)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("quick_key_$key"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key,
                        color = IdePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 6. Bottom Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeCrust)
                .padding(horizontal = 10.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SmartCode IDE",
                    color = IdePrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lines: $linesCount",
                    color = IdeTextSubtle,
                    fontSize = 10.sp
                )
                Text(
                    text = "UTF-8",
                    color = IdeTextSubtle,
                    fontSize = 10.sp
                )
            }
            Text(
                text = "Linux PRoot (Ubuntu 24.04)",
                color = IdeSuccess,
                fontSize = 10.sp
            )
        }
    }
}
