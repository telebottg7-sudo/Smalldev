package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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

@Composable
fun ProjectsHomeView(
    projects: List<Project>,
    activeProjectId: String?,
    onOpenProject: (Project) -> Unit,
    onCreateProject: (name: String, desc: String, template: ProjectTemplate) -> Unit,
    onRenameProject: (projectId: String, newName: String) -> Unit,
    onDeleteProject: (projectId: String) -> Unit,
    onExportZip: (projectId: String, onComplete: (File) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isNewProjectDialogOpen by remember { mutableStateOf(false) }
    var projectToRename by remember { mutableStateOf<Project?>(null) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }
    var exportedZipFile by remember { mutableStateOf<File?>(null) }

    var newProjectName by remember { mutableStateOf("") }
    var newProjectDesc by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(ProjectTemplate.AI_AGENT) }

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
                selectedTemplate = ProjectTemplate.AI_AGENT
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

    // Dialog: Create New Project
    if (isNewProjectDialogOpen) {
        AlertDialog(
            onDismissRequest = { isNewProjectDialogOpen = false },
            title = { Text("New Project", color = SmartText, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        ProjectTemplate.values().forEach { tpl ->
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjectName.isNotBlank()) {
                            onCreateProject(newProjectName.trim(), newProjectDesc.trim(), selectedTemplate)
                            isNewProjectDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SmartPrimary),
                    modifier = Modifier.testTag("confirm_create_project_btn")
                ) {
                    Text("Create", color = Color(0xFF101118), fontWeight = FontWeight.Bold)
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
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = SmartTextMuted,
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
