package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Project
import com.example.ui.components.AiAgentPanel
import com.example.ui.components.DistroSettingsView
import com.example.ui.components.PackageManagerView
import com.example.ui.components.ProjectsHomeView
import com.example.ui.components.SmartBottomNav
import com.example.ui.components.SmartIdeEditorWorkspace
import com.example.ui.components.SmartNavTab
import com.example.ui.components.WebPreviewView
import com.example.ui.theme.SmartDarkBg
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.IdeActiveTab
import com.example.viewmodel.IdeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SmartCodeIdeApp()
            }
        }
    }
}

@Composable
fun SmartCodeIdeApp(
    viewModel: IdeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var bottomNavTab by remember { mutableStateOf(SmartNavTab.PROJECTS) }
    var isInEditorMode by remember { mutableStateOf(false) }

    LaunchedEffect(state.statusMessage) {
        if (state.statusMessage.isNotBlank() && state.statusMessage != "Ready") {
            snackbarHostState.showSnackbar(state.statusMessage)
        }
    }

    // Android System Back Button Behavior
    BackHandler(enabled = isInEditorMode || state.activeTab != IdeActiveTab.PROJECTS) {
        if (isInEditorMode) {
            isInEditorMode = false
            bottomNavTab = SmartNavTab.PROJECTS
        } else if (state.activeTab != IdeActiveTab.PROJECTS) {
            viewModel.setActiveTab(IdeActiveTab.PROJECTS)
            bottomNavTab = SmartNavTab.PROJECTS
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("smartcode_main_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isInEditorMode) {
                SmartBottomNav(
                    activeTab = bottomNavTab,
                    onTabSelected = { tab ->
                        bottomNavTab = tab
                        when (tab) {
                            SmartNavTab.PROJECTS -> viewModel.setActiveTab(IdeActiveTab.PROJECTS)
                            SmartNavTab.PLUGINS -> viewModel.setActiveTab(IdeActiveTab.PACKAGES)
                            SmartNavTab.AI_AGENT -> viewModel.setActiveTab(IdeActiveTab.AI_AGENT)
                        }
                    }
                )
            }
        },
        containerColor = SmartDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SmartDarkBg)
        ) {
            if (isInEditorMode && state.activeProject != null) {
                // Screen 3: Full SmartIDE Editor with Split Terminal & Virtual Keyboard
                SmartIdeEditorWorkspace(
                    project = state.activeProject!!,
                    files = state.files,
                    openTabs = state.openTabs,
                    activeFilePath = state.activeFilePath,
                    activeContent = state.activeFileContent,
                    activeLanguage = state.activeLanguage,
                    terminalTabs = state.terminalTabs,
                    activeTerminalSessionId = state.activeTerminalSessionId,
                    webPreviewHtml = state.webPreviewHtml,
                    onRefreshPreview = { viewModel.refreshWebPreview() },
                    onBackToProjects = {
                        isInEditorMode = false
                        bottomNavTab = SmartNavTab.PROJECTS
                    },
                    onFileSelected = { viewModel.openFile(it) },
                    onCloseTab = { viewModel.closeTab(it) },
                    onContentChanged = { viewModel.updateEditorContent(it) },
                    onSaveFile = { viewModel.saveActiveFile() },
                    onRunCode = {
                        if (state.activeFilePath?.endsWith(".html") == true) {
                            viewModel.refreshWebPreview()
                            viewModel.setActiveTab(IdeActiveTab.PREVIEW)
                        } else {
                            val rel = state.activeFilePath?.removePrefix("/") ?: "main.py"
                            val runCmd = if (rel.endsWith(".py")) "python $rel"
                            else if (rel.endsWith(".js")) "node $rel"
                            else if (rel.endsWith(".sh")) "bash $rel"
                            else "cat $rel"
                            viewModel.executeTerminalCommand(runCmd)
                        }
                    },
                    onOpenAiAssistant = {
                        isInEditorMode = false
                        bottomNavTab = SmartNavTab.AI_AGENT
                        viewModel.setActiveTab(IdeActiveTab.AI_AGENT)
                    },
                    onOpenSettings = {
                        viewModel.setActiveTab(IdeActiveTab.SETTINGS)
                        isInEditorMode = false
                    },
                    onOpenPreview = {
                        viewModel.refreshWebPreview()
                        viewModel.setActiveTab(IdeActiveTab.PREVIEW)
                    },
                    onNewItemCreated = { name, parent, isDir ->
                        viewModel.createNewFile(name, parent, isDir)
                    },
                    onItemRenamed = { oldPath, newName ->
                        viewModel.renameFile(oldPath, newName)
                    },
                    onItemDeleted = { viewModel.deleteFile(it) },
                    onSelectTerminalTab = { viewModel.selectTerminalTab(it) },
                    onNewTerminalTab = { viewModel.createNewTerminalTab(it) },
                    onCloseTerminalTab = { viewModel.closeTerminalTab(it) },
                    onCommandInputChanged = { viewModel.setCommandInput(it) },
                    onExecuteCommand = { viewModel.executeTerminalCommand(it) },
                    onNavigateHistory = { viewModel.navigateHistory(it) },
                    onLaunchAgentAction = { viewModel.launchAgentAction(it) }
                )
            } else {
                // Main Screens with Bottom Navigation Bar
                when (bottomNavTab) {
                    SmartNavTab.PROJECTS -> {
                        if (state.activeTab == IdeActiveTab.PREVIEW) {
                            WebPreviewView(
                                htmlContent = state.webPreviewHtml,
                                onRefresh = { viewModel.refreshWebPreview() },
                                onBack = {
                                    if (state.activeProject != null) {
                                        isInEditorMode = true
                                        viewModel.setActiveTab(IdeActiveTab.EDITOR)
                                    } else {
                                        viewModel.setActiveTab(IdeActiveTab.PROJECTS)
                                    }
                                }
                            )
                        } else if (state.activeTab == IdeActiveTab.SETTINGS) {
                            DistroSettingsView(
                                distros = state.distros,
                                activeDistro = state.activeDistro,
                                onSwitchDistro = { distro ->
                                    viewModel.switchDistro(distro)
                                }
                            )
                        } else {
                            // Screenshot 3: Projects Home Screen
                            ProjectsHomeView(
                                projects = state.projects,
                                activeProjectId = state.activeProject?.id,
                                onOpenProject = { project ->
                                    viewModel.openProject(project)
                                    isInEditorMode = true
                                },
                                onCreateProject = { name, desc, tpl ->
                                    viewModel.createProject(name, desc, tpl)
                                    isInEditorMode = true
                                },
                                onRenameProject = { id, name ->
                                    viewModel.renameProject(id, name)
                                },
                                onDeleteProject = { id ->
                                    viewModel.deleteProject(id)
                                },
                                onExportZip = { id, onComplete ->
                                    viewModel.exportProjectZip(id, onComplete)
                                }
                            )
                        }
                    }

                    SmartNavTab.PLUGINS -> {
                        // Screenshot 1: Plugins / Packages Screen
                        PackageManagerView(
                            installedPluginKeys = state.installedPluginKeys,
                            installingPluginKeys = state.installingPluginKeys,
                            searchFilter = state.packageFilter,
                            onFilterChanged = { viewModel.setPackageFilter(it) },
                            onInstallPlugin = { plugin ->
                                viewModel.installPlugin(
                                    pluginId = plugin.id,
                                    name = plugin.name,
                                    category = plugin.category,
                                    version = plugin.version,
                                    command = plugin.cliCommand
                                )
                            },
                            onUninstallPlugin = { pluginId ->
                                viewModel.uninstallPlugin(pluginId)
                            },
                            onLaunchInTerminal = { pluginId, cmd ->
                                if (state.activeProject == null && state.projects.isNotEmpty()) {
                                    viewModel.openProject(state.projects.first())
                                }
                                isInEditorMode = true
                                val runCmd = if (cmd.isNotBlank()) cmd else pluginId
                                viewModel.launchPluginInTerminal(pluginId, runCmd)
                            },
                            onUseInProject = { pluginId, fileName, content ->
                                if (state.activeProject == null && state.projects.isNotEmpty()) {
                                    viewModel.openProject(state.projects.first())
                                }
                                isInEditorMode = true
                                viewModel.usePluginInProject(pluginId, fileName, content)
                            }
                        )
                    }

                    SmartNavTab.AI_AGENT -> {
                        // AI Coding Agents & Pair Programmer
                        AiAgentPanel(
                            agents = state.aiAgents,
                            messages = state.aiMessages,
                            inputText = state.aiInputText,
                            isThinking = state.isAiThinking,
                            onInputTextChanged = { viewModel.setAiInputText(it) },
                            onSendMessage = { viewModel.sendAiPrompt(it) },
                            onInstallAgent = { viewModel.installAgent(it) },
                            onApplyCodeToEditor = { snippet ->
                                if (state.activeProject == null && state.projects.isNotEmpty()) {
                                    viewModel.openProject(state.projects.first())
                                }
                                viewModel.applyAiCodeToEditor(snippet)
                                isInEditorMode = true
                            },
                            onLaunchTerminalAgent = { cmd ->
                                if (state.activeProject == null && state.projects.isNotEmpty()) {
                                    viewModel.openProject(state.projects.first())
                                }
                                isInEditorMode = true
                                viewModel.executeTerminalCommand(cmd)
                            }
                        )
                    }
                }
            }
        }
    }
}
