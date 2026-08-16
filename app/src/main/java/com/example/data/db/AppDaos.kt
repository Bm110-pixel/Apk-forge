package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApkBuildRecord
import com.example.data.model.AppProject
import com.example.data.model.UiComponent
import kotlinx.coroutines.flow.Flow

@Dao
interface AppProjectDao {
    @Query("SELECT * FROM app_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<AppProject>>

    @Query("SELECT * FROM app_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): AppProject?

    @Query("SELECT * FROM app_projects WHERE id = :id LIMIT 1")
    fun observeProjectById(id: String): Flow<AppProject?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: AppProject)

    @Update
    suspend fun updateProject(project: AppProject)

    @Query("DELETE FROM app_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}

@Dao
interface UiComponentDao {
    @Query("SELECT * FROM ui_components WHERE projectId = :projectId ORDER BY orderIndex ASC")
    fun getComponentsForProject(projectId: String): Flow<List<UiComponent>>

    @Query("SELECT * FROM ui_components WHERE projectId = :projectId ORDER BY orderIndex ASC")
    suspend fun getComponentsList(projectId: String): List<UiComponent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(component: UiComponent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponents(components: List<UiComponent>)

    @Update
    suspend fun updateComponent(component: UiComponent)

    @Query("DELETE FROM ui_components WHERE id = :id")
    suspend fun deleteComponentById(id: String)

    @Query("DELETE FROM ui_components WHERE projectId = :projectId")
    suspend fun deleteAllComponentsForProject(projectId: String)
}

@Dao
interface ApkBuildDao {
    @Query("SELECT * FROM apk_builds ORDER BY timestamp DESC")
    fun getAllBuilds(): Flow<List<ApkBuildRecord>>

    @Query("SELECT * FROM apk_builds WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getBuildsForProject(projectId: String): Flow<List<ApkBuildRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(build: ApkBuildRecord)

    @Query("DELETE FROM apk_builds WHERE id = :id")
    suspend fun deleteBuildById(id: String)
}
