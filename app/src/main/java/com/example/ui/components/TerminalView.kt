package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TerminalLine
import com.example.model.TerminalLineType
import com.example.model.TerminalTabSession
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCrust
import com.example.ui.theme.IdeError
import com.example.ui.theme.IdePrimary
import com.example.ui.theme.IdeSecondary
import com.example.ui.theme.IdeSuccess
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeSurfaceElevated
import com.example.ui.theme.IdeText
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextSubtle

@Composable
fun TerminalView(
    terminalTabs: List<TerminalTabSession>,
    activeSessionId: String,
    onSelectTab: (String) -> Unit,
    onNewTerminalTab: (shellType: String) -> Unit,
    onCloseTerminalTab: (String) -> Unit,
    onCommandInputChanged: (String) -> Unit,
    onExecuteCommand: (String?) -> Unit,
    onClearTerminal: () -> Unit,
    onNavigateHistory: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSession = remember(terminalTabs, activeSessionId) {
        terminalTabs.firstOrNull { it.id == activeSessionId } ?: terminalTabs.firstOrNull()
    }
    val listState = rememberLazyListState()
    var newTerminalMenuOpen by remember { mutableStateOf(false) }

    val currentLines = activeSession?.lines ?: emptyList()
    val prompt = activeSession?.prompt ?: "root@ubuntu:~/workspace# "
    val commandInput = activeSession?.commandInput ?: ""

    // Auto-scroll to bottom on new output in active session
    LaunchedEffect(currentLines.size, activeSessionId) {
        if (currentLines.isNotEmpty()) {
            listState.animateScrollToItem(currentLines.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IdeCrust)
            .testTag("terminal_view")
    ) {
        // VS Code Multi-Terminal Tabs Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeSurface)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            terminalTabs.forEachIndexed { index, session ->
                val isActive = session.id == activeSession?.id
                val tabBg = if (isActive) IdeCrust else IdeSurface
                val textColor = if (isActive) IdePrimary else IdeTextMuted

                Row(
                    modifier = Modifier
                        .background(tabBg)
                        .clickable { onSelectTab(session.id) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("terminal_tab_${session.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = if (isActive) IdeSuccess else IdeTextSubtle,
                        modifier = Modifier.size(14.dp)
                    )

                    Text(
                        text = "${index + 1}: ${session.title}",
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )

                    if (session.isRunning) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(IdeSuccess)
                        )
                    }

                    // Close terminal tab
                    IconButton(
                        onClick = { onCloseTerminalTab(session.id) },
                        modifier = Modifier.size(16.dp).testTag("close_terminal_${session.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Terminal",
                            tint = IdeTextSubtle,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            // New Terminal Tab (+)
            Box {
                IconButton(
                    onClick = { newTerminalMenuOpen = true },
                    modifier = Modifier.size(32.dp).testTag("new_terminal_tab_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Terminal",
                        tint = IdePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = newTerminalMenuOpen,
                    onDismissRequest = { newTerminalMenuOpen = false },
                    modifier = Modifier.background(IdeSurfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("bash (Ubuntu PRoot)", color = IdeText, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null, tint = IdeSuccess) },
                        onClick = {
                            newTerminalMenuOpen = false
                            onNewTerminalTab("bash")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("python3 (Interactive)", color = IdeText, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = IdePrimary) },
                        onClick = {
                            newTerminalMenuOpen = false
                            onNewTerminalTab("python")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("aider (AI Pair Programmer)", color = IdeText, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null, tint = IdeSecondary) },
                        onClick = {
                            newTerminalMenuOpen = false
                            onNewTerminalTab("aider")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("node (Node.js Shell)", color = IdeText, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null, tint = IdePrimary) },
                        onClick = {
                            newTerminalMenuOpen = false
                            onNewTerminalTab("node")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action icons: Up, Down, Interrupt, Clear
            Row(
                modifier = Modifier.padding(end = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onNavigateHistory(true) },
                    modifier = Modifier.size(26.dp).testTag("terminal_history_up")
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "History Up", tint = IdeTextMuted, modifier = Modifier.size(15.dp))
                }
                IconButton(
                    onClick = { onNavigateHistory(false) },
                    modifier = Modifier.size(26.dp).testTag("terminal_history_down")
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "History Down", tint = IdeTextMuted, modifier = Modifier.size(15.dp))
                }
                IconButton(
                    onClick = { onExecuteCommand("^C") },
                    modifier = Modifier.size(26.dp).testTag("terminal_ctrl_c")
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Interrupt", tint = IdeError, modifier = Modifier.size(15.dp))
                }
                IconButton(
                    onClick = onClearTerminal,
                    modifier = Modifier.size(26.dp).testTag("terminal_clear")
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = IdeTextMuted, modifier = Modifier.size(15.dp))
                }
            }
        }

        // Quick Command Bar
        val quickCommands = listOf(
            "python main.py",
            "help",
            "neofetch",
            "ls -la",
            "distro list",
            "aider",
            "claude",
            "apt update",
            "uname -a"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeSurfaceElevated)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickCommands.forEach { cmd ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(IdeCrust)
                        .clickable { onExecuteCommand(cmd) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .testTag("quick_cmd_$cmd")
                ) {
                    Text(
                        text = cmd,
                        color = IdePrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Terminal Log Canvas
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("terminal_log_output")
        ) {
            items(currentLines, key = { it.id }) { line ->
                val textColor = when (line.type) {
                    TerminalLineType.INPUT -> IdePrimary
                    TerminalLineType.OUTPUT -> IdeText
                    TerminalLineType.ERROR -> IdeError
                    TerminalLineType.SYSTEM -> IdeSecondary
                    TerminalLineType.SUCCESS -> IdeSuccess
                }

                Text(
                    text = line.text,
                    color = textColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (line.type == TerminalLineType.INPUT || line.type == TerminalLineType.SUCCESS) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Divider(color = IdeBorder, thickness = 0.5.dp)

        // Command Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeSurface)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = IdeSuccess,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = commandInput,
                onValueChange = onCommandInputChanged,
                placeholder = { Text("type shell command...", fontSize = 12.sp, color = IdeTextSubtle) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { onExecuteCommand(null) }
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = IdeText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IdePrimary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = IdeCrust,
                    unfocusedContainerColor = IdeCrust
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("terminal_command_input")
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { onExecuteCommand(null) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IdePrimary)
                    .testTag("terminal_execute_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Execute Command",
                    tint = Color(0xFF11111B),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
