package com.example.data.repository

import android.content.Context
import com.example.data.ai.AiAppGenerator
import com.example.data.apk.ApkBuildProgress
import com.example.data.apk.ApkBuilderEngine
import com.example.data.db.AppDatabase
import com.example.data.model.ApkBuildRecord
import com.example.data.model.AppProject
import com.example.data.model.UiComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class AppProjectRepository(private val database: AppDatabase) {

    private val projectDao = database.projectDao()
    private val componentDao = database.componentDao()
    private val buildDao = database.apkBuildDao()

    val allProjects: Flow<List<AppProject>> = projectDao.getAllProjects()
    val allBuilds: Flow<List<ApkBuildRecord>> = buildDao.getAllBuilds()

    fun observeProject(id: String): Flow<AppProject?> = projectDao.observeProjectById(id)

    fun observeComponents(projectId: String): Flow<List<UiComponent>> =
        componentDao.getComponentsForProject(projectId)

    suspend fun getProject(id: String): AppProject? = projectDao.getProjectById(id)

    suspend fun getComponentsList(projectId: String): List<UiComponent> =
        componentDao.getComponentsList(projectId)

    suspend fun createProjectWithAi(
        prompt: String,
        config: com.example.data.model.AiConfiguration = com.example.data.model.AiConfiguration()
    ): AppProject = withContext(Dispatchers.IO) {
        val result = AiAppGenerator.generateApp(prompt, config)
        val projToInsert = result.project.copy(isSynced = false)
        projectDao.insertProject(projToInsert)
        componentDao.insertComponents(result.components)
        return@withContext projToInsert
    }

    suspend fun createProjectFromTemplate(templateId: String): AppProject = withContext(Dispatchers.IO) {
        val result = when (templateId) {
            "template_calculator" -> AiAppGenerator.createCalculatorTemplate()
            "template_todo" -> AiAppGenerator.createTodoListTemplate()
            "template_blog" -> AiAppGenerator.createBlogTemplate()
            else -> AiAppGenerator.synthesizeApp(templateId)
        }
        val projToInsert = result.project.copy(isSynced = false)
        projectDao.insertProject(projToInsert)
        componentDao.insertComponents(result.components)
        return@withContext projToInsert
    }

    suspend fun saveProject(project: AppProject) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
    }

    suspend fun toggleStar(projectId: String) = withContext(Dispatchers.IO) {
        val proj = projectDao.getProjectById(projectId) ?: return@withContext
        val newStarred = !proj.isStarred
        val newCount = if (newStarred) proj.starCount + 1 else (proj.starCount - 1).coerceAtLeast(0)
        projectDao.updateProject(proj.copy(isStarred = newStarred, starCount = newCount, isSynced = false))
    }

    suspend fun incrementViewCount(projectId: String) = withContext(Dispatchers.IO) {
        val proj = projectDao.getProjectById(projectId) ?: return@withContext
        projectDao.updateProject(proj.copy(viewCount = proj.viewCount + 1))
    }

    suspend fun incrementDownloadCount(projectId: String) = withContext(Dispatchers.IO) {
        val proj = projectDao.getProjectById(projectId) ?: return@withContext
        projectDao.updateProject(proj.copy(downloadCount = proj.downloadCount + 1))
    }

    suspend fun addComponent(component: UiComponent) = withContext(Dispatchers.IO) {
        componentDao.insertComponent(component)
        updateProjectTimestamp(component.projectId)
    }

    suspend fun updateComponent(component: UiComponent) = withContext(Dispatchers.IO) {
        componentDao.updateComponent(component)
        updateProjectTimestamp(component.projectId)
    }

    suspend fun deleteComponent(componentId: String, projectId: String) = withContext(Dispatchers.IO) {
        componentDao.deleteComponentById(componentId)
        updateProjectTimestamp(projectId)
    }

    suspend fun reorderComponents(projectId: String, newOrderedList: List<UiComponent>) = withContext(Dispatchers.IO) {
        val updated = newOrderedList.mapIndexed { index, comp ->
            comp.copy(orderIndex = index)
        }
        componentDao.insertComponents(updated)
        updateProjectTimestamp(projectId)
    }

    suspend fun replaceAllComponents(projectId: String, newComponents: List<UiComponent>) = withContext(Dispatchers.IO) {
        componentDao.deleteAllComponentsForProject(projectId)
        val updated = newComponents.mapIndexed { index, comp ->
            comp.copy(orderIndex = index)
        }
        componentDao.insertComponents(updated)
        updateProjectTimestamp(projectId)
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        componentDao.deleteAllComponentsForProject(projectId)
        projectDao.deleteProjectById(projectId)
    }

    suspend fun deleteBuild(buildId: String) = withContext(Dispatchers.IO) {
        buildDao.deleteBuildById(buildId)
    }

    private suspend fun updateProjectTimestamp(projectId: String) {
        val proj = projectDao.getProjectById(projectId)
        if (proj != null) {
            projectDao.updateProject(proj.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
        }
    }

    suspend fun syncOfflineChanges(cloudSyncEngine: com.example.data.cloud.FirebaseCloudSyncEngine): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val unsynced = projectDao.getUnsyncedProjects()
            var count = 0
            for (proj in unsynced) {
                val comps = componentDao.getComponentsList(proj.id)
                val res = cloudSyncEngine.uploadProjectToCloud(proj, comps)
                if (res.isSuccess) {
                    projectDao.updateProjectSyncStatus(proj.id, true)
                    count++
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buildApk(
        context: Context,
        projectId: String,
        onProgress: (ApkBuildProgress) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val project = projectDao.getProjectById(projectId)
                ?: return@withContext Result.failure(IllegalStateException("Project not found"))
            val components = componentDao.getComponentsList(projectId)

            val startTime = System.currentTimeMillis()
            val apkFile = ApkBuilderEngine.buildApk(context, project, components, onProgress)
            val duration = System.currentTimeMillis() - startTime

            val buildRecord = ApkBuildRecord(
                id = UUID.randomUUID().toString(),
                projectId = project.id,
                projectName = project.name,
                packageName = project.packageName,
                versionName = project.versionName,
                versionCode = project.versionCode,
                timestamp = System.currentTimeMillis(),
                status = "SUCCESS",
                apkFileName = apkFile.name,
                apkFilePath = apkFile.absolutePath,
                fileSizeBytes = apkFile.length(),
                buildDurationMs = duration,
                buildLogs = "Build completed in ${duration}ms"
            )
            buildDao.insertBuild(buildRecord)

            projectDao.updateProject(
                project.copy(
                    latestApkPath = apkFile.absolutePath,
                    latestApkSize = apkFile.length(),
                    buildCount = project.buildCount + 1,
                    updatedAt = System.currentTimeMillis()
                )
            )

            Result.success(apkFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = projectDao.getAllProjects().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val calcSeed = AiAppGenerator.createCalculatorTemplate()
            projectDao.insertProject(calcSeed.project)
            componentDao.insertComponents(calcSeed.components)

            val todoSeed = AiAppGenerator.createTodoListTemplate()
            projectDao.insertProject(todoSeed.project)
            componentDao.insertComponents(todoSeed.components)

            val blogSeed = AiAppGenerator.createBlogTemplate()
            projectDao.insertProject(blogSeed.project)
            componentDao.insertComponents(blogSeed.components)

            val fitSeed = AiAppGenerator.synthesizeApp("Titan Fit Tracker with workout rep counter and rest timers")
            projectDao.insertProject(fitSeed.project)
            componentDao.insertComponents(fitSeed.components)
        }
    }

    suspend fun createOrLoadTutorialSandbox(tutorialManager: com.example.data.tutorial.TutorialManager): AppProject = withContext(Dispatchers.IO) {
        val (tutorialProj, tutorialComps) = tutorialManager.createTutorialSandboxProject()
        val existing = projectDao.getProjectById(tutorialProj.id)
        if (existing == null) {
            projectDao.insertProject(tutorialProj)
            componentDao.insertComponents(tutorialComps)
            return@withContext tutorialProj
        } else {
            return@withContext existing
        }
    }

    suspend fun importProjectFromCloud(
        cloudRecord: com.example.data.model.CloudProjectRecord,
        cloudSyncEngine: com.example.data.cloud.FirebaseCloudSyncEngine
    ): AppProject = withContext(Dispatchers.IO) {
        val (project, components) = cloudSyncEngine.deserializeCloudProject(cloudRecord)
        projectDao.insertProject(project)
        componentDao.deleteAllComponentsForProject(project.id)
        componentDao.insertComponents(components)
        return@withContext project
    }
}
