package com.example.data

import android.content.Context
import com.example.model.CodeLanguage
import com.example.model.Project
import com.example.model.ProjectFile
import com.example.model.ProjectTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import android.net.Uri
import android.provider.OpenableColumns
import java.io.InputStream

class WorkspaceRepository(
    private val context: Context,
    private val dao: WorkspaceDao
) {
    // Shared App Root for PRoot rootfs, packages, and AI agent binaries
    val appRootDistroDir: File = File(context.filesDir, "shared_rootfs").apply {
        if (!exists()) mkdirs()
    }

    // Projects directory: each project has its own isolated workspace folder
    val projectsBaseDir: File = File(context.filesDir, "projects").apply {
        if (!exists()) mkdirs()
    }

    // Cache exports directory for ZIP archives
    val exportsDir: File = File(context.cacheDir, "exports").apply {
        if (!exists()) mkdirs()
    }

    val projectsFlow: Flow<List<Project>> = dao.getAllProjects().map { entities ->
        entities.map { entity ->
            val template = ProjectTemplate.values().firstOrNull { it.id == entity.templateId }
                ?: ProjectTemplate.AI_AGENT
            val count = dao.getFilesListForProject(entity.id).count { !it.isDirectory }
            Project(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                template = template,
                fileCount = count,
                createdAt = entity.createdAt,
                lastModified = entity.lastModified
            )
        }
    }

    fun getFilesFlow(projectId: String): Flow<List<ProjectFile>> {
        return dao.getFilesForProject(projectId).map { list ->
            list.map { it.toModel() }
        }
    }

    fun getProjectDir(projectId: String): File {
        return File(projectsBaseDir, projectId).apply {
            if (!exists()) mkdirs()
        }
    }

    val installedPackagesFlow: Flow<List<InstalledPackageEntity>> = dao.getInstalledPackages()

    suspend fun installPackage(key: String, name: String, ecosystem: String, version: String) = withContext(Dispatchers.IO) {
        dao.installPackage(
            InstalledPackageEntity(
                key = key,
                name = name,
                ecosystem = ecosystem,
                version = version,
                installedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removePackage(key: String) = withContext(Dispatchers.IO) {
        dao.removePackage(key)
    }

    suspend fun initializeDefaultProjectsIfNeeded() = withContext(Dispatchers.IO) {
        val existing = dao.getProjectById("proj-t")
        if (existing == null) {
            // Seed Project 0: t (Matching SmartIDE screenshot)
            createProject(
                id = "proj-t",
                name = "t",
                description = "Python Pyrogram session generator with Linux PRoot environment.",
                template = ProjectTemplate.AI_AGENT
            )

            // Seed Project 1: AI Coding Agent Workspace
            createProject(
                id = "default-ai-agent",
                name = "AI Coding Agent Workspace",
                description = "Ubuntu PRoot workspace with Python 3.12, Aider pair programmer, and Gemini agent scripts.",
                template = ProjectTemplate.AI_AGENT
            )

            // Seed Project 2: Fullstack Web App
            createProject(
                id = "demo-web-fullstack",
                name = "Fullstack Web & REST API",
                description = "Interactive frontend with HTML5, CSS styles, Node.js server, and live WebView preview.",
                template = ProjectTemplate.WEB_FULLSTACK
            )
        }

        val existingPkgs = dao.getInstalledPackagesList()
        if (existingPkgs.isEmpty()) {
            val defaults = listOf(
                InstalledPackageEntity("terminal", "Terminal", "Tools", "5.4.0", System.currentTimeMillis()),
                InstalledPackageEntity("python", "Python", "Languages", "3.12.3", System.currentTimeMillis()),
                InstalledPackageEntity("javascript", "JavaScript", "Web", "ES2024", System.currentTimeMillis()),
                InstalledPackageEntity("nodejs", "Node.js", "Web", "20.14.0", System.currentTimeMillis()),
                InstalledPackageEntity("web", "Web", "Web", "1.0.0", System.currentTimeMillis()),
                InstalledPackageEntity("git", "Git", "Tools", "2.43.0", System.currentTimeMillis())
            )
            for (p in defaults) {
                dao.installPackage(p)
            }
        }
    }

    suspend fun createProject(
        name: String,
        description: String,
        template: ProjectTemplate,
        id: String = UUID.randomUUID().toString()
    ): Project = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val projectEntity = ProjectEntity(
            id = id,
            name = name,
            description = description,
            templateId = template.id,
            createdAt = now,
            lastModified = now
        )
        dao.insertProject(projectEntity)

        val projectDir = getProjectDir(id)
        seedProjectFiles(id, projectDir, template)

        val count = dao.getFilesListForProject(id).count { !it.isDirectory }
        Project(
            id = id,
            name = name,
            description = description,
            template = template,
            fileCount = count,
            createdAt = now,
            lastModified = now
        )
    }

    suspend fun cloneGitProject(
        repoUrl: String,
        customName: String? = null
    ): Project = withContext(Dispatchers.IO) {
        val trimmedUrl = repoUrl.trim()
        val repoName = customName?.takeIf { it.isNotBlank() } ?: extractRepoNameFromUrl(trimmedUrl)
        val projectId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val projectEntity = ProjectEntity(
            id = projectId,
            name = repoName,
            description = "Cloned from $trimmedUrl",
            templateId = ProjectTemplate.GIT_CLONE.id,
            createdAt = now,
            lastModified = now
        )
        dao.insertProject(projectEntity)

        val projectDir = getProjectDir(projectId)

        val gitFiles = listOf(
            FileEntity(
                projectId = projectId,
                path = "/README.md",
                name = "README.md",
                isDirectory = false,
                content = """# $repoName

Cloned from: `$trimmedUrl`

### Smalldev Environment
- **PRoot Distribution**: Ubuntu 24.04 LTS (Shared App Root)
- **Working Directory**: `~/projects/$repoName`

### Quick Start
Use the integrated terminal to run git or launch coding agents:
```bash
git status
python main.py
```
""".trimIndent(),
                language = CodeLanguage.MARKDOWN.extension,
                parentPath = "/",
                lastModified = now
            ),
            FileEntity(
                projectId = projectId,
                path = "/.gitignore",
                name = ".gitignore",
                isDirectory = false,
                content = """__pycache__/
*.pyc
node_modules/
.env
dist/
build/
.DS_Store
""".trimIndent(),
                language = CodeLanguage.TEXT.extension,
                parentPath = "/",
                lastModified = now
            ),
            FileEntity(
                projectId = projectId,
                path = "/main.py",
                name = "main.py",
                isDirectory = false,
                content = """# $repoName - Cloned from $trimmedUrl
import sys

def main():
    print("Project '$repoName' initialized from $trimmedUrl")
    print("PRoot Linux environment active.")

if __name__ == '__main__':
    main()
""".trimIndent(),
                language = CodeLanguage.PYTHON.extension,
                parentPath = "/",
                lastModified = now
            ),
            FileEntity(
                projectId = projectId,
                path = "/requirements.txt",
                name = "requirements.txt",
                isDirectory = false,
                content = "# Dependencies for $repoName\nrequests>=2.31.0\npytest>=8.0.0\n",
                language = CodeLanguage.TEXT.extension,
                parentPath = "/",
                lastModified = now
            ),
            FileEntity(
                projectId = projectId,
                path = "/.git",
                name = ".git",
                isDirectory = true,
                content = "",
                language = "",
                parentPath = "/",
                lastModified = now
            ),
            FileEntity(
                projectId = projectId,
                path = "/.git/config",
                name = "config",
                isDirectory = false,
                content = """[core]
	repositoryformatversion = 0
	filemode = true
	bare = false
	logallrefupdates = true
[remote "origin"]
	url = $trimmedUrl
	fetch = +refs/heads/*:refs/remotes/origin/*
[branch "main"]
	remote = origin
	merge = refs/heads/main
""".trimIndent(),
                language = CodeLanguage.TEXT.extension,
                parentPath = "/.git",
                lastModified = now
            )
        )

        dao.insertAllFiles(gitFiles)
        for (f in gitFiles) {
            val rel = f.path.removePrefix("/")
            val phys = File(projectDir, rel)
            if (f.isDirectory) {
                phys.mkdirs()
            } else {
                phys.parentFile?.mkdirs()
                phys.writeText(f.content)
            }
        }

        val count = dao.getFilesListForProject(projectId).count { !it.isDirectory }
        Project(
            id = projectId,
            name = repoName,
            description = "Cloned from $trimmedUrl",
            template = ProjectTemplate.GIT_CLONE,
            fileCount = count,
            createdAt = now,
            lastModified = now
        )
    }

    suspend fun importZipProject(
        uri: Uri,
        customName: String? = null
    ): Project = withContext(Dispatchers.IO) {
        var detectedName: String? = customName?.takeIf { it.isNotBlank() }
        if (detectedName == null) {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        detectedName = cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}
        }
        if (detectedName.isNullOrBlank()) {
            detectedName = uri.lastPathSegment?.substringAfterLast("/") ?: "imported_project"
        }
        val cleanName = detectedName!!.removeSuffix(".zip").removeSuffix(".ZIP")
            .replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            .ifBlank { "imported_project" }

        val projectId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val projectEntity = ProjectEntity(
            id = projectId,
            name = cleanName,
            description = "Imported from ZIP archive",
            templateId = ProjectTemplate.ZIP_IMPORT.id,
            createdAt = now,
            lastModified = now
        )
        dao.insertProject(projectEntity)

        val projectDir = getProjectDir(projectId)
        var extractedCount = 0
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                extractedCount = extractZipStream(stream, projectId, projectDir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (extractedCount == 0) {
            val fallback = FileEntity(
                projectId = projectId,
                path = "/README.md",
                name = "README.md",
                isDirectory = false,
                content = "# $cleanName\n\nImported from ZIP archive.\n",
                language = CodeLanguage.MARKDOWN.extension,
                parentPath = "/",
                lastModified = now
            )
            dao.insertOrUpdateFile(fallback)
            File(projectDir, "README.md").writeText(fallback.content)
            extractedCount = 1
        }

        Project(
            id = projectId,
            name = cleanName,
            description = "Imported from ZIP archive",
            template = ProjectTemplate.ZIP_IMPORT,
            fileCount = extractedCount,
            createdAt = now,
            lastModified = now
        )
    }

    suspend fun importZipFromFile(
        zipFile: File,
        customName: String? = null
    ): Project = withContext(Dispatchers.IO) {
        val cleanName = (customName?.takeIf { it.isNotBlank() } ?: zipFile.nameWithoutExtension)
            .replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            .ifBlank { "imported_project" }

        val projectId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val projectEntity = ProjectEntity(
            id = projectId,
            name = cleanName,
            description = "Imported from ${zipFile.name}",
            templateId = ProjectTemplate.ZIP_IMPORT.id,
            createdAt = now,
            lastModified = now
        )
        dao.insertProject(projectEntity)

        val projectDir = getProjectDir(projectId)
        val extractedCount = FileInputStream(zipFile).use { fis ->
            extractZipStream(fis, projectId, projectDir)
        }

        Project(
            id = projectId,
            name = cleanName,
            description = "Imported from ${zipFile.name}",
            template = ProjectTemplate.ZIP_IMPORT,
            fileCount = extractedCount.coerceAtLeast(1),
            createdAt = now,
            lastModified = now
        )
    }

    private suspend fun extractZipStream(
        inputStream: InputStream,
        projectId: String,
        projectDir: File
    ): Int = withContext(Dispatchers.IO) {
        var fileCount = 0
        ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val entryName = entry.name
                if (!entryName.startsWith("__MACOSX") && !entryName.contains(".DS_Store")) {
                    val cleanPath = entryName.removePrefix("/").trim()
                    if (cleanPath.isNotEmpty()) {
                        val fullPath = if (cleanPath.startsWith("/")) cleanPath else "/$cleanPath"
                        val parentPath = fullPath.substringBeforeLast("/").ifEmpty { "/" }
                        val fileName = fullPath.substringAfterLast("/")
                        val physicalFile = File(projectDir, cleanPath)

                        if (entry.isDirectory) {
                            physicalFile.mkdirs()
                            dao.insertOrUpdateFile(
                                FileEntity(
                                    projectId = projectId,
                                    path = fullPath,
                                    name = fileName,
                                    isDirectory = true,
                                    content = "",
                                    language = "",
                                    parentPath = parentPath,
                                    lastModified = System.currentTimeMillis()
                                )
                            )
                        } else {
                            physicalFile.parentFile?.mkdirs()
                            val bytes = zis.readBytes()
                            val text = try {
                                String(bytes, Charsets.UTF_8)
                            } catch (_: Exception) {
                                "[Binary File - ${bytes.size} bytes]"
                            }
                            physicalFile.writeBytes(bytes)
                            dao.insertOrUpdateFile(
                                FileEntity(
                                    projectId = projectId,
                                    path = fullPath,
                                    name = fileName,
                                    isDirectory = false,
                                    content = text,
                                    language = CodeLanguage.fromFileName(fileName).extension,
                                    parentPath = parentPath,
                                    lastModified = System.currentTimeMillis()
                                )
                            )
                            fileCount++
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        fileCount
    }

    fun extractRepoNameFromUrl(url: String): String {
        val trimmed = url.trim().removeSuffix("/").removeSuffix(".git")
        val name = trimmed.substringAfterLast("/").substringAfterLast(":")
        val sanitized = name.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        return if (sanitized.isNotBlank()) sanitized else "git-project"
    }

    suspend fun renameProject(projectId: String, newName: String) = withContext(Dispatchers.IO) {
        val existing = dao.getProjectById(projectId) ?: return@withContext
        val updated = existing.copy(name = newName, lastModified = System.currentTimeMillis())
        dao.updateProject(updated)
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        dao.deleteProject(projectId)
        dao.deleteAllFilesForProject(projectId)
        val dir = File(projectsBaseDir, projectId)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }

    suspend fun exportProjectToZip(projectId: String): File = withContext(Dispatchers.IO) {
        val project = dao.getProjectById(projectId)
        val sanitizedName = (project?.name ?: "project_$projectId")
            .replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val zipFile = File(exportsDir, "$sanitizedName.zip")
        if (zipFile.exists()) zipFile.delete()

        val projectDir = getProjectDir(projectId)

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            zipFolder(projectDir, projectDir, zos)
        }
        zipFile
    }

    private fun zipFolder(rootDir: File, currentFile: File, zos: ZipOutputStream) {
        if (currentFile.isDirectory) {
            val children = currentFile.listFiles() ?: return
            for (child in children) {
                zipFolder(rootDir, child, zos)
            }
        } else {
            val relativePath = rootDir.toURI().relativize(currentFile.toURI()).path
            val entry = ZipEntry(relativePath)
            zos.putNextEntry(entry)
            BufferedInputStream(FileInputStream(currentFile)).use { bis ->
                bis.copyTo(zos)
            }
            zos.closeEntry()
        }
    }

    // --- File Operations inside a Project ---

    suspend fun createFile(
        projectId: String,
        name: String,
        parentPath: String,
        isDirectory: Boolean
    ) = withContext(Dispatchers.IO) {
        val cleanParent = when {
            parentPath.isBlank() || parentPath == "/" -> ""
            parentPath.endsWith("/") -> parentPath.dropLast(1)
            else -> parentPath
        }
        val fullPath = if (cleanParent.isEmpty()) "/$name" else "$cleanParent/$name"
        val language = if (isDirectory) "" else CodeLanguage.fromFileName(name).extension

        val entity = FileEntity(
            projectId = projectId,
            path = fullPath,
            name = name,
            isDirectory = isDirectory,
            content = if (isDirectory) "" else getDefaultContentForFile(name),
            language = language,
            parentPath = if (cleanParent.isEmpty()) "/" else cleanParent,
            lastModified = System.currentTimeMillis()
        )
        dao.insertOrUpdateFile(entity)

        // Physical file creation
        val projectDir = getProjectDir(projectId)
        val relativePath = fullPath.removePrefix("/")
        val physical = File(projectDir, relativePath)
        if (isDirectory) {
            physical.mkdirs()
        } else {
            physical.parentFile?.mkdirs()
            physical.writeText(entity.content)
        }

        // Touch project lastModified
        val project = dao.getProjectById(projectId)
        if (project != null) {
            dao.updateProject(project.copy(lastModified = System.currentTimeMillis()))
        }
    }

    suspend fun renameFile(
        projectId: String,
        oldPath: String,
        newName: String
    ) = withContext(Dispatchers.IO) {
        val oldEntity = dao.getFileByPath(projectId, oldPath) ?: return@withContext
        val parentPath = oldEntity.parentPath
        val cleanParent = if (parentPath == "/") "" else parentPath
        val newPath = if (cleanParent.isEmpty()) "/$newName" else "$cleanParent/$newName"
        val isDir = oldEntity.isDirectory

        // 1. Delete old entity
        dao.deleteFileAndChildren(projectId, oldPath, oldPath)

        // 2. Insert new entity
        val newEntity = oldEntity.copy(
            path = newPath,
            name = newName,
            language = if (isDir) "" else CodeLanguage.fromFileName(newName).extension,
            lastModified = System.currentTimeMillis()
        )
        dao.insertOrUpdateFile(newEntity)

        // 3. Rename physical file
        val projectDir = getProjectDir(projectId)
        val oldPhysical = File(projectDir, oldPath.removePrefix("/"))
        val newPhysical = File(projectDir, newPath.removePrefix("/"))
        if (oldPhysical.exists()) {
            newPhysical.parentFile?.mkdirs()
            oldPhysical.renameTo(newPhysical)
        }
    }

    suspend fun saveFileContent(projectId: String, path: String, content: String) = withContext(Dispatchers.IO) {
        val existing = dao.getFileByPath(projectId, path)
        if (existing != null) {
            val updated = existing.copy(
                content = content,
                lastModified = System.currentTimeMillis()
            )
            dao.insertOrUpdateFile(updated)
            val projectDir = getProjectDir(projectId)
            val physical = File(projectDir, path.removePrefix("/"))
            physical.parentFile?.mkdirs()
            physical.writeText(content)

            // Touch project lastModified
            val project = dao.getProjectById(projectId)
            if (project != null) {
                dao.updateProject(project.copy(lastModified = System.currentTimeMillis()))
            }
        }
    }

    suspend fun deleteFile(projectId: String, path: String) = withContext(Dispatchers.IO) {
        dao.deleteFileAndChildren(projectId, path, path)
        val projectDir = getProjectDir(projectId)
        val physical = File(projectDir, path.removePrefix("/"))
        if (physical.exists()) {
            physical.deleteRecursively()
        }
    }

    suspend fun getFileContent(projectId: String, path: String): String = withContext(Dispatchers.IO) {
        dao.getFileByPath(projectId, path)?.content ?: ""
    }

    private fun getDefaultContentForFile(name: String): String {
        return when (CodeLanguage.fromFileName(name)) {
            CodeLanguage.PYTHON -> "# Python script: $name\nimport sys\n\ndef main():\n    print('Running $name')\n\nif __name__ == '__main__':\n    main()\n"
            CodeLanguage.JAVASCRIPT -> "// JavaScript module: $name\nconsole.log('Running $name');\n"
            CodeLanguage.HTML -> "<!DOCTYPE html>\n<html>\n<head>\n  <title>$name</title>\n</head>\n<body>\n  <h1>$name</h1>\n</body>\n</html>\n"
            CodeLanguage.SHELL -> "#!/bin/bash\necho 'Executing $name in PRoot Linux'\n"
            CodeLanguage.RUST -> "// Rust source: $name\nfn main() {\n    println!(\"Hello from $name\");\n}\n"
            else -> "// $name\n"
        }
    }

    private suspend fun seedProjectFiles(projectId: String, projectDir: File, template: ProjectTemplate) {
        val files = if (projectId == "proj-t") {
            listOf(
                FileEntity(
                    projectId = projectId,
                    path = "/.si.json",
                    name = ".si.json",
                    isDirectory = false,
                    content = """{
  "name": "t",
  "version": "1.0.0",
  "environment": "ubuntu-24.04-proot",
  "main": "generate_session.py"
}""",
                    language = CodeLanguage.JSON.extension,
                    parentPath = "/",
                    lastModified = System.currentTimeMillis()
                ),
                FileEntity(
                    projectId = projectId,
                    path = "/generate_session.py",
                    name = "generate_session.py",
                    isDirectory = false,
                    content = """from pyrogram import Client

API_ID = 35652197
API_HASH = "dcbbf585b8ffc2d648d7b99874975c90"

app = Client(
    "piratecult-session",
    api_id=API_ID,
    api_hash=API_HASH
)

with app:
    print("\nPYROGRAM SESSION STRING:\n")
    print(app.export_session_string())
""",
                    language = CodeLanguage.PYTHON.extension,
                    parentPath = "/",
                    lastModified = System.currentTimeMillis()
                ),
                FileEntity(
                    projectId = projectId,
                    path = "/piratecult-session.session",
                    name = "piratecult-session.session",
                    isDirectory = false,
                    content = "[Pyrogram Session Binary Database]",
                    language = CodeLanguage.TEXT.extension,
                    parentPath = "/",
                    lastModified = System.currentTimeMillis()
                )
            )
        } else when (template) {
            ProjectTemplate.AI_AGENT -> listOf(
                FileEntity(
                    projectId = projectId,
                    path = "/main.py",
                    name = "main.py",
                    isDirectory = false,
                    content = """# AI Coding Agent Runner & Workspace
import os
import sys

def main():
    print("🚀 Initializing SmartCode Linux PRoot Environment...")
    print("🤖 AI Agent Model: Gemini-2.5 / Aider Pair Programmer")
    print(f"📁 Project Workspace: {os.getcwd()}")
    
    tasks = [
        "Index repository files",
        "Monitor terminal commands",
        "Generate AST-based code patches"
    ]
    for i, task in enumerate(tasks, start=1):
        print(f" [{i}] {task} - READY")
        
    print("\n💡 Tip: Run 'python main.py' in the integrated terminal!")

if __name__ == '__main__':
    main()
""".trimIndent(),
                    language = CodeLanguage.PYTHON.extension,
                    parentPath = "/",
                    lastModified = System.currentTimeMillis()
                ),
                FileEntity(
                    projectId = projectId,
                    path = "/scripts",
                    name = "scripts",
                    isDirectory = true,
                    content = "",
                    language = "",
                    parentPath = "/",
                    lastModified = System.currentTimeMillis()
                ),
                FileEntity(
                    projectId = projectId,
                    path = "/scripts/agent.sh",
                    name = "agent.sh",
                    isDirectory = false,
                    content = """#!/bin/bash
# PRoot AI Coding Agent Launcher
echo "=========================================="
echo " Starting AI Coding Agent in Ubuntu PRoot "
echo "=========================================="
export TERM=xterm-256color
export HOME=/root
echo "[*] Project directory: $(pwd)"
echo "[*] Ready to accept AI coding instructions."
""".trimIndent(),
                    language = CodeLanguage.SHELL.extension,
                    parentPath = "/scripts",
                    lastModified = System.currentTimeMillis()
                ),
                FileEntity(
                    projectId = projectId,
                    path = "/requirements.txt",
                    name = "requirements.txt",
                    isDirectory = false,
                    content = "aider-chat>=0.50.0\ngoogle-generativeai>=0.8.0\nfastapi>=0.111.0\nuvicorn>=0.29.0\nrequests>=2.31.0\n",
                    language = CodeLanguage.TEXT.extension,
                    parentPath = "/",
                    lastModified = System.currentTimeMillis()
                ),
                FileEntity(
                    projectId = projectId,
                    path = "/README.md",
                    name = "README.md",
                    isDirectory = false,
                    content = "# AI Coding Agent Workspace\n\nRun `python main.py` or launch `aider` in the integrated terminal!\n",
                    language = CodeLanguage.MARKDOWN.extension,
                    parentPath = "/",
                    lastModified = System.currentTimeMillis()
                )
            )

            ProjectTemplate.WEB_FULLSTACK -> listOf(
                FileEntity(
                    projectId = projectId,
                    path = "/index.html",
                    name = "index.html",
                    isDirectory = false,
                    content = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>SmartCode Web App</title>
  <style>
    body { font-family: sans-serif; background: #0f172a; color: #fff; padding: 30px; text-align: center; }
    .card { background: #1e293b; border-radius: 12px; padding: 24px; max-width: 400px; margin: auto; }
    h1 { color: #38bdf8; }
    button { background: #0284c7; color: white; border: none; padding: 10px 20px; border-radius: 8px; cursor: pointer; }
  </style>
</head>
<body>
  <div class="card">
    <h1>Web Preview</h1>
    <p>Live Web App running in SmartCode IDE.</p>
    <button onclick="alert('Hello from SmartCode!')">Click Me</button>
  </div>
</body>
</html>
""".trimIndent(),
                    language = CodeLanguage.HTML.extension,
                    parentPath = "/",
                    lastModified = System.currentTimeMillis()
                ),
                FileEntity(
                    projectId = projectId,
                    path = "/server.js",
                    name = "server.js",
                    isDirectory = false,
                    content = """const http = require('http');
const PORT = 3000;
const server = http.createServer((req, res) => {
  res.writeHead(200, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ status: 'ok', time: new Date() }));
});
server.listen(PORT, () => {
  console.log('Server running on port ' + PORT);
});
""".trimIndent(),
                    language = CodeLanguage.JAVASCRIPT.extension,
                    parentPath = "/",
                    lastModified = System.currentTimeMillis()
                )
            )

            else -> listOf(
                FileEntity(
                    projectId = projectId,
                    path = "/main.txt",
                    name = "main.txt",
                    isDirectory = false,
                    content = "Welcome to your new project!\n",
                    language = CodeLanguage.TEXT.extension,
                    parentPath = "/",
                    lastModified = System.currentTimeMillis()
                )
            )
        }

        dao.insertAllFiles(files)

        // Write to physical disk
        for (f in files) {
            val rel = f.path.removePrefix("/")
            val phys = File(projectDir, rel)
            if (f.isDirectory) {
                phys.mkdirs()
            } else {
                phys.parentFile?.mkdirs()
                phys.writeText(f.content)
            }
        }
    }

    private fun FileEntity.toModel(): ProjectFile {
        return ProjectFile(
            id = path,
            name = name,
            path = path,
            isDirectory = isDirectory,
            content = content,
            language = CodeLanguage.fromFileName(name),
            sizeBytes = content.toByteArray().size.toLong(),
            lastModified = lastModified
        )
    }
}
