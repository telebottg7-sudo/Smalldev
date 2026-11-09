package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val templateId: String,
    val createdAt: Long,
    val lastModified: Long
)

@Entity(
    tableName = "workspace_files",
    primaryKeys = ["projectId", "path"]
)
data class FileEntity(
    val projectId: String,
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val content: String,
    val language: String,
    val parentPath: String,
    val lastModified: Long
)

// Global App-Root Entities: Shared across all projects
@Entity(tableName = "linux_distros")
data class DistroEntity(
    @PrimaryKey val id: String,
    val typeId: String,
    val status: String,
    val rootfsPath: String,
    val defaultUser: String,
    val architecture: String,
    val isDefault: Boolean
)

@Entity(tableName = "installed_packages")
data class InstalledPackageEntity(
    @PrimaryKey val key: String, // ecosystem:name
    val name: String,
    val ecosystem: String,
    val version: String,
    val installedAt: Long
)

@Entity(tableName = "terminal_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val timestamp: Long
)
