package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CodeLanguage
import com.example.model.CodingAgentAction
import com.example.model.CodingAgentGroup
import com.example.model.CodingAgentOptionsCatalog
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.model.TerminalLine
import com.example.model.TerminalLineType
import com.example.model.TerminalTabSession
import com.example.ui.editor.SyntaxHighlighter
import com.example.ui.theme.SmartBorder
import com.example.ui.theme.SmartDarkBg
import com.example.ui.theme.SmartError
import com.example.ui.theme.SmartKeyBarBg
import com.example.ui.theme.SmartKeyBg
import com.example.ui.theme.SmartPillActive
import com.example.ui.theme.SmartPrimary
import com.example.ui.theme.SmartSuccess
import com.example.ui.theme.SmartSurface
import com.example.ui.theme.SmartSurfaceElevated
import com.example.ui.theme.SmartTerminalBg
import com.example.ui.theme.SmartText
import com.example.ui.theme.SmartTextMuted
import com.example.ui.theme.SmartTextSubtle

@Composable
fun SmartIdeEditorWorkspace(
    project: Project,
    files: List<ProjectFile>,
    openTabs: List<String>,
    activeFilePath: String?,
    activeContent: String,
    activeLanguage: CodeLanguage,
    terminalTabs: List<TerminalTabSession>,
    activeTerminalSessionId: String,
    webPreviewHtml: String = "",
    onRefreshPreview: () -> Unit = {},
    onBackToProjects: () -> Unit,
    onFileSelected: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onSaveFile: () -> Unit,
    onRunCode: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPreview: () -> Unit,
    // File Management
    onNewItemCreated: (name: String, parentPath: String, isDir: Boolean) -> Unit,
    onItemRenamed: (oldPath: String, newName: String) -> Unit,
    onItemDeleted: (String) -> Unit,
    // Terminal Actions
    onSelectTerminalTab: (String) -> Unit,
    onNewTerminalTab: (shellType: String) -> Unit,
    onCloseTerminalTab: (String) -> Unit,
    onCommandInputChanged: (String) -> Unit,
    onExecuteCommand: (String?) -> Unit,
    onNavigateHistory: (Boolean) -> Unit,
    onLaunchAgentAction: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isFileTreeOpen by remember { mutableStateOf(true) }
    var isTerminalOpen by remember { mutableStateOf(true) }
    var isWebPreviewOpen by remember { mutableStateOf(false) }
    var isAgentOptionsModalOpen by remember { mutableStateOf(false) }
    var selectedAgentGroup by remember { mutableStateOf<CodingAgentGroup?>(null) }

    var editorTextFieldValue by remember(activeFilePath) {
        mutableStateOf(TextFieldValue(activeContent, TextRange(0)))
    }

    LaunchedEffect(activeContent) {
        if (editorTextFieldValue.text != activeContent) {
            editorTextFieldValue = editorTextFieldValue.copy(text = activeContent)
        }
    }

    val linesCount by remember(editorTextFieldValue.text) {
        derivedStateOf {
            maxOf(editorTextFieldValue.text.count { it == '\n' } + 1, 1)
        }
    }

    val activeTerminalSession = remember(terminalTabs, activeTerminalSessionId) {
        terminalTabs.firstOrNull { it.id == activeTerminalSessionId } ?: terminalTabs.firstOrNull()
    }
    val terminalLines = activeTerminalSession?.lines ?: emptyList()
    val terminalListState = rememberLazyListState()

    LaunchedEffect(terminalLines.size) {
        if (terminalLines.isNotEmpty()) {
            terminalListState.animateScrollToItem(terminalLines.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SmartDarkBg)
            .testTag("smart_ide_workspace")
    ) {
        // 1. Top Navigation Bar matching Screenshot 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SmartDarkBg)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Back button & File Tree Toggle Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBackToProjects,
                    modifier = Modifier.size(36.dp).testTag("editor_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Projects",
                        tint = SmartText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // File Explorer Folder Toggle in rounded pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isFileTreeOpen) SmartPillActive else SmartSurface)
                        .clickable { isFileTreeOpen = !isFileTreeOpen }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("toggle_file_tree_pill"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Toggle Files",
                        tint = if (isFileTreeOpen) SmartPrimary else SmartTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Center / Right action buttons matching Screenshot 2:
            // [▶ Run] [🌐 Preview] [🤖 Agents] [>_ Terminal] [⚙ Settings]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Play / Run
                IconButton(
                    onClick = {
                        if (activeFilePath?.endsWith(".html") == true) {
                            onRefreshPreview()
                            isWebPreviewOpen = true
                        } else {
                            onRunCode()
                        }
                    },
                    modifier = Modifier.size(36.dp).testTag("editor_run_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run Script",
                        tint = SmartText,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Prominent Live Web Preview Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isWebPreviewOpen) SmartPillActive else Color.Transparent)
                        .clickable {
                            isWebPreviewOpen = !isWebPreviewOpen
                            if (isWebPreviewOpen) {
                                onRefreshPreview()
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("editor_live_preview_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Live Web Preview",
                            tint = if (isWebPreviewOpen) SmartPrimary else SmartTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Preview",
                            color = if (isWebPreviewOpen) SmartPrimary else SmartTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Installed Coding Agents Multi-Option Launcher Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SmartPillActive)
                        .clickable {
                            selectedAgentGroup = CodingAgentOptionsCatalog.availableAgentGroups.firstOrNull()
                            isAgentOptionsModalOpen = true
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("editor_coding_agents_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Coding Agents",
                            tint = SmartPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "Agents",
                            color = SmartPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Terminal Toggle in active pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isTerminalOpen) SmartPillActive else Color.Transparent)
                        .clickable { isTerminalOpen = !isTerminalOpen }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("toggle_terminal_split_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Toggle Terminal",
                        tint = if (isTerminalOpen) SmartPrimary else SmartTextMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Settings Gear
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp).testTag("editor_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SmartTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 2. Main Body: Left File Tree (optional) + Editor / Preview Canvas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left File Tree (Drawer / Pane)
            if (isFileTreeOpen) {
                FileManagerDrawer(
                    projectName = project.name,
                    files = files,
                    activeFilePath = activeFilePath,
                    onFileSelected = { path ->
                        onFileSelected(path)
                        isWebPreviewOpen = false
                    },
                    onNewItemCreated = onNewItemCreated,
                    onItemRenamed = onItemRenamed,
                    onItemDeleted = onItemDeleted,
                    onCloseSidebar = { isFileTreeOpen = false },
                    modifier = Modifier.width(260.dp)
                )

                // Drag Handle with 3 vertical dots
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(12.dp)
                        .background(SmartDarkBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = SmartTextSubtle,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Editor & Terminal Split Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Top Tab Bar inside Editor matching Screenshot 2: active tab has blue underline
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SmartDarkBg)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    openTabs.forEach { tabPath ->
                        val isActive = tabPath == activeFilePath && !isWebPreviewOpen
                        val fileName = tabPath.substringAfterLast("/")

                        Column(
                            modifier = Modifier
                                .clickable {
                                    onFileSelected(tabPath)
                                    isWebPreviewOpen = false
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("tab_$fileName")
                        ) {
                            Text(
                                text = fileName,
                                color = if (isActive) SmartPrimary else SmartTextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Active underline indicator
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(36.dp)
                                    .background(if (isActive) SmartPrimary else Color.Transparent)
                            )
                        }
                    }

                    // Optional Web Preview Tab if open
                    if (isWebPreviewOpen) {
                        Column(
                            modifier = Modifier
                                .clickable { isWebPreviewOpen = true }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("tab_live_preview")
                        ) {
                            Text(
                                text = "🌐 Live Preview",
                                color = SmartPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(60.dp)
                                    .background(SmartPrimary)
                            )
                        }
                    }
                }

                // Workspace Center: Either Live Web Preview or Code Editor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (isTerminalOpen) 0.55f else 1f)
                        .background(SmartDarkBg)
                ) {
                    if (isWebPreviewOpen) {
                        WebPreviewView(
                            htmlContent = webPreviewHtml,
                            onRefresh = onRefreshPreview,
                            onBack = { isWebPreviewOpen = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Line numbers gutter
                            Column(
                                modifier = Modifier
                                    .width(38.dp)
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                for (i in 1..linesCount) {
                                    Text(
                                        text = "$i",
                                        color = SmartTextSubtle,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Text Field with Syntax Highlighting
                            val syntaxTransform = remember(activeLanguage) {
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
                                    .padding(vertical = 8.dp)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                BasicTextField(
                                    value = editorTextFieldValue,
                                    onValueChange = { newVal ->
                                        editorTextFieldValue = newVal
                                        if (newVal.text != activeContent) {
                                            onContentChanged(newVal.text)
                                        }
                                    },
                                    textStyle = TextStyle(
                                        color = SmartText,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    ),
                                    cursorBrush = SolidColor(SmartPrimary),
                                    visualTransformation = syntaxTransform,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("smart_code_editor_input")
                                )
                            }
                        }
                    }
                }

                // Middle Quick Symbol Accessory Bar matching Screenshot 2:
                // ->  =  .  ''  ""  ()  {}  []  <  >  /  :  ;  _
                val editorSymbols = listOf("->", "=", ".", "''", "\"\"", "()", "{}", "[]", "<", ">", "/", ":", ";", "_")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SmartKeyBarBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    editorSymbols.forEach { sym ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SmartKeyBg)
                                .clickable {
                                    val cursor = editorTextFieldValue.selection.start
                                    val current = editorTextFieldValue.text
                                    val newText = StringBuilder(current).insert(cursor, sym).toString()
                                    val newCursor = cursor + sym.length
                                    editorTextFieldValue = TextFieldValue(newText, TextRange(newCursor))
                                    onContentChanged(newText)
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("sym_$sym")
                        ) {
                            Text(
                                text = sym,
                                color = SmartText,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Split Screen Divider with 3 dots `•••` matching Screenshot 2
                if (isTerminalOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .background(SmartDarkBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "•••",
                            color = SmartTextSubtle,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    // Integrated Terminal Panel (Split View) matching Screenshot 2
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.45f)
                            .background(SmartTerminalBg)
                    ) {
                        // Terminal Tab bar: `Terminal 1 ×` ... `+` `[🤖 Agents]` `×`
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SmartDarkBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Terminal tabs list
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                terminalTabs.forEachIndexed { idx, session ->
                                    val isSessionActive = session.id == activeTerminalSession?.id

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSessionActive) SmartPillActive else Color.Transparent)
                                            .clickable { onSelectTerminalTab(session.id) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Terminal ${idx + 1}",
                                            color = if (isSessionActive) SmartText else SmartTextSubtle,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (isSessionActive) FontWeight.Bold else FontWeight.Normal
                                        )

                                        IconButton(
                                            onClick = { onCloseTerminalTab(session.id) },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = SmartTextSubtle,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Right controls: `+`, `Agents`, and `×` (collapse terminal)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SmartSurface)
                                        .clickable {
                                            selectedAgentGroup = CodingAgentOptionsCatalog.availableAgentGroups.firstOrNull()
                                            isAgentOptionsModalOpen = true
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("term_tab_agents_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "AI Agent",
                                            tint = SmartPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "Agents",
                                            color = SmartPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onNewTerminalTab("bash") },
                                    modifier = Modifier.size(28.dp).testTag("terminal_add_tab_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Terminal", tint = SmartTextMuted, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = { isTerminalOpen = false },
                                    modifier = Modifier.size(28.dp).testTag("terminal_collapse_btn")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Terminal Split", tint = SmartTextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Terminal Log Screen with green prompt matching Screenshot 2
                        LazyColumn(
                            state = terminalListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            items(terminalLines, key = { it.id }) { line ->
                                Text(
                                    text = line.text,
                                    color = when (line.type) {
                                        TerminalLineType.INPUT -> SmartPrimary
                                        TerminalLineType.OUTPUT -> SmartText
                                        TerminalLineType.ERROR -> SmartError
                                        TerminalLineType.SUCCESS -> SmartSuccess
                                        TerminalLineType.SYSTEM -> SmartTextSubtle
                                    },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }

                            // Active prompt line matching Screenshot 2: `~/projects/<name> $ ` with block cursor
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "~/projects/${project.name} $ ",
                                        color = SmartSuccess,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )

                                    var currentInput by remember(activeTerminalSession?.id) {
                                        mutableStateOf(activeTerminalSession?.commandInput ?: "")
                                    }

                                    LaunchedEffect(activeTerminalSession?.commandInput) {
                                        val incoming = activeTerminalSession?.commandInput ?: ""
                                        if (currentInput != incoming) {
                                            currentInput = incoming
                                        }
                                    }

                                    BasicTextField(
                                        value = currentInput,
                                        onValueChange = { newVal ->
                                            if (newVal.contains('\n')) {
                                                val cmd = newVal.replace("\n", "").trim()
                                                if (cmd.isNotEmpty()) {
                                                    onExecuteCommand(cmd)
                                                } else {
                                                    onExecuteCommand(null)
                                                }
                                                currentInput = ""
                                                onCommandInputChanged("")
                                            } else {
                                                currentInput = newVal
                                                onCommandInputChanged(newVal)
                                            }
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = SmartText,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp
                                        ),
                                        cursorBrush = SolidColor(Color.White),
                                        keyboardOptions = KeyboardOptions(
                                            autoCorrect = false,
                                            keyboardType = KeyboardType.Ascii,
                                            imeAction = ImeAction.Go
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onGo = {
                                                val cmd = currentInput.trim()
                                                onExecuteCommand(cmd.ifEmpty { null })
                                                currentInput = ""
                                                onCommandInputChanged("")
                                            },
                                            onDone = {
                                                val cmd = currentInput.trim()
                                                onExecuteCommand(cmd.ifEmpty { null })
                                                currentInput = ""
                                                onCommandInputChanged("")
                                            },
                                            onSend = {
                                                val cmd = currentInput.trim()
                                                onExecuteCommand(cmd.ifEmpty { null })
                                                currentInput = ""
                                                onCommandInputChanged("")
                                            }
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("inline_terminal_input")
                                    )
                                }
                            }
                        }

                        // Bottom Extended Terminal Keyboard matching Screenshot 2:
                        // Row 1: ESC  /  —  HOME  ↑  END  PGUP
                        // Row 2: TAB  CTRL  ALT  ←  ↓  →  ↵ (ENTER)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SmartKeyBarBg)
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val row1 = listOf("ESC", "/", "—", "HOME", "↑", "END", "PGUP")
                            val row2 = listOf("TAB", "CTRL", "ALT", "←", "↓", "→", "↵")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                row1.forEach { key ->
                                    TerminalVirtualKey(key = key) {
                                        when (key) {
                                            "↑" -> onNavigateHistory(true)
                                            "ESC" -> onExecuteCommand("^C")
                                            else -> onCommandInputChanged((activeTerminalSession?.commandInput ?: "") + key)
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                row2.forEach { key ->
                                    TerminalVirtualKey(key = key) {
                                        when (key) {
                                            "↓" -> onNavigateHistory(false)
                                            "TAB" -> onCommandInputChanged((activeTerminalSession?.commandInput ?: "") + "    ")
                                            "↵" -> {
                                                val cmd = (activeTerminalSession?.commandInput ?: "").trim()
                                                onExecuteCommand(cmd.ifEmpty { null })
                                                onCommandInputChanged("")
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Multiple Options for Installed Coding Agents
    if (isAgentOptionsModalOpen) {
        Dialog(onDismissRequest = { isAgentOptionsModalOpen = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("coding_agents_options_dialog"),
                color = SmartSurfaceElevated,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SmartPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Installed Coding Agents",
                                color = SmartText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { isAgentOptionsModalOpen = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = SmartTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    Text(
                        text = "Installed in Shared PRoot App Root (Active in ~/projects/${project.name})",
                        color = SmartTextSubtle,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Agent Horizontal Selector Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CodingAgentOptionsCatalog.availableAgentGroups.forEach { group ->
                            val isSel = group.agentId == selectedAgentGroup?.agentId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SmartPrimary else SmartSurface)
                                    .clickable { selectedAgentGroup = group }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("agent_tab_${group.agentId}")
                            ) {
                                Text(
                                    text = group.name,
                                    color = if (isSel) Color.White else SmartText,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Display Selected Agent Info & Action Options
                    val currentGroup = selectedAgentGroup ?: CodingAgentOptionsCatalog.availableAgentGroups.first()
                    Text(
                        text = currentGroup.description,
                        color = SmartTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Text(
                        text = "Available Launch Options:",
                        color = SmartText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentGroup.actions, key = { it.id }) { action ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SmartSurface),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isAgentOptionsModalOpen = false
                                        isTerminalOpen = true
                                        onLaunchAgentAction(action.command)
                                    }
                                    .testTag("agent_action_${action.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = action.title,
                                                color = SmartText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (action.isInteractive) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(SmartPillActive)
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "Shell",
                                                        color = SmartPrimary,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = action.description,
                                            color = SmartTextSubtle,
                                            fontSize = 11.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$ ${action.command}",
                                            color = SmartSuccess,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Run",
                                        tint = SmartPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isAgentOptionsModalOpen = false }) {
                            Text("Close", color = SmartPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalVirtualKey(key: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("term_key_$key"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key,
            color = if (key == "↵") SmartPrimary else SmartText,
            fontSize = if (key == "↵") 13.sp else 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
