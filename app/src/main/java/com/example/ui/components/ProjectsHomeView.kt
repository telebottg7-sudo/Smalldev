package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.model.Project
import com.example.model.ProjectTemplate
import com.example.ui.theme.SmartBorder
import com.example.ui.theme.SmartDarkBg
import com.example.ui.theme.SmartError
import com.example.ui.theme.SmartPillActive
import com.example.ui.theme.SmartPrimary
import com.example.ui.theme.SmartPurple
import com.example.ui.theme.SmartSuccess
import com.example.ui.theme.SmartSurface
import com.example.ui.theme.SmartSurfaceElevated
import com.example.ui.theme.SmartText
import com.example.ui.theme.SmartTextMuted
import com.example.ui.theme.SmartTextSubtle
import java.io.File

enum class CreateProjectMode {
    TEMPLATE,
    GIT_CLONE,
    ZIP_IMPORT
}

@Composable
fun ProjectsHomeView(
    projects: List<Project>,
    activeProjectId: String?,
    onOpenProject: (Project) -> Unit,
    onCreateProject: (name: String, desc: String, template: ProjectTemplate) -> Unit,
    onRenameProject: (projectId: String, newName: String) -> Unit,
    onDeleteProject: (projectId: String) -> Unit,
    onExportZip: (projectId: String, onComplete: (File) -> Unit) -> Unit,
    onCloneGitProject: ((repoUrl: String, customName: String?) -> Unit)? = null,
    onImportZip: ((uri: Uri, customName: String?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isNewProjectDialogOpen by remember { mutableStateOf(false) }
    var projectToRename by remember { mutableStateOf<Project?>(null) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }
    var exportedZipFile by remember { mutableStateOf<File?>(null) }

    var creationMode by remember { mutableStateOf(CreateProjectMode.TEMPLATE) }

    // Template Mode state
    var newProjectName by remember { mutableStateOf("") }
    var newProjectDesc by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(ProjectTemplate.AI_AGENT) }

    // Git Clone Mode state (no name required)
    var gitRepoUrl by remember { mutableStateOf("") }
    var gitCustomName by remember { mutableStateOf("") }

    // ZIP Import Mode state
    var selectedZipUri by remember { mutableStateOf<Uri?>(null) }
    var selectedZipDisplayName by remember { mutableStateOf("") }
    var zipCustomName by remember { mutableStateOf("") }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedZipUri = uri
            var name = ""
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIdx)
                    }
                }
            } catch (_: Exception) {}
            if (name.isBlank()) {
                name = uri.lastPathSegment?.substringAfterLast("/") ?: "imported_project.zip"
            }
            selectedZipDisplayName = name
            zipCustomName = name.removeSuffix(".zip").removeSuffix(".ZIP")
        }
    }

    val filteredProjects = remember(projects, searchQuery) {
        if (searchQuery.isBlank()) projects
        else projects.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SmartDarkBg)
            .testTag("projects_home_view")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar matching Screenshot 3
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smalldev",
                    color = SmartText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Telegram / Community Icon
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/smartide_community"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier.size(34.dp).testTag("header_telegram_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2AABEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Telegram",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Grid / List Toggle Icon
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(34.dp).testTag("header_grid_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Toggle Grid",
                            tint = SmartTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Palette / Theme Icon
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(34.dp).testTag("header_theme_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme Palette",
                            tint = SmartTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Search Bar & Filter Row matching Screenshot 3
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Pill
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search recent projects",
                            color = SmartTextSubtle,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = SmartTextSubtle,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
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
                        .testTag("search_recent_projects_input")
                )

                // Filter Pill Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SmartPillActive)
                        .clickable {}
                        .testTag("filter_projects_btn"),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Projects List matching Screenshot 3 item style
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProjects, key = { it.id }) { project ->
                    SmartProjectItemRow(
                        project = project,
                        onOpen = { onOpenProject(project) },
                        onRename = { projectToRename = project },
                        onDelete = { projectToDelete = project },
                        onExportZip = {
                            onExportZip(project.id) { zipFile ->
                                exportedZipFile = zipFile
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom FAB and nav
                }
            }
        }

        // Floating Action Button matching Screenshot 3 (bottom right)
        FloatingActionButton(
            onClick = {
                newProjectName = ""
                newProjectDesc = ""
                gitRepoUrl = ""
                gitCustomName = ""
                selectedZipUri = null
                selectedZipDisplayName = ""
                zipCustomName = ""
                selectedTemplate = ProjectTemplate.AI_AGENT
                creationMode = CreateProjectMode.TEMPLATE
                isNewProjectDialogOpen = true
            },
            containerColor = SmartPillActive,
            contentColor = SmartText,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(56.dp)
                .testTag("create_project_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Project",
                tint = SmartText,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Comprehensive Dialog: New Project / Git Clone / Import ZIP
    if (isNewProjectDialogOpen) {
        AlertDialog(
            onDismissRequest = { isNewProjectDialogOpen = false },
            title = {
                Text(
                    text = when (creationMode) {
                        CreateProjectMode.TEMPLATE -> "New Project"
                        CreateProjectMode.GIT_CLONE -> "Git Clone Repository"
                        CreateProjectMode.ZIP_IMPORT -> "Import ZIP Project"
                    },
                    color = SmartText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Creation Mode Selector Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SmartDarkBg)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            CreateProjectMode.TEMPLATE to "Template",
                            CreateProjectMode.GIT_CLONE to "Git Clone",
                            CreateProjectMode.ZIP_IMPORT to "Import ZIP"
                        ).forEach { (mode, label) ->
                            val isSel = creationMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) SmartPillActive else Color.Transparent)
                                    .clickable { creationMode = mode }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) SmartPrimary else SmartTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Content depending on selected creation mode
                    when (creationMode) {
                        CreateProjectMode.TEMPLATE -> {
                            OutlinedTextField(
                                value = newProjectName,
                                onValueChange = { newProjectName = it },
                                label = { Text("Project Name") },
                                placeholder = { Text("e.g. t, telegram-bot, web-api") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = SmartText,
                                    unfocusedTextColor = SmartText,
                                    focusedBorderColor = SmartPrimary,
                                    unfocusedBorderColor = SmartBorder
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("new_project_name_input")
                            )

                            OutlinedTextField(
                                value = newProjectDesc,
                                onValueChange = { newProjectDesc = it },
                                label = { Text("Description (Optional)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = SmartText,
                                    unfocusedTextColor = SmartText,
                                    focusedBorderColor = SmartPrimary,
                                    unfocusedBorderColor = SmartBorder
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("new_project_desc_input")
                            )

                            Text("Template:", color = SmartTextSubtle, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ProjectTemplate.values()
                                    .filter { it != ProjectTemplate.GIT_CLONE && it != ProjectTemplate.ZIP_IMPORT }
                                    .forEach { tpl ->
                                        val isSelected = tpl == selectedTemplate
                                        val bg = if (isSelected) SmartPrimary else SmartSurfaceElevated
                                        val txt = if (isSelected) Color(0xFF101118) else SmartText

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(bg)
                                                .clickable { selectedTemplate = tpl }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                .testTag("template_chip_${tpl.id}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(tpl.title, color = txt, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                            }
                        }

                        CreateProjectMode.GIT_CLONE -> {
                            // Git Clone Mode (no name required)
                            val autoDerivedName = remember(gitRepoUrl) {
                                if (gitRepoUrl.isNotBlank()) {
                                    val trimmed = gitRepoUrl.trim().removeSuffix("/").removeSuffix(".git")
                                    val n = trimmed.substringAfterLast("/").substringAfterLast(":")
                                    n.replace("[^a-zA-Z0-9._-]".toRegex(), "_").ifBlank { "git-project" }
                                } else ""
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = SmartDarkBg),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = SmartPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "No project name required",
                                            color = SmartPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (autoDerivedName.isNotEmpty()) "Will be created as: ~/projects/$autoDerivedName"
                                            else "Project name is auto-derived from the Git repository URL.",
                                            color = SmartTextSubtle,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = gitRepoUrl,
                                onValueChange = { gitRepoUrl = it },
                                label = { Text("Git Repository URL") },
                                placeholder = { Text("https://github.com/user/repo.git") },
                                singleLine = true,
                                trailingIcon = {
                                    if (gitRepoUrl.isNotEmpty()) {
                                        IconButton(onClick = { gitRepoUrl = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = SmartTextSubtle, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = SmartText,
                                    unfocusedTextColor = SmartText,
                                    focusedBorderColor = SmartPrimary,
                                    unfocusedBorderColor = SmartBorder
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("git_repo_url_input")
                            )

                            OutlinedTextField(
                                value = gitCustomName,
                                onValueChange = { gitCustomName = it },
                                label = { Text("Custom Project Name (Optional)") },
                                placeholder = { Text(if (autoDerivedName.isNotEmpty()) autoDerivedName else "Leave blank to auto-detect") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = SmartText,
                                    unfocusedTextColor = SmartText,
                                    focusedBorderColor = SmartPrimary,
                                    unfocusedBorderColor = SmartBorder
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("git_custom_name_input")
                            )

                            Text(
                                text = "Quick Clone Samples:",
                                color = SmartTextSubtle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "FastAPI Starter" to "https://github.com/fastapi/full-stack-fastapi-template.git",
                                    "Flask API" to "https://github.com/pallets/flask.git",
                                    "React Starter" to "https://github.com/facebook/react.git",
                                    "Pyrogram Bot" to "https://github.com/pyrogram/pyrogram.git"
                                ).forEach { (label, url) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SmartSurfaceElevated)
                                            .clickable { gitRepoUrl = url }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = SmartText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        CreateProjectMode.ZIP_IMPORT -> {
                            // ZIP Import Mode
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SmartDarkBg),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderZip,
                                            contentDescription = null,
                                            tint = SmartSuccess,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Import Project Archive (.zip)",
                                            color = SmartText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Select any project ZIP file from your device. All folders, source files, and dependencies are extracted into your workspace.",
                                        color = SmartTextSubtle,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Button to pick file
                            Button(
                                onClick = {
                                    zipPickerLauncher.launch("application/zip")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedZipUri != null) SmartSurfaceElevated else SmartPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("select_zip_file_btn")
                            ) {
                                Icon(
                                    imageVector = if (selectedZipUri != null) Icons.Default.Check else Icons.Default.UploadFile,
                                    contentDescription = null,
                                    tint = if (selectedZipUri != null) SmartSuccess else Color(0xFF101118),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedZipUri != null) "Selected: $selectedZipDisplayName" else "Select ZIP File",
                                    color = if (selectedZipUri != null) SmartText else Color(0xFF101118),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedTextField(
                                value = zipCustomName,
                                onValueChange = { zipCustomName = it },
                                label = { Text("Project Name (Optional)") },
                                placeholder = { Text("Auto-derived from ZIP file name") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = SmartText,
                                    unfocusedTextColor = SmartText,
                                    focusedBorderColor = SmartPrimary,
                                    unfocusedBorderColor = SmartBorder
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("zip_custom_name_input")
                            )

                            // Quick sample project import option
                            Text(
                                text = "Sample project archive:",
                                color = SmartTextSubtle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SmartSurfaceElevated)
                                    .clickable {
                                        onCreateProject(
                                            "express_react_fullstack",
                                            "Fullstack project imported from zip",
                                            ProjectTemplate.WEB_FULLSTACK
                                        )
                                        isNewProjectDialogOpen = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Archive,
                                        contentDescription = null,
                                        tint = SmartPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Import Sample Project (fullstack-app.zip)",
                                        color = SmartText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (creationMode) {
                            CreateProjectMode.TEMPLATE -> {
                                if (newProjectName.isNotBlank()) {
                                    onCreateProject(newProjectName.trim(), newProjectDesc.trim(), selectedTemplate)
                                    isNewProjectDialogOpen = false
                                }
                            }
                            CreateProjectMode.GIT_CLONE -> {
                                if (gitRepoUrl.isNotBlank() && onCloneGitProject != null) {
                                    onCloneGitProject(gitRepoUrl.trim(), gitCustomName.trim().ifEmpty { null })
                                    isNewProjectDialogOpen = false
                                }
                            }
                            CreateProjectMode.ZIP_IMPORT -> {
                                if (selectedZipUri != null && onImportZip != null) {
                                    onImportZip(selectedZipUri!!, zipCustomName.trim().ifEmpty { null })
                                    isNewProjectDialogOpen = false
                                }
                            }
                        }
                    },
                    enabled = when (creationMode) {
                        CreateProjectMode.TEMPLATE -> newProjectName.isNotBlank()
                        CreateProjectMode.GIT_CLONE -> gitRepoUrl.isNotBlank()
                        CreateProjectMode.ZIP_IMPORT -> selectedZipUri != null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SmartPrimary),
                    modifier = Modifier.testTag("confirm_create_project_btn")
                ) {
                    Text(
                        text = when (creationMode) {
                            CreateProjectMode.TEMPLATE -> "Create"
                            CreateProjectMode.GIT_CLONE -> "Clone & Open"
                            CreateProjectMode.ZIP_IMPORT -> "Import & Open"
                        },
                        color = Color(0xFF101118),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isNewProjectDialogOpen = false }) {
                    Text("Cancel", color = SmartTextMuted)
                }
            },
            containerColor = SmartSurfaceElevated
        )
    }

    // Dialog: Rename Project
    projectToRename?.let { proj ->
        var renameText by remember(proj) { mutableStateOf(proj.name) }
        AlertDialog(
            onDismissRequest = { projectToRename = null },
            title = { Text("Rename Project", color = SmartText, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SmartText,
                        unfocusedTextColor = SmartText,
                        focusedBorderColor = SmartPrimary,
                        unfocusedBorderColor = SmartBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("rename_project_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRenameProject(proj.id, renameText.trim())
                            projectToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SmartPrimary),
                    modifier = Modifier.testTag("confirm_rename_project_btn")
                ) {
                    Text("Rename", color = Color(0xFF101118), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToRename = null }) {
                    Text("Cancel", color = SmartTextMuted)
                }
            },
            containerColor = SmartSurfaceElevated
        )
    }

    // Dialog: Delete Project
    projectToDelete?.let { proj ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project", color = SmartText, fontWeight = FontWeight.Bold) },
            text = { Text("Delete '${proj.name}'? Files inside will be permanently deleted.", color = SmartTextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(proj.id)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SmartError),
                    modifier = Modifier.testTag("confirm_delete_project_btn")
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = SmartTextMuted)
                }
            },
            containerColor = SmartSurfaceElevated
        )
    }

    // Dialog: Exported ZIP ready
    exportedZipFile?.let { zipFile ->
        AlertDialog(
            onDismissRequest = { exportedZipFile = null },
            title = { Text("Project ZIP Exported", color = SmartText, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(zipFile.name, color = SmartSuccess, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Size: ${zipFile.length() / 1024} KB", color = SmartTextMuted, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        shareZipFile(context, zipFile)
                        exportedZipFile = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SmartPrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF101118), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share / Save ZIP", color = Color(0xFF101118), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { exportedZipFile = null }) {
                    Text("Close", color = SmartTextMuted)
                }
            },
            containerColor = SmartSurfaceElevated
        )
    }
}

@Composable
fun SmartProjectItemRow(
    project: Project,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onExportZip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpen() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("project_item_${project.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Project Icon (rounded squircle with document/pen glyph)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SmartSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (project.template) {
                        ProjectTemplate.GIT_CLONE -> Icons.Default.CloudDownload
                        ProjectTemplate.ZIP_IMPORT -> Icons.Default.FolderZip
                        else -> Icons.Default.Description
                    },
                    contentDescription = null,
                    tint = when (project.template) {
                        ProjectTemplate.GIT_CLONE -> SmartPrimary
                        ProjectTemplate.ZIP_IMPORT -> SmartSuccess
                        else -> SmartTextMuted
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = project.name,
                    color = SmartText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "~/projects/${project.name}",
                    color = SmartTextSubtle,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Right side: AI Agent icon + 3-dots more menu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Google Antigravity / Aider AI wave logo
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SmartSurfaceElevated)
                    .clickable { onOpen() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Agent",
                    tint = SmartPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp).testTag("project_menu_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = SmartTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(SmartSurfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Open Project", color = SmartText) },
                        leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = SmartPrimary) },
                        onClick = {
                            menuExpanded = false
                            onOpen()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export as ZIP", color = SmartText) },
                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null, tint = SmartSuccess) },
                        onClick = {
                            menuExpanded = false
                            onExportZip()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename", color = SmartText) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = SmartTextMuted) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    Divider(color = SmartBorder, thickness = 0.5.dp)
                    DropdownMenuItem(
                        text = { Text("Delete", color = SmartError) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SmartError) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

private fun shareZipFile(context: Context, zipFile: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Project ZIP"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
