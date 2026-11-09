package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiCodingService
import com.example.data.AppDatabase
import com.example.data.PackageCatalog
import com.example.data.ShellExecutionService
import com.example.data.WorkspaceRepository
import com.example.model.AgentInstallStatus
import com.example.model.AiAgentInfo
import com.example.model.AiMessage
import com.example.model.CodeLanguage
import com.example.model.DistroStatus
import com.example.model.DistroType
import com.example.model.LinuxDistro
import com.example.model.PackageEcosystem
import com.example.model.PackageItem
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.model.ProjectTemplate
import com.example.model.TerminalLine
import com.example.model.TerminalLineType
import com.example.model.TerminalTabSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class IdeActiveTab {
    PROJECTS,
    EXPLORER,
    EDITOR,
    TERMINAL,
    AI_AGENT,
    PACKAGES,
    PREVIEW,
    SETTINGS
}

data class IdeUiState(
    val activeTab: IdeActiveTab = IdeActiveTab.PROJECTS,
    val isSidebarExpanded: Boolean = false,

    // Projects
    val projects: List<Project> = emptyList(),
    val activeProject: Project? = null,

    // Files in active project
    val files: List<ProjectFile> = emptyList(),
    val openTabs: List<String> = emptyList(),
    val activeFilePath: String? = null,
    val activeFileContent: String = "",
    val isFileModified: Boolean = false,
    val activeLanguage: CodeLanguage = CodeLanguage.TEXT,
    val isFindReplaceOpen: Boolean = false,
    val findQuery: String = "",
    val replaceWith: String = "",
    val statusMessage: String = "Ready",

    // VS Code Multi-Terminal Tabs
    val terminalTabs: List<TerminalTabSession> = emptyList(),
    val activeTerminalSessionId: String = "",

    // Linux PRoot Distro (Shared App-Root)
    val distros: List<LinuxDistro> = emptyList(),
    val activeDistro: LinuxDistro? = null,

    // AI Agents (Shared App-Root)
    val aiAgents: List<AiAgentInfo> = emptyList(),
    val aiMessages: List<AiMessage> = emptyList(),
    val isAiThinking: Boolean = false,
    val aiInputText: String = "",

    // Package Manager (Shared App-Root)
    val packages: List<PackageItem> = emptyList(),
    val selectedEcosystem: PackageEcosystem = PackageEcosystem.APT,
    val packageFilter: String = "",
    val installingPackages: Set<String> = emptySet(),
    val installedPluginKeys: Set<String> = emptySet(),
    val installingPluginKeys: Set<String> = emptySet(),

    // Web Preview
    val webPreviewHtml: String = ""
)

class IdeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.workspaceDao()
    private val repository = WorkspaceRepository(application, dao)
    private val shellService = ShellExecutionService()
    private val aiService = AiCodingService()

    private val _uiState = MutableStateFlow(IdeUiState())
    val uiState: StateFlow<IdeUiState> = _uiState.asStateFlow()

    private var filesObserverJob: Job? = null

    init {
        initProjects()
        initInitialTerminals()
        initDistros()
        initAiAgents()
        initPackages()
    }

    private fun initProjects() {
        viewModelScope.launch {
            repository.initializeDefaultProjectsIfNeeded()
            repository.projectsFlow.collectLatest { projectList ->
                _uiState.update { current ->
                    val active = current.activeProject?.let { act ->
                        projectList.firstOrNull { it.id == act.id }
                    } ?: projectList.firstOrNull()

                    current.copy(
                        projects = projectList,
                        activeProject = active
                    )
                }

                // If active project is set, observe its files
                _uiState.value.activeProject?.let { proj ->
                    observeProjectFiles(proj.id)
                }
            }
        }
    }

    private fun observeProjectFiles(projectId: String) {
        filesObserverJob?.cancel()
        filesObserverJob = viewModelScope.launch {
            repository.getFilesFlow(projectId).collectLatest { fileList ->
                _uiState.update { current ->
                    val updatedState = current.copy(files = fileList)
                    if (current.activeFilePath == null && fileList.isNotEmpty()) {
                        val defaultFile = fileList.firstOrNull { it.path == "/main.py" }
                            ?: fileList.firstOrNull { it.path == "/index.html" }
                            ?: fileList.firstOrNull { !it.isDirectory }

                        if (defaultFile != null) {
                            updatedState.copy(
                                openTabs = listOf(defaultFile.path),
                                activeFilePath = defaultFile.path,
                                activeFileContent = defaultFile.content,
                                activeLanguage = defaultFile.language,
                                isFileModified = false
                            )
                        } else updatedState
                    } else updatedState
                }
            }
        }
    }

    private fun initInitialTerminals() {
        val initialSession = TerminalTabSession(
            id = "term-1",
            title = "Terminal 1",
            shellType = "bash",
            workingDir = "~/projects",
            prompt = "~/projects $ ",
            lines = emptyList()
        )
        _uiState.update {
            it.copy(
                terminalTabs = listOf(initialSession),
                activeTerminalSessionId = initialSession.id
            )
        }
    }

    private fun initDistros() {
        val defaultDistros = DistroType.values().map { type ->
            LinuxDistro(
                id = type.id,
                type = type,
                status = if (type == DistroType.UBUNTU) DistroStatus.RUNNING else DistroStatus.NOT_INSTALLED,
                isDefault = type == DistroType.UBUNTU,
                installedPackagesCount = if (type == DistroType.UBUNTU) 48 else 0,
                memoryUsageMb = if (type == DistroType.UBUNTU) 180 else 0
            )
        }
        _uiState.update {
            it.copy(
                distros = defaultDistros,
                activeDistro = defaultDistros.firstOrNull { d -> d.type == DistroType.UBUNTU }
            )
        }
    }

    private fun initAiAgents() {
        val agents = aiService.availableAgents
        val initialMessages = listOf(
            AiMessage(
                sender = "system",
                text = "SmartCode AI Coding Agent (Gemini 2.5 & PRoot CLI). Tools and agents are cached in shared app root."
            ),
            AiMessage(
                sender = "agent",
                text = "Hello! I can inspect your project code, apply AST diff patches directly to the editor, or launch CLI agents in any terminal tab. How can I help?"
            )
        )
        _uiState.update { it.copy(aiAgents = agents, aiMessages = initialMessages) }
    }

    private fun initPackages() {
        _uiState.update { it.copy(packages = PackageCatalog.defaultPackages) }
        viewModelScope.launch {
            repository.installedPackagesFlow.collectLatest { installedList ->
                val keys = installedList.map { it.key }.toSet()
                _uiState.update { current ->
                    val updatedPkgs = current.packages.map { pkg ->
                        val key = "${pkg.ecosystem.name.lowercase()}:${pkg.name}"
                        val isInstalled = keys.contains(pkg.name) || keys.contains(key) ||
                                (keys.contains("python") && (pkg.name == "python3" || pkg.name == "python")) ||
                                (keys.contains("nodejs") && (pkg.name == "nodejs" || pkg.name == "node"))
                        pkg.copy(isInstalled = isInstalled)
                    }
                    val updatedAgents = current.aiAgents.map { agent ->
                        val isInstalled = keys.contains(agent.id) || keys.contains(agent.runCommand)
                        if (isInstalled) agent.copy(status = AgentInstallStatus.INSTALLED) else agent
                    }
                    current.copy(
                        installedPluginKeys = keys,
                        packages = updatedPkgs,
                        aiAgents = updatedAgents
                    )
                }
            }
        }
    }

    // --- Projects Management ---

    fun openProject(project: Project) {
        val projWorkingDir = "~/projects/${project.name}"
        val projPrompt = "$projWorkingDir $ "
        _uiState.update { current ->
            val updatedTabs = if (current.terminalTabs.isEmpty()) {
                listOf(
                    TerminalTabSession(
                        id = "term-1",
                        title = "Terminal 1",
                        shellType = "bash",
                        workingDir = projWorkingDir,
                        prompt = projPrompt,
                        lines = emptyList()
                    )
                )
            } else {
                current.terminalTabs.map { tab ->
                    tab.copy(
                        workingDir = projWorkingDir,
                        prompt = projPrompt
                    )
                }
            }
            current.copy(
                activeProject = project,
                activeTab = IdeActiveTab.EDITOR,
                openTabs = emptyList(),
                activeFilePath = null,
                activeFileContent = "",
                isFileModified = false,
                terminalTabs = updatedTabs,
                statusMessage = "Opened ${project.name}"
            )
        }
        observeProjectFiles(project.id)
    }

    fun createProject(name: String, description: String, template: ProjectTemplate) {
        viewModelScope.launch {
            val created = repository.createProject(name, description, template)
            openProject(created)
        }
    }

    fun renameProject(projectId: String, newName: String) {
        viewModelScope.launch {
            repository.renameProject(projectId, newName)
            _uiState.update { it.copy(statusMessage = "Renamed project to '$newName'") }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            _uiState.update { it.copy(statusMessage = "Project deleted") }
        }
    }

    fun exportProjectZip(projectId: String, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            val zipFile = repository.exportProjectToZip(projectId)
            onComplete(zipFile)
            _uiState.update { it.copy(statusMessage = "Exported ZIP: ${zipFile.name}") }
        }
    }

    // --- Navigation & Workspace ---

    fun setActiveTab(tab: IdeActiveTab) {
        _uiState.update { it.copy(activeTab = tab) }
        if (tab == IdeActiveTab.PREVIEW) {
            refreshWebPreview()
        }
    }

    fun toggleSidebar() {
        _uiState.update { it.copy(isSidebarExpanded = !it.isSidebarExpanded) }
    }

    fun openFile(path: String) {
        val projId = _uiState.value.activeProject?.id ?: return
        viewModelScope.launch {
            val file = _uiState.value.files.firstOrNull { it.path == path } ?: return@launch
            if (file.isDirectory) return@launch

            val content = repository.getFileContent(projId, path)
            _uiState.update { current ->
                val tabs = if (current.openTabs.contains(path)) current.openTabs else current.openTabs + path
                current.copy(
                    openTabs = tabs,
                    activeFilePath = path,
                    activeFileContent = content,
                    activeLanguage = file.language,
                    isFileModified = false,
                    activeTab = IdeActiveTab.EDITOR,
                    isSidebarExpanded = false,
                    statusMessage = "Opened ${file.name}"
                )
            }
        }
    }

    fun closeTab(path: String) {
        _uiState.update { current ->
            val updatedTabs = current.openTabs.filter { it != path }
            val nextActive = if (current.activeFilePath == path) updatedTabs.lastOrNull() else current.activeFilePath
            val nextFile = current.files.firstOrNull { it.path == nextActive }
            current.copy(
                openTabs = updatedTabs,
                activeFilePath = nextActive,
                activeFileContent = nextFile?.content ?: "",
                activeLanguage = nextFile?.language ?: CodeLanguage.TEXT,
                isFileModified = false
            )
        }
    }

    fun updateEditorContent(newContent: String) {
        _uiState.update { it.copy(activeFileContent = newContent, isFileModified = true) }
    }

    fun saveActiveFile() {
        val projId = _uiState.value.activeProject?.id ?: return
        val path = _uiState.value.activeFilePath ?: return
        val content = _uiState.value.activeFileContent
        viewModelScope.launch {
            repository.saveFileContent(projId, path, content)
            _uiState.update { it.copy(isFileModified = false, statusMessage = "Saved $path") }
        }
    }

    fun createNewFile(name: String, parentPath: String, isDirectory: Boolean) {
        val projId = _uiState.value.activeProject?.id ?: return
        viewModelScope.launch {
            repository.createFile(projId, name, parentPath, isDirectory)
            if (!isDirectory) {
                val cleanParent = if (parentPath == "/" || parentPath.isEmpty()) "" else parentPath
                val fullPath = if (cleanParent.isEmpty()) "/$name" else "$cleanParent/$name"
                openFile(fullPath)
            }
            _uiState.update { it.copy(statusMessage = "Created ${if (isDirectory) "folder" else "file"} $name") }
        }
    }

    fun renameFile(oldPath: String, newName: String) {
        val projId = _uiState.value.activeProject?.id ?: return
        viewModelScope.launch {
            repository.renameFile(projId, oldPath, newName)
            if (_uiState.value.activeFilePath == oldPath) {
                val parent = oldPath.substringBeforeLast("/").ifEmpty { "/" }
                val cleanParent = if (parent == "/") "" else parent
                val newPath = if (cleanParent.isEmpty()) "/$newName" else "$cleanParent/$newName"
                openFile(newPath)
            }
            _uiState.update { it.copy(statusMessage = "Renamed to '$newName'") }
        }
    }

    fun deleteFile(path: String) {
        val projId = _uiState.value.activeProject?.id ?: return
        viewModelScope.launch {
            repository.deleteFile(projId, path)
            closeTab(path)
            _uiState.update { it.copy(statusMessage = "Deleted $path") }
        }
    }

    // --- VS Code Multi-Terminal Tabs Management ---

    fun createNewTerminalTab(shellType: String = "bash", initialCommand: String? = null) {
        val count = _uiState.value.terminalTabs.size + 1
        val projName = _uiState.value.activeProject?.name ?: "workspace"
        val projWorkingDir = "~/projects/$projName"
        val projPrompt = "$projWorkingDir $ "
        val newSession = TerminalTabSession(
            id = "term-${UUID.randomUUID().toString().take(6)}",
            title = if (shellType == "bash") "Terminal $count" else shellType,
            shellType = shellType,
            workingDir = projWorkingDir,
            prompt = projPrompt,
            lines = emptyList()
        )
        _uiState.update {
            it.copy(
                terminalTabs = it.terminalTabs + newSession,
                activeTerminalSessionId = newSession.id
            )
        }
        if (!initialCommand.isNullOrBlank()) {
            executeTerminalCommand(initialCommand)
        }
    }

    fun launchAgentAction(command: String) {
        val currentTab = _uiState.value.terminalTabs.firstOrNull { it.id == _uiState.value.activeTerminalSessionId }
        if (currentTab == null) {
            createNewTerminalTab("bash", command)
        } else {
            executeTerminalCommand(command)
        }
    }

    fun selectTerminalTab(sessionId: String) {
        _uiState.update { it.copy(activeTerminalSessionId = sessionId) }
    }

    fun closeTerminalTab(sessionId: String) {
        _uiState.update { current ->
            if (current.terminalTabs.size <= 1) {
                // Clear the single remaining tab rather than leaving 0
                val reset = current.terminalTabs.first().copy(
                    lines = listOf(TerminalLine(text = "Terminal cleared.", type = TerminalLineType.SYSTEM))
                )
                current.copy(terminalTabs = listOf(reset))
            } else {
                val remaining = current.terminalTabs.filter { it.id != sessionId }
                val nextActive = if (current.activeTerminalSessionId == sessionId) remaining.last().id else current.activeTerminalSessionId
                current.copy(
                    terminalTabs = remaining,
                    activeTerminalSessionId = nextActive
                )
            }
        }
    }

    fun setCommandInput(input: String) {
        val activeId = _uiState.value.activeTerminalSessionId
        _uiState.update { current ->
            val updated = current.terminalTabs.map {
                if (it.id == activeId) it.copy(commandInput = input) else it
            }
            current.copy(terminalTabs = updated)
        }
    }

    fun executeTerminalCommand(customCommand: String? = null) {
        val activeId = _uiState.value.activeTerminalSessionId
        val currentTab = _uiState.value.terminalTabs.firstOrNull { it.id == activeId }
            ?: _uiState.value.terminalTabs.firstOrNull() ?: return
        val cmd = customCommand ?: currentTab.commandInput
        if (cmd.isBlank()) return

        val projId = _uiState.value.activeProject?.id ?: "default"
        val projectDir = repository.getProjectDir(projId)

        _uiState.update { current ->
            val updated = current.terminalTabs.map {
                if (it.id == activeId) {
                    it.copy(
                        commandInput = "",
                        commandHistory = it.commandHistory + cmd,
                        historyIndex = -1,
                        isRunning = true
                    )
                } else it
            }
            current.copy(terminalTabs = updated)
        }

        viewModelScope.launch {
            shellService.executeCommandInSession(
                rawCommand = cmd,
                projectDir = projectDir,
                currentDirDisplay = currentTab.workingDir,
                onOutput = { line ->
                    _uiState.update { current ->
                        val updated = current.terminalTabs.map {
                            if (it.id == activeId) {
                                if (line.text == "CLEAR_SCREEN") it.copy(lines = emptyList())
                                else it.copy(lines = (it.lines + line).takeLast(500))
                            } else it
                        }
                        current.copy(terminalTabs = updated)
                    }
                },
                onNewDirectory = { newDir ->
                    _uiState.update { current ->
                        val updated = current.terminalTabs.map {
                            if (it.id == activeId) it.copy(workingDir = newDir, prompt = shellService.getPrompt(newDir))
                            else it
                        }
                        current.copy(terminalTabs = updated)
                    }
                }
            )

            _uiState.update { current ->
                val updated = current.terminalTabs.map {
                    if (it.id == activeId) it.copy(isRunning = false) else it
                }
                current.copy(terminalTabs = updated)
            }
        }
    }

    fun clearTerminal() {
        val activeId = _uiState.value.activeTerminalSessionId
        _uiState.update { current ->
            val updated = current.terminalTabs.map {
                if (it.id == activeId) it.copy(lines = emptyList()) else it
            }
            current.copy(terminalTabs = updated)
        }
    }

    fun navigateHistory(up: Boolean) {
        val activeId = _uiState.value.activeTerminalSessionId
        val currentTab = _uiState.value.terminalTabs.firstOrNull { it.id == activeId } ?: return
        val history = currentTab.commandHistory
        if (history.isEmpty()) return

        var newIdx = currentTab.historyIndex
        if (up) {
            newIdx = if (newIdx == -1) history.size - 1 else (newIdx - 1).coerceAtLeast(0)
        } else {
            newIdx = if (newIdx == -1) -1 else if (newIdx >= history.size - 1) -1 else newIdx + 1
        }
        val text = if (newIdx in history.indices) history[newIdx] else ""
        _uiState.update { current ->
            val updated = current.terminalTabs.map {
                if (it.id == activeId) it.copy(historyIndex = newIdx, commandInput = text) else it
            }
            current.copy(terminalTabs = updated)
        }
    }

    // --- AI Agent ---

    fun setAiInputText(text: String) {
        _uiState.update { it.copy(aiInputText = text) }
    }

    fun sendAiPrompt(promptText: String? = null) {
        val text = promptText ?: _uiState.value.aiInputText
        if (text.isBlank()) return

        val userMsg = AiMessage(sender = "user", text = text)
        val currentFileName = _uiState.value.activeFilePath ?: "main.py"
        val currentContent = _uiState.value.activeFileContent

        _uiState.update {
            it.copy(
                aiMessages = it.aiMessages + userMsg,
                aiInputText = "",
                isAiThinking = true
            )
        }

        viewModelScope.launch {
            val response = aiService.generateAiResponse(text, currentFileName, currentContent)
            _uiState.update {
                it.copy(
                    aiMessages = it.aiMessages + response,
                    isAiThinking = false
                )
            }
        }
    }

    fun applyAiCodeToEditor(snippet: String) {
        updateEditorContent(snippet)
        saveActiveFile()
        _uiState.update {
            it.copy(
                activeTab = IdeActiveTab.EDITOR,
                statusMessage = "Applied AI patch to editor"
            )
        }
    }

    fun installAgent(agent: AiAgentInfo) {
        viewModelScope.launch {
            _uiState.update { current ->
                val updated = current.aiAgents.map {
                    if (it.id == agent.id) it.copy(status = AgentInstallStatus.INSTALLING) else it
                }
                current.copy(
                    aiAgents = updated,
                    installingPluginKeys = current.installingPluginKeys + agent.id
                )
            }

            executeTerminalCommand(agent.installCommand)
            delay(1200)

            repository.installPackage(agent.id, agent.name, "AI Agents", "1.0.0")

            _uiState.update { current ->
                val updated = current.aiAgents.map {
                    if (it.id == agent.id) it.copy(status = AgentInstallStatus.INSTALLED) else it
                }
                current.copy(
                    aiAgents = updated,
                    installingPluginKeys = current.installingPluginKeys - agent.id,
                    installedPluginKeys = current.installedPluginKeys + agent.id,
                    statusMessage = "${agent.name} installed in shared app root!"
                )
            }
        }
    }

    // --- Package Manager & Plugin Store (Shared App Root) ---

    fun setSelectedEcosystem(eco: PackageEcosystem) {
        _uiState.update { it.copy(selectedEcosystem = eco) }
    }

    fun setPackageFilter(query: String) {
        _uiState.update { it.copy(packageFilter = query) }
    }

    fun installPlugin(pluginId: String, name: String, category: String, version: String, command: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(installingPluginKeys = it.installingPluginKeys + pluginId) }
            val cmd = if (command.isNotEmpty()) command else "apt install $pluginId"
            executeTerminalCommand(cmd)
            delay(1200)

            repository.installPackage(pluginId, name, category, version)

            _uiState.update { current ->
                current.copy(
                    installingPluginKeys = current.installingPluginKeys - pluginId,
                    installedPluginKeys = current.installedPluginKeys + pluginId,
                    statusMessage = "$name installed successfully in shared app root!"
                )
            }
        }
    }

    fun uninstallPlugin(pluginId: String) {
        viewModelScope.launch {
            repository.removePackage(pluginId)
            _uiState.update { current ->
                current.copy(
                    installedPluginKeys = current.installedPluginKeys - pluginId,
                    statusMessage = "Uninstalled $pluginId from shared app root"
                )
            }
        }
    }

    fun launchPluginInTerminal(pluginId: String, command: String) {
        val targetCmd = if (command.isNotEmpty()) command else pluginId
        executeTerminalCommand(targetCmd)
    }

    fun usePluginInProject(pluginId: String, fileName: String, fileContent: String) {
        val projId = _uiState.value.activeProject?.id ?: "proj-t"
        viewModelScope.launch {
            val existing = dao.getFileByPath(projId, "/$fileName")
            if (existing == null) {
                repository.createFile(projId, fileName, "/", false)
                repository.saveFileContent(projId, "/$fileName", fileContent)
            }
            openFile("/$fileName")
            _uiState.update { it.copy(statusMessage = "Opened $fileName for $pluginId") }
        }
    }

    fun installPackage(pkg: PackageItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(installingPackages = it.installingPackages + pkg.name) }
            val cmd = when (pkg.ecosystem) {
                PackageEcosystem.APT -> "apt install ${pkg.name}"
                PackageEcosystem.PKG -> "pkg install ${pkg.name}"
                PackageEcosystem.PIP -> "pip install ${pkg.name}"
                PackageEcosystem.NPM -> "npm install -g ${pkg.name}"
                PackageEcosystem.CARGO -> "cargo install ${pkg.name}"
            }
            executeTerminalCommand(cmd)
            delay(1000)

            val key = "${pkg.ecosystem.name.lowercase()}:${pkg.name}"
            repository.installPackage(key, pkg.name, pkg.ecosystem.name, pkg.version)

            _uiState.update { current ->
                val updatedPkgs = current.packages.map {
                    if (it.name == pkg.name && it.ecosystem == pkg.ecosystem) it.copy(isInstalled = true) else it
                }
                current.copy(
                    packages = updatedPkgs,
                    installingPackages = current.installingPackages - pkg.name,
                    installedPluginKeys = current.installedPluginKeys + key + pkg.name,
                    statusMessage = "Installed ${pkg.name} to shared app root"
                )
            }
        }
    }

    fun uninstallPackage(pkg: PackageItem) {
        viewModelScope.launch {
            val key = "${pkg.ecosystem.name.lowercase()}:${pkg.name}"
            repository.removePackage(key)
            repository.removePackage(pkg.name)

            _uiState.update { current ->
                val updated = current.packages.map {
                    if (it.name == pkg.name && it.ecosystem == pkg.ecosystem) it.copy(isInstalled = false) else it
                }
                current.copy(
                    packages = updated,
                    installedPluginKeys = current.installedPluginKeys - key - pkg.name,
                    statusMessage = "Uninstalled ${pkg.name}"
                )
            }
        }
    }

    fun switchDistro(distro: LinuxDistro) {
        _uiState.update { current ->
            val updated = current.distros.map {
                if (it.id == distro.id) it.copy(status = DistroStatus.RUNNING, isDefault = true)
                else it.copy(status = DistroStatus.STOPPED, isDefault = false)
            }
            current.copy(
                distros = updated,
                activeDistro = distro.copy(status = DistroStatus.RUNNING, isDefault = true),
                statusMessage = "Switched default PRoot distro to ${distro.type.displayName}"
            )
        }
        executeTerminalCommand("proot -0 -r ${distro.rootfsPath} /bin/bash")
    }

    fun refreshWebPreview() {
        val htmlFile = _uiState.value.files.firstOrNull { it.path == "/index.html" }
        val content = htmlFile?.content ?: "<html><body style='background:#111;color:#fff;'><h1>No index.html found in this project</h1></body></html>"
        _uiState.update { it.copy(webPreviewHtml = content) }
    }

    fun insertQuickKey(key: String) {
        val currentContent = _uiState.value.activeFileContent
        val updated = currentContent + key
        updateEditorContent(updated)
    }

    fun toggleFindReplace() {
        _uiState.update { it.copy(isFindReplaceOpen = !it.isFindReplaceOpen) }
    }

    fun setFindQuery(q: String) {
        _uiState.update { it.copy(findQuery = q) }
    }

    fun setReplaceWith(r: String) {
        _uiState.update { it.copy(replaceWith = r) }
    }

    fun executeReplaceAll() {
        val query = _uiState.value.findQuery
        val replace = _uiState.value.replaceWith
        if (query.isNotEmpty()) {
            val content = _uiState.value.activeFileContent
            val replaced = content.replace(query, replace)
            updateEditorContent(replaced)
            saveActiveFile()
            _uiState.update { it.copy(statusMessage = "Replaced occurrences of '$query'") }
        }
    }
}
