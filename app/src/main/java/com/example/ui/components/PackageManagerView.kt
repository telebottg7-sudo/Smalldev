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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PackageEcosystem
import com.example.model.PackageItem
import com.example.ui.theme.SmartBorder
import com.example.ui.theme.SmartDarkBg
import com.example.ui.theme.SmartError
import com.example.ui.theme.SmartPillActive
import com.example.ui.theme.SmartPrimary
import com.example.ui.theme.SmartSuccess
import com.example.ui.theme.SmartSurface
import com.example.ui.theme.SmartSurfaceElevated
import com.example.ui.theme.SmartText
import com.example.ui.theme.SmartTextMuted
import com.example.ui.theme.SmartTextSubtle

data class SmartPluginItem(
    val id: String,
    val name: String,
    val description: String,
    val detailedDescription: String = "",
    val category: String, // Languages, Apps, Web, Frameworks, AI/ML, AI Agents, Tools
    val isPopular: Boolean = false,
    val usedCountText: String? = null,
    val iconColor: Color,
    val iconSymbol: String, // TERM, AI, KIRO, PY, JS, NODE, REACT, RN, C, WEB, CLAUDE, AIDER, GIT, SQL
    val isPinned: Boolean = false,
    val version: String = "1.0.0",
    val installSize: String = "24 MB",
    val cliCommand: String = "",
    val starterTemplateFileName: String? = null,
    val starterTemplateContent: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageManagerView(
    installedPluginKeys: Set<String>,
    installingPluginKeys: Set<String>,
    searchFilter: String,
    onFilterChanged: (String) -> Unit,
    onInstallPlugin: (SmartPluginItem) -> Unit,
    onUninstallPlugin: (String) -> Unit,
    onLaunchInTerminal: (pluginId: String, command: String) -> Unit,
    onUseInProject: (pluginId: String, fileName: String, content: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var pinnedSet by remember { mutableStateOf(setOf("terminal", "antigravity", "kiro", "python")) }
    var selectedPluginForDetails by remember { mutableStateOf<SmartPluginItem?>(null) }

    val categories = listOf(
        "All (885)",
        "Languages (58)",
        "Apps (8)",
        "Web (199)",
        "Frameworks (810)",
        "AI/ML (58)",
        "AI Agents (14)",
        "Tools (45)"
    )

    // Complete catalog matching Screenshot 1 exactly
    val pluginsList = remember {
        listOf(
            SmartPluginItem(
                id = "terminal",
                name = "Terminal",
                description = "Terminal with linux environment.",
                detailedDescription = "Multi-terminal subsystem running rootless PRoot Linux (Ubuntu 24.04 LTS). Supports apt, bash, python, node, and custom CLI tools with persistent storage in shared app root.",
                category = "Tools",
                iconColor = Color.White,
                iconSymbol = "TERM",
                version = "5.4.0",
                installSize = "48 MB",
                cliCommand = "bash"
            ),
            SmartPluginItem(
                id = "antigravity",
                name = "Antigravity CLI",
                description = "Google's AI coding agent in your terminal.",
                detailedDescription = "Autonomous software engineering agent powered by Google Gemini 2.5 Flash. Inspects repo AST, reads multi-file context, writes surgical code diffs, and executes terminal debugging commands.",
                category = "AI Agents",
                iconColor = Color(0xFF4285F4),
                iconSymbol = "AI",
                version = "2.1.0",
                installSize = "32 MB",
                cliCommand = "antigravity"
            ),
            SmartPluginItem(
                id = "kiro",
                name = "Kiro CLI",
                description = "AWS's AI coding agent in your terminal.",
                detailedDescription = "AWS cloud and autonomous development CLI agent. Analyzes codebase architecture, refactors API layers, and streamlines container and microservice workflows directly from the terminal.",
                category = "AI Agents",
                iconColor = Color(0xFF9333EA),
                iconSymbol = "KIRO",
                version = "1.4.2",
                installSize = "28 MB",
                cliCommand = "kiro"
            ),
            SmartPluginItem(
                id = "python",
                name = "Python",
                description = "Create Python application",
                detailedDescription = "Python 3.12.3 runtime in Ubuntu PRoot with pip package manager, virtual environments, and scientific AI libraries (NumPy, Pyrogram, FastAPI, Torch).",
                category = "Languages",
                isPopular = true,
                usedCountText = "Used 1 time",
                iconColor = Color(0xFFFFD43B),
                iconSymbol = "PY",
                version = "3.12.3",
                installSize = "34 MB",
                cliCommand = "python3",
                starterTemplateFileName = "main.py",
                starterTemplateContent = "import sys\n\ndef main():\n    print('Hello from Python 3.12 in SmartCode IDE!')\n\nif __name__ == '__main__':\n    main()\n"
            ),
            SmartPluginItem(
                id = "javascript",
                name = "JavaScript",
                description = "Create JavaScript application",
                detailedDescription = "Modern ECMAScript (ES2024) environment with V8 runtime, async/await, Fetch API, and standard Node.js module resolution.",
                category = "Web",
                isPopular = true,
                usedCountText = "Used 1 time",
                iconColor = Color(0xFFF7DF1E),
                iconSymbol = "JS",
                version = "ES2024",
                installSize = "18 MB",
                cliCommand = "node",
                starterTemplateFileName = "index.js",
                starterTemplateContent = "console.log('Running JavaScript application in SmartCode IDE!');\n"
            ),
            SmartPluginItem(
                id = "nodejs",
                name = "Node.js",
                description = "Create Node.js application",
                detailedDescription = "Node.js v20.14.0 LTS runtime with npm package manager. Fast event-driven I/O for web backends, microservices, and AI CLI agents.",
                category = "Web",
                isPopular = true,
                usedCountText = "Used 1 time",
                iconColor = Color(0xFF22C55E),
                iconSymbol = "NODE",
                version = "20.14.0",
                installSize = "42 MB",
                cliCommand = "node server.js",
                starterTemplateFileName = "server.js",
                starterTemplateContent = "const http = require('http');\nconst PORT = 3000;\nconst server = http.createServer((req, res) => {\n  res.writeHead(200, { 'Content-Type': 'application/json' });\n  res.end(JSON.stringify({ status: 'ok', server: 'Node.js 20' }));\n});\nserver.listen(PORT, () => console.log('Server running on port ' + PORT));\n"
            ),
            SmartPluginItem(
                id = "react",
                name = "React",
                description = "Create React application",
                detailedDescription = "React 19 library for component-driven UI with Hooks, JSX/TSX support, and live fast-refresh bundler in the web previewer.",
                category = "Frameworks",
                isPopular = true,
                usedCountText = "Used 1 time",
                iconColor = Color(0xFF06B6D4),
                iconSymbol = "REACT",
                version = "19.0.0",
                installSize = "38 MB",
                cliCommand = "npm start",
                starterTemplateFileName = "App.jsx",
                starterTemplateContent = "import React, { useState } from 'react';\n\nexport default function App() {\n  const [count, setCount] = useState(0);\n  return (\n    <div style={{ textAlign: 'center', padding: 20 }}>\n      <h1>React in SmartCode</h1>\n      <button onClick={() => setCount(count + 1)}>Count: {count}</button>\n    </div>\n  );\n}\n"
            ),
            SmartPluginItem(
                id = "react_native",
                name = "React Native",
                description = "Create, build and install a React Native app on-device — Run bundles the JS, compiles a debug APK, and installs it.",
                detailedDescription = "On-device mobile development suite. Bundles React Native JavaScript using Metro, compiles debug APK binaries via Android toolchain, and tests components without an external PC.",
                category = "Apps",
                isPopular = true,
                usedCountText = "Used 1 time",
                iconColor = Color(0xFF38BDF8),
                iconSymbol = "RN",
                version = "0.74.2",
                installSize = "120 MB",
                cliCommand = "react-native run-android"
            ),
            SmartPluginItem(
                id = "tcc",
                name = "Tiny C Compiler",
                description = "Create C application",
                detailedDescription = "Ultra-fast C compiler (TCC 0.9.27). Instant compilation and execution of ANSI C scripts directly from the editor without heavy toolchains.",
                category = "Languages",
                iconColor = Color(0xFF64748B),
                iconSymbol = "C",
                version = "0.9.27",
                installSize = "6.5 MB",
                cliCommand = "tcc main.c -run",
                starterTemplateFileName = "main.c",
                starterTemplateContent = "#include <stdio.h>\n\nint main() {\n    printf(\"Hello, World from C in SmartCode IDE!\\n\");\n    return 0;\n}\n"
            ),
            SmartPluginItem(
                id = "web",
                name = "Web",
                description = "HTML5, CSS & Vanilla JS preview template",
                detailedDescription = "Zero-setup responsive web template with interactive DOM elements, CSS styles, and instant integrated WebView browser preview.",
                category = "Web",
                isPopular = true,
                iconColor = Color(0xFFEC4899),
                iconSymbol = "WEB",
                version = "HTML5",
                installSize = "2.1 MB",
                cliCommand = "python3 -m http.server 8000",
                starterTemplateFileName = "index.html",
                starterTemplateContent = "<!DOCTYPE html>\n<html>\n<head>\n  <title>SmartCode App</title>\n  <style>body { background: #0f172a; color: #fff; font-family: sans-serif; padding: 20px; }</style>\n</head>\n<body>\n  <h1>Welcome to SmartCode Web</h1>\n</body>\n</html>\n"
            ),
            SmartPluginItem(
                id = "claude_code",
                name = "Claude Code",
                description = "Anthropic's autonomous coding agent in your terminal.",
                detailedDescription = "Anthropic's command-line agent with terminal tools, subagent dispatch, file editing, and test execution directly inside your PRoot Linux workspace.",
                category = "AI Agents",
                isPopular = true,
                iconColor = Color(0xFFD97706),
                iconSymbol = "CLAUDE",
                version = "1.0.12",
                installSize = "16 MB",
                cliCommand = "claude"
            ),
            SmartPluginItem(
                id = "aider",
                name = "Aider",
                description = "AI pair programming in your terminal.",
                detailedDescription = "Terminal pair programmer that uses Gemini or Claude to edit code across multiple files in your local repository with git commit integration.",
                category = "AI Agents",
                isPopular = true,
                iconColor = Color(0xFF10B981),
                iconSymbol = "AIDER",
                version = "0.54.0",
                installSize = "19 MB",
                cliCommand = "aider"
            )
        )
    }

    val filteredPlugins = remember(pluginsList, searchFilter, selectedCategory) {
        pluginsList.filter { plugin ->
            val matchCategory = if (selectedCategory.startsWith("All")) true
            else selectedCategory.startsWith(plugin.category)
            val matchSearch = if (searchFilter.isBlank()) true
            else plugin.name.contains(searchFilter, ignoreCase = true) ||
                    plugin.description.contains(searchFilter, ignoreCase = true) ||
                    plugin.category.contains(searchFilter, ignoreCase = true)
            matchCategory && matchSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SmartDarkBg)
            .testTag("plugins_page_view")
    ) {
        // Search & Filter Header matching Screenshot 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchFilter,
                onValueChange = onFilterChanged,
                placeholder = { Text("Search plugins...", fontSize = 13.sp, color = SmartTextSubtle) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SmartTextSubtle,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchFilter.isNotEmpty()) {
                        IconButton(onClick = { onFilterChanged("") }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = SmartTextSubtle, modifier = Modifier.size(14.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SmartBorder,
                    unfocusedBorderColor = SmartBorder,
                    focusedTextColor = SmartText,
                    unfocusedTextColor = SmartText,
                    focusedContainerColor = SmartSurface,
                    unfocusedContainerColor = SmartSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("search_plugins_input")
            )

            // Grid View Icon Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SmartPillActive)
                    .clickable {},
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "Grid",
                    tint = SmartText,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Filter List Icon Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SmartPillActive)
                    .clickable {},
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = SmartText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Horizontal Category Filter Chips matching Screenshot 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                val chipBg = if (isSelected) SmartPillActive else SmartSurface
                val txtColor = if (isSelected) SmartPrimary else SmartTextMuted

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(chipBg)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("plugin_cat_$cat"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = SmartPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = cat,
                        color = txtColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Plugins List matching Screenshot 1
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredPlugins, key = { it.id }) { plugin ->
                val isPinned = pinnedSet.contains(plugin.id)
                val isInstalled = installedPluginKeys.contains(plugin.id) ||
                        installedPluginKeys.contains("${plugin.category.lowercase()}:${plugin.id}") ||
                        installedPluginKeys.contains(plugin.name.lowercase())
                val isInstalling = installingPluginKeys.contains(plugin.id)

                SmartPluginRow(
                    plugin = plugin,
                    isPinned = isPinned,
                    isInstalled = isInstalled,
                    isInstalling = isInstalling,
                    onTogglePin = {
                        pinnedSet = if (isPinned) pinnedSet - plugin.id else pinnedSet + plugin.id
                    },
                    onClick = {
                        // Crucial fix: Clicking an item opens the install & details sheet, NOT the code editor!
                        selectedPluginForDetails = plugin
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Modal Details & Installation Sheet
    selectedPluginForDetails?.let { plugin ->
        val isInstalled = installedPluginKeys.contains(plugin.id) ||
                installedPluginKeys.contains("${plugin.category.lowercase()}:${plugin.id}") ||
                installedPluginKeys.contains(plugin.name.lowercase())
        val isInstalling = installingPluginKeys.contains(plugin.id)

        PluginDetailsDialog(
            plugin = plugin,
            isInstalled = isInstalled,
            isInstalling = isInstalling,
            onDismiss = { selectedPluginForDetails = null },
            onInstall = {
                onInstallPlugin(plugin)
            },
            onUninstall = {
                onUninstallPlugin(plugin.id)
            },
            onLaunchTerminal = {
                selectedPluginForDetails = null
                onLaunchInTerminal(plugin.id, plugin.cliCommand)
            },
            onUseInProject = {
                selectedPluginForDetails = null
                val fileName = plugin.starterTemplateFileName ?: "main.${plugin.iconSymbol.lowercase()}"
                val content = plugin.starterTemplateContent ?: "// Starter for ${plugin.name}\n"
                onUseInProject(plugin.id, fileName, content)
            }
        )
    }
}

@Composable
fun SmartPluginRow(
    plugin: SmartPluginItem,
    isPinned: Boolean,
    isInstalled: Boolean,
    isInstalling: Boolean,
    onTogglePin: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
            .testTag("plugin_row_${plugin.id}"),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Squircle Icon Badge matching Screenshot 1
            PluginIconSquircle(symbol = plugin.iconSymbol, color = plugin.iconColor)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Title & Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = plugin.name,
                        color = SmartText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (plugin.isPopular) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SmartPillActive)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NorthEast,
                                contentDescription = null,
                                tint = SmartPrimary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Popular",
                                color = SmartPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Downloaded status indicator badge
                    if (isInstalled) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SmartSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SmartSuccess,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Downloaded",
                                color = SmartSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isInstalling) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SmartPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 1.5.dp,
                                color = SmartPrimary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Installing",
                                color = SmartPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Description
                Text(
                    text = plugin.description,
                    color = SmartTextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                // Category & Usage Tags matching Screenshot 1
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SmartSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = plugin.category,
                            color = SmartTextSubtle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    plugin.usedCountText?.let { countText ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SmartSurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = countText,
                                color = SmartTextSubtle,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Pin Button on the right
        IconButton(
            onClick = onTogglePin,
            modifier = Modifier.size(32.dp).testTag("pin_${plugin.id}")
        ) {
            Icon(
                imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = "Pin",
                tint = if (isPinned) SmartPrimary else SmartTextSubtle.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PluginDetailsDialog(
    plugin: SmartPluginItem,
    isInstalled: Boolean,
    isInstalling: Boolean,
    onDismiss: () -> Unit,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
    onLaunchTerminal: () -> Unit,
    onUseInProject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SmartSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PluginIconSquircle(symbol = plugin.iconSymbol, color = plugin.iconColor)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = plugin.name,
                            color = SmartText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (plugin.isPopular) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SmartPillActive)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Popular", color = SmartPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "${plugin.category} • Version ${plugin.version}",
                        color = SmartTextSubtle,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status Banner / Card
                if (isInstalled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SmartSuccess.copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SmartSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Already Downloaded",
                                    color = SmartSuccess,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Installed in Shared App Root (Ubuntu 24.04 PRoot). This package is globally accessible across all your projects without re-downloading.",
                                color = SmartTextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                } else if (isInstalling) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SmartPillActive)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = SmartPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Downloading & Installing...",
                                    color = SmartPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Unpacking binaries to /data/data/.../shared_rootfs",
                                    color = SmartTextSubtle,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SmartSurfaceElevated)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = SmartPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Available for Download",
                                    color = SmartText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Download size: ${plugin.installSize} • Cached in shared app root",
                                    color = SmartTextSubtle,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Description
                Text(
                    text = plugin.detailedDescription.ifEmpty { plugin.description },
                    color = SmartText,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                // Technical specs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SmartDarkBg)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CLI Command:", color = SmartTextSubtle, fontSize = 11.sp)
                        Text(
                            plugin.cliCommand.ifEmpty { plugin.id },
                            color = SmartPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Storage Target:", color = SmartTextSubtle, fontSize = 11.sp)
                        Text("Shared PRoot App Root", color = SmartTextMuted, fontSize = 11.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Package Size:", color = SmartTextSubtle, fontSize = 11.sp)
                        Text(plugin.installSize, color = SmartTextMuted, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (isInstalled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onLaunchTerminal,
                        colors = ButtonDefaults.buttonColors(containerColor = SmartPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("launch_tool_button")
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Terminal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (plugin.starterTemplateFileName != null) {
                        Button(
                            onClick = onUseInProject,
                            colors = ButtonDefaults.buttonColors(containerColor = SmartSurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("use_in_project_button")
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = SmartPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use in Project", color = SmartPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Button(
                    onClick = onInstall,
                    enabled = !isInstalling,
                    colors = ButtonDefaults.buttonColors(containerColor = SmartPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("install_plugin_button")
                ) {
                    if (isInstalling) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Installing...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Install (${plugin.installSize})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            if (isInstalled) {
                TextButton(
                    onClick = onUninstall,
                    modifier = Modifier.testTag("uninstall_plugin_button")
                ) {
                    Text("Uninstall", color = SmartError, fontSize = 12.sp)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = SmartTextSubtle, fontSize = 12.sp)
                }
            }
        }
    )
}

@Composable
fun PluginIconSquircle(symbol: String, color: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SmartSurfaceElevated),
        contentAlignment = Alignment.Center
    ) {
        when (symbol) {
            "TERM" -> {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            "AI" -> {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color(0xFF38BDF8), fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }
            "KIRO" -> {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF8B5CF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👻", fontSize = 16.sp)
                }
            }
            "PY" -> {
                Text("🐍", fontSize = 22.sp)
            }
            "JS" -> {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF7DF1E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("JS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
            "NODE" -> {
                Text("⬡", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            "REACT", "RN" -> {
                Text("⚛", color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
            "C" -> {
                Text("C", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            "CLAUDE" -> {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFD97706)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("C", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
            "AIDER" -> {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("★", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
