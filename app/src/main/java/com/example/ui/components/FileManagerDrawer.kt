package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodeLanguage
import com.example.model.ProjectFile
import com.example.ui.theme.SmartBorder
import com.example.ui.theme.SmartDarkBg
import com.example.ui.theme.SmartError
import com.example.ui.theme.SmartPillActive
import com.example.ui.theme.SmartPrimary
import com.example.ui.theme.SmartSurface
import com.example.ui.theme.SmartSurfaceElevated
import com.example.ui.theme.SmartText
import com.example.ui.theme.SmartTextMuted
import com.example.ui.theme.SmartTextSubtle

data class TreeItem(
    val file: ProjectFile,
    val depth: Int,
    val isFolder: Boolean,
    val isExpanded: Boolean
)

@Composable
fun FileManagerDrawer(
    projectName: String,
    files: List<ProjectFile>,
    activeFilePath: String?,
    onFileSelected: (String) -> Unit,
    onNewItemCreated: (name: String, parentPath: String, isDir: Boolean) -> Unit,
    onItemRenamed: (oldPath: String, newName: String) -> Unit,
    onItemDeleted: (String) -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var targetParentPath by remember { mutableStateOf("/") }
    var isNewItemDialogOpen by remember { mutableStateOf(false) }
    var isCreatingFolder by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }

    var fileToRename by remember { mutableStateOf<ProjectFile?>(null) }
    var fileToDelete by remember { mutableStateOf<String?>(null) }

    var isProjectRootExpanded by remember { mutableStateOf(true) }
    // Set of expanded folder paths
    var expandedFolders by remember { mutableStateOf(setOf("/", "/scripts", "/src")) }

    val foldersList = remember(files) {
        listOf("/") + files.filter { it.isDirectory }.map { it.path }
    }

    // Build the properly structured hierarchical tree items
    val treeItems = remember(files, searchQuery, expandedFolders, isProjectRootExpanded) {
        if (searchQuery.isNotBlank()) {
            files.filter { it.name.contains(searchQuery, ignoreCase = true) }
                .map { file ->
                    TreeItem(
                        file = file,
                        depth = 1,
                        isFolder = file.isDirectory,
                        isExpanded = expandedFolders.contains(file.path)
                    )
                }
        } else if (!isProjectRootExpanded) {
            emptyList()
        } else {
            buildHierarchicalTree(
                allFiles = files,
                parentPath = "/",
                depth = 1,
                expandedFolders = expandedFolders
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(SmartDarkBg)
            .padding(vertical = 8.dp)
            .testTag("file_manager_drawer")
    ) {
        // Explorer Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EXPLORER",
                    color = SmartText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                // New File at root
                IconButton(
                    onClick = {
                        targetParentPath = "/"
                        isCreatingFolder = false
                        newItemName = ""
                        isNewItemDialogOpen = true
                    },
                    modifier = Modifier.size(28.dp).testTag("new_file_root_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "New File",
                        tint = SmartPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                // New Folder at root
                IconButton(
                    onClick = {
                        targetParentPath = "/"
                        isCreatingFolder = true
                        newItemName = ""
                        isNewItemDialogOpen = true
                    },
                    modifier = Modifier.size(28.dp).testTag("new_folder_root_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "New Folder",
                        tint = SmartPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onCloseSidebar,
                    modifier = Modifier.size(28.dp).testTag("close_sidebar_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Sidebar",
                        tint = SmartTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Search/Filter in Project
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter files...", fontSize = 11.sp, color = SmartTextSubtle) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SmartTextSubtle,
                        modifier = Modifier.size(14.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = SmartTextSubtle, modifier = Modifier.size(12.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SmartPrimary,
                    unfocusedBorderColor = SmartBorder,
                    focusedTextColor = SmartText,
                    unfocusedTextColor = SmartText,
                    focusedContainerColor = SmartSurface,
                    unfocusedContainerColor = SmartSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("file_search_input")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Files Tree List matching Screenshot 2
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            // Project Root Folder Row (Matching Screenshot 2: "∨ 📁 t")
            if (searchQuery.isBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isProjectRootExpanded = !isProjectRootExpanded }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("project_root_folder"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isProjectRootExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                                contentDescription = "Toggle Root",
                                tint = SmartTextSubtle,
                                modifier = Modifier.size(18.dp)
                            )
                            Icon(
                                imageVector = if (isProjectRootExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                contentDescription = "Project Root",
                                tint = Color(0xFFF9E2AF),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = projectName,
                                color = SmartText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Add item directly to root shortcut
                        IconButton(
                            onClick = {
                                targetParentPath = "/"
                                isCreatingFolder = false
                                newItemName = ""
                                isNewItemDialogOpen = true
                            },
                            modifier = Modifier.size(24.dp).testTag("add_file_to_root_quick")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add to root",
                                tint = SmartPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Hierarchical Items (Subfolders and their direct children)
            items(treeItems, key = { it.file.path }) { item ->
                val file = item.file
                val isSelected = file.path == activeFilePath
                val itemBg = if (isSelected) SmartPrimary.copy(alpha = 0.2f) else Color.Transparent
                val indentDp = (item.depth * 14).dp

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = indentDp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(itemBg)
                        .clickable {
                            if (item.isFolder) {
                                expandedFolders = if (item.isExpanded) {
                                    expandedFolders - file.path
                                } else {
                                    expandedFolders + file.path
                                }
                            } else {
                                onFileSelected(file.path)
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("file_item_${file.name}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (item.isFolder) {
                            Icon(
                                imageVector = if (item.isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                                contentDescription = "Toggle Folder",
                                tint = SmartTextSubtle,
                                modifier = Modifier.size(16.dp)
                            )
                            Icon(
                                imageVector = if (item.isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                contentDescription = "Folder",
                                tint = Color(0xFFF9E2AF),
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            // File spacer for chevron alignment
                            Spacer(modifier = Modifier.width(16.dp))
                            FileLanguageIcon(file = file)
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = file.name,
                            color = if (isSelected) SmartPrimary else if (item.isFolder) SmartText else SmartTextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (item.isFolder || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Context action buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.isFolder) {
                            // "+ Add file inside this folder"
                            IconButton(
                                onClick = {
                                    targetParentPath = file.path
                                    isCreatingFolder = false
                                    newItemName = ""
                                    isNewItemDialogOpen = true
                                },
                                modifier = Modifier.size(22.dp).testTag("add_file_to_${file.name}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add File Inside",
                                    tint = SmartPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // Rename button
                        IconButton(
                            onClick = { fileToRename = file },
                            modifier = Modifier.size(22.dp).testTag("rename_${file.name}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename",
                                tint = SmartTextSubtle,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = { fileToDelete = file.path },
                            modifier = Modifier.size(22.dp).testTag("delete_${file.name}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = SmartTextSubtle.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog for Creating File or Subfolder (with specific Parent Folder)
    if (isNewItemDialogOpen) {
        var selectedFolder by remember { mutableStateOf(targetParentPath) }

        AlertDialog(
            onDismissRequest = { isNewItemDialogOpen = false },
            title = {
                Text(
                    text = if (isCreatingFolder) "Create Folder" else "Create File",
                    color = SmartText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Target directory: $selectedFolder",
                        color = SmartPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        placeholder = { Text(if (isCreatingFolder) "e.g. controllers" else "e.g. helper.py") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SmartText,
                            unfocusedTextColor = SmartText,
                            focusedBorderColor = SmartPrimary,
                            unfocusedBorderColor = SmartBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("new_item_name_input")
                    )

                    if (foldersList.size > 1) {
                        Text("Change destination folder:", color = SmartTextSubtle, fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            foldersList.forEach { fPath ->
                                val isChosen = fPath == selectedFolder
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isChosen) SmartPrimary else SmartSurface)
                                        .clickable { selectedFolder = fPath }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (fPath == "/") "root (/)" else fPath,
                                        color = if (isChosen) Color(0xFF11111B) else SmartTextMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
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
                        if (newItemName.isNotBlank()) {
                            // Expand the folder where the file was created so it immediately shows up
                            expandedFolders = expandedFolders + selectedFolder
                            onNewItemCreated(newItemName.trim(), selectedFolder, isCreatingFolder)
                            isNewItemDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SmartPrimary),
                    modifier = Modifier.testTag("confirm_create_item_btn")
                ) {
                    Text("Create", color = Color(0xFF11111B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isNewItemDialogOpen = false }) {
                    Text("Cancel", color = SmartTextMuted)
                }
            },
            containerColor = SmartSurfaceElevated
        )
    }

    // Dialog for Renaming File or Folder
    fileToRename?.let { target ->
        var renameInput by remember(target) { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            title = {
                Text(
                    text = "Rename ${if (target.isDirectory) "Folder" else "File"}",
                    color = SmartText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Path: ${target.path}", color = SmartTextSubtle, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SmartText,
                            unfocusedTextColor = SmartText,
                            focusedBorderColor = SmartPrimary,
                            unfocusedBorderColor = SmartBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("rename_item_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank() && renameInput.trim() != target.name) {
                            onItemRenamed(target.path, renameInput.trim())
                            fileToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SmartPrimary),
                    modifier = Modifier.testTag("confirm_rename_item_btn")
                ) {
                    Text("Rename", color = Color(0xFF11111B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) {
                    Text("Cancel", color = SmartTextMuted)
                }
            },
            containerColor = SmartSurfaceElevated
        )
    }

    // Confirmation for Delete
    fileToDelete?.let { path ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete Item", color = SmartText, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '$path'? This cannot be undone.", color = SmartTextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        onItemDeleted(path)
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SmartError),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel", color = SmartTextMuted)
                }
            },
            containerColor = SmartSurfaceElevated
        )
    }
}

/**
 * Recursively builds hierarchical tree items so that files inside a directory
 * are placed DIRECTLY below their parent directory, properly indented,
 * and never mixed up with the project root files.
 */
private fun buildHierarchicalTree(
    allFiles: List<ProjectFile>,
    parentPath: String,
    depth: Int,
    expandedFolders: Set<String>
): List<TreeItem> {
    // Direct children of parentPath:
    val directChildren = allFiles.filter { file ->
        val fileParent = file.path.substringBeforeLast("/").ifEmpty { "/" }
        fileParent == parentPath
    }

    // Sort: directories first (alphabetical), then files (alphabetical)
    val sorted = directChildren.sortedWith(
        compareByDescending<ProjectFile> { it.isDirectory }
            .thenBy { it.name.lowercase() }
    )

    val result = mutableListOf<TreeItem>()

    for (item in sorted) {
        val isExpanded = expandedFolders.contains(item.path)

        result.add(
            TreeItem(
                file = item,
                depth = depth,
                isFolder = item.isDirectory,
                isExpanded = isExpanded
            )
        )

        // If it's a directory and it is expanded, recursively append its children directly underneath it!
        if (item.isDirectory && isExpanded) {
            result.addAll(
                buildHierarchicalTree(
                    allFiles = allFiles,
                    parentPath = item.path,
                    depth = depth + 1,
                    expandedFolders = expandedFolders
                )
            )
        }
    }

    return result
}

@Composable
fun FileLanguageIcon(file: ProjectFile, modifier: Modifier = Modifier) {
    if (file.isDirectory) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = "Folder",
            tint = Color(0xFFF9E2AF),
            modifier = modifier.size(16.dp)
        )
    } else {
        val (color, icon) = when (file.language) {
            CodeLanguage.PYTHON -> Pair(Color(0xFFFFD43B), Icons.Default.Code)
            CodeLanguage.JAVASCRIPT -> Pair(Color(0xFFF7DF1E), Icons.Default.Code)
            CodeLanguage.TYPESCRIPT -> Pair(Color(0xFF3178C6), Icons.Default.Code)
            CodeLanguage.HTML -> Pair(Color(0xFFE34F26), Icons.Default.Description)
            CodeLanguage.CSS -> Pair(Color(0xFF1572B6), Icons.Default.Description)
            CodeLanguage.JSON -> Pair(Color(0xFF89DCEB), Icons.Default.InsertDriveFile)
            CodeLanguage.KOTLIN -> Pair(Color(0xFF7F52FF), Icons.Default.Code)
            CodeLanguage.RUST -> Pair(Color(0xFFDEA584), Icons.Default.Code)
            CodeLanguage.SHELL -> Pair(Color(0xFF4EAA25), Icons.Default.Terminal)
            CodeLanguage.MARKDOWN -> Pair(Color(0xFF89B4FA), Icons.Default.Description)
            CodeLanguage.DOCKER -> Pair(Color(0xFF2496ED), Icons.Default.InsertDriveFile)
            else -> Pair(SmartTextMuted, Icons.Default.InsertDriveFile)
        }
        Icon(
            imageVector = icon,
            contentDescription = file.language.displayName,
            tint = color,
            modifier = modifier.size(16.dp)
        )
    }
}
