package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {
    // Projects
    @Query("SELECT * FROM projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: String)

    // Project Files
    @Query("SELECT * FROM workspace_files WHERE projectId = :projectId ORDER BY isDirectory DESC, path ASC")
    fun getFilesForProject(projectId: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM workspace_files WHERE projectId = :projectId")
    suspend fun getFilesListForProject(projectId: String): List<FileEntity>

    @Query("SELECT * FROM workspace_files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPath(projectId: String, path: String): FileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFile(file: FileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFiles(files: List<FileEntity>)

    @Query("DELETE FROM workspace_files WHERE projectId = :projectId AND (path = :path OR path LIKE :pathPrefix || '/%')")
    suspend fun deleteFileAndChildren(projectId: String, path: String, pathPrefix: String)

    @Query("DELETE FROM workspace_files WHERE projectId = :projectId")
    suspend fun deleteAllFilesForProject(projectId: String)

    // Global PRoot Distros & Packages (Shared App-Root)
    @Query("SELECT * FROM linux_distros")
    fun getAllDistros(): Flow<List<DistroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDistro(distro: DistroEntity)

    @Query("SELECT * FROM installed_packages")
    fun getInstalledPackages(): Flow<List<InstalledPackageEntity>>

    @Query("SELECT * FROM installed_packages")
    suspend fun getInstalledPackagesList(): List<InstalledPackageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun installPackage(pkg: InstalledPackageEntity)

    @Query("DELETE FROM installed_packages WHERE `key` = :key")
    suspend fun removePackage(key: String)

    @Query("SELECT command FROM terminal_history ORDER BY id DESC LIMIT 50")
    suspend fun getRecentCommands(): List<String>

    @Insert
    suspend fun insertCommandHistory(entity: CommandHistoryEntity)
}
