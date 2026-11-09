package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgentInstallStatus
import com.example.model.AiAgentInfo
import com.example.model.AiMessage
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCrust
import com.example.ui.theme.IdeEditorBg
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
fun AiAgentPanel(
    agents: List<AiAgentInfo>,
    messages: List<AiMessage>,
    inputText: String,
    isThinking: Boolean,
    onInputTextChanged: (String) -> Unit,
    onSendMessage: (String?) -> Unit,
    onInstallAgent: (AiAgentInfo) -> Unit,
    onApplyCodeToEditor: (String) -> Unit,
    onLaunchTerminalAgent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chatListState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            chatListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IdeCrust)
            .testTag("ai_agent_panel")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeSurface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Coding Agent",
                    tint = IdeSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "AI Coding Agents & PRoot Installer",
                        color = IdeText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Install & run agentic CLIs: Claude Code, Aider, OpenDevin",
                        color = IdeTextSubtle,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Horizontal Agent Catalog Carousel
        Text(
            text = "AVAILABLE AGENTS IN PROOT LINUX",
            color = IdeTextSubtle,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            agents.forEach { agent ->
                AgentCard(
                    agent = agent,
                    onInstall = { onInstallAgent(agent) },
                    onLaunch = { onLaunchTerminalAgent(agent.runCommand) }
                )
            }
        }

        Divider(color = IdeBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

        // Quick Task Prompts
        val quickTasks = listOf(
            "Explain this file",
            "Fix bugs & optimize",
            "Write unit tests",
            "Install CLI Agent",
            "Generate REST API"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickTasks.forEach { task ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .border(1.dp, IdeSecondary.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
                        .clickable { onSendMessage(task) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("quick_task_$task")
                ) {
                    Text(
                        text = task,
                        color = IdeSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = chatListState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                AiMessageBubble(
                    message = msg,
                    onApplyCode = onApplyCodeToEditor
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = IdeSecondary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "AI Coding Agent thinking & analyzing AST...",
                            color = IdeSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IdeSurface)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChanged,
                placeholder = { Text("Ask AI to generate, refactor, or fix code...", fontSize = 12.sp, color = IdeTextSubtle) },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IdeSecondary,
                    unfocusedBorderColor = IdeBorder,
                    focusedTextColor = IdeText,
                    unfocusedTextColor = IdeText,
                    focusedContainerColor = IdeCrust,
                    unfocusedContainerColor = IdeCrust
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input")
            )

            IconButton(
                onClick = { onSendMessage(null) },
                enabled = inputText.isNotBlank() && !isThinking,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (inputText.isNotBlank() && !isThinking) IdeSecondary else IdeBorder)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Prompt",
                    tint = Color(0xFF11111B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AgentCard(
    agent: AiAgentInfo,
    onInstall: () -> Unit,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .testTag("agent_card_${agent.id}"),
        colors = CardDefaults.cardColors(containerColor = IdeSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, IdeBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = agent.name,
                    color = IdeText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(IdeSecondary.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(agent.tag, color = IdeSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = agent.description,
                color = IdeTextMuted,
                fontSize = 11.sp,
                maxLines = 2
            )

            Text(
                text = "Runtime: ${agent.runtime}",
                color = IdeTextSubtle,
                fontSize = 10.sp
            )

            // Install or Launch Button
            when (agent.status) {
                AgentInstallStatus.INSTALLING -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(IdeSurfaceElevated)
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = IdeSecondary, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Installing in PRoot...", color = IdeSecondary, fontSize = 11.sp)
                    }
                }
                AgentInstallStatus.INSTALLED -> {
                    Button(
                        onClick = onLaunch,
                        colors = ButtonDefaults.buttonColors(containerColor = IdeSuccess),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(32.dp).testTag("launch_agent_${agent.id}")
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Launch CLI", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = onInstall,
                        colors = ButtonDefaults.buttonColors(containerColor = IdeSecondary),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(32.dp).testTag("install_agent_${agent.id}")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Install in PRoot", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AiMessageBubble(
    message: AiMessage,
    onApplyCode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "user"
    val isSystem = message.sender == "system"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Icon(
                    imageVector = if (isSystem) Icons.Default.Terminal else Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (isSystem) IdePrimary else IdeSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (isSystem) "System" else "SmartCode AI Agent",
                    color = IdeTextSubtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isUser) IdePrimary.copy(alpha = 0.25f) else IdeSurfaceElevated)
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = message.text,
                    color = IdeText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                // Optional Code Snippet block
                message.codeSnippet?.let { snippet ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(IdeCrust)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message.codeLanguage ?: "code",
                                color = IdePrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = { onApplyCode(snippet) },
                                colors = ButtonDefaults.buttonColors(containerColor = IdeSuccess),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp).testTag("apply_code_button")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Apply to Editor", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = snippet,
                            color = IdeSuccess,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
