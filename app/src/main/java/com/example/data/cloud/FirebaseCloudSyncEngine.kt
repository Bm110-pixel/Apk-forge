package com.example.data.cloud

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.example.data.model.AppProject
import com.example.data.model.CloudProjectRecord
import com.example.data.model.CloudSyncState
import com.example.data.model.CloudSyncStatus
import com.example.data.model.ComponentAction
import com.example.data.model.ComponentType
import com.example.data.model.UiComponent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class FirebaseCloudSyncEngine private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("firebase_cloud_sync_prefs", Context.MODE_PRIVATE)

    private val _syncState = MutableStateFlow(loadInitialState())
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    private val deviceId: String
    private var deviceName: String

    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    init {
        // Initialize device identifier
        var storedDeviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (storedDeviceId == null) {
            storedDeviceId = "dev_${UUID.randomUUID().toString().take(8)}"
            prefs.edit().putString(KEY_DEVICE_ID, storedDeviceId).apply()
        }
        deviceId = storedDeviceId

        val defaultName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        deviceName = prefs.getString(KEY_DEVICE_NAME, defaultName) ?: defaultName

        // Initialize Firebase safely
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                auth = FirebaseAuth.getInstance()
                Log.d("CloudSync", "Firebase Firestore & Auth initialized successfully")
            } else {
                Log.w("CloudSync", "FirebaseApp not initialized, will use Cloud Vault storage cache")
            }
        } catch (e: Exception) {
            Log.e("CloudSync", "Firebase initialization error: ${e.message}")
        }

        _syncState.value = _syncState.value.copy(
            currentDeviceId = deviceId,
            currentDeviceName = deviceName,
            isFirebaseReady = firestore != null
        )
    }

    private fun loadInitialState(): CloudSyncState {
        val autoSync = prefs.getBoolean(KEY_AUTO_SYNC, true)
        val isGuest = prefs.getBoolean(KEY_IS_GUEST_MODE, false)
        val isSignedIn = prefs.getBoolean(KEY_IS_SIGNED_IN, false) || isGuest
        val userEmail = prefs.getString(KEY_USER_EMAIL, if (isGuest) "guest@aistudio.local" else null)
        val userName = prefs.getString(KEY_USER_NAME, if (isGuest) "Guest User" else null)
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L).takeIf { it > 0 }

        val cachedProjects = loadCachedCloudProjects()

        return CloudSyncState(
            isSyncing = false,
            lastSyncTime = lastSync,
            syncError = null,
            currentDeviceName = "Mobile Device",
            currentDeviceId = "",
            autoSyncEnabled = autoSync,
            cloudProjects = cachedProjects,
            userEmail = userEmail,
            userDisplayName = userName,
            isSignedIn = isSignedIn,
            isGuestMode = isGuest,
            isFirebaseReady = true
        )
    }

    fun setGuestMode(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_IS_GUEST_MODE, enabled)
            .putBoolean(KEY_IS_SIGNED_IN, true)
            .putString(KEY_USER_EMAIL, "guest@aistudio.local")
            .putString(KEY_USER_NAME, "Guest User")
            .apply()

        _syncState.value = _syncState.value.copy(
            isGuestMode = enabled,
            isSignedIn = true,
            userEmail = "guest@aistudio.local",
            userDisplayName = "Guest User"
        )
    }

    fun updateDeviceName(newName: String) {
        if (newName.isBlank()) return
        deviceName = newName.trim()
        prefs.edit().putString(KEY_DEVICE_NAME, deviceName).apply()
        _syncState.value = _syncState.value.copy(currentDeviceName = deviceName)
    }

    fun setAutoSync(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
        _syncState.value = _syncState.value.copy(autoSyncEnabled = enabled)
    }

    fun setUserAccount(email: String, displayName: String) {
        prefs.edit()
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, displayName)
            .putBoolean(KEY_IS_SIGNED_IN, true)
            .apply()

        _syncState.value = _syncState.value.copy(
            userEmail = email,
            userDisplayName = displayName,
            isSignedIn = true
        )
    }

    fun signOut() {
        auth?.signOut()
        prefs.edit()
            .putBoolean(KEY_IS_SIGNED_IN, false)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .apply()

        _syncState.value = _syncState.value.copy(
            isSignedIn = false,
            userEmail = null,
            userDisplayName = null
        )
    }

    // ==========================================
    // Cloud Sync Operations (Upload & Download)
    // ==========================================

    suspend fun uploadProjectToCloud(
        project: AppProject,
        components: List<UiComponent>
    ): Result<CloudProjectRecord> = withContext(Dispatchers.IO) {
        _syncState.value = _syncState.value.copy(isSyncing = true, syncError = null)
        try {
            val componentsJson = serializeComponentsToJson(components)
            val record = CloudProjectRecord(
                id = project.id,
                name = project.name,
                packageName = project.packageName,
                description = project.description,
                category = project.category,
                versionName = project.versionName,
                versionCode = project.versionCode,
                primaryColorHex = project.primaryColorHex,
                secondaryColorHex = project.secondaryColorHex,
                backgroundColorHex = project.backgroundColorHex,
                surfaceColorHex = project.surfaceColorHex,
                textColorHex = project.textColorHex,
                isDarkTheme = project.isDarkTheme,
                iconName = project.iconName,
                promptUsed = project.promptUsed,
                createdAt = project.createdAt,
                updatedAt = project.updatedAt,
                lastSyncedAt = System.currentTimeMillis(),
                lastSyncDeviceId = deviceId,
                lastSyncDeviceName = deviceName,
                authorEmail = _syncState.value.userEmail ?: "developer@aistudio.com",
                authorName = _syncState.value.userDisplayName ?: "Mobile Developer",
                componentCount = components.size,
                componentsJson = componentsJson,
                syncStatus = CloudSyncStatus.SYNCED
            )

            // 1. Upload to Firebase Firestore if available
            val db = firestore
            if (db != null) {
                try {
                    val map = cloudRecordToMap(record)
                    db.collection(COLLECTION_PROJECTS)
                        .document(record.id)
                        .set(map, SetOptions.merge())
                        .await()
                    Log.d("CloudSync", "Saved project ${record.id} to Firestore")
                } catch (e: Exception) {
                    Log.w("CloudSync", "Firestore sync error, saving to local cloud vault: ${e.message}")
                }
            }

            // 2. Cache in local cloud cache
            saveProjectToCache(record)

            val updatedList = loadCachedCloudProjects()
            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_LAST_SYNC, now).apply()

            _syncState.value = _syncState.value.copy(
                isSyncing = false,
                lastSyncTime = now,
                cloudProjects = updatedList,
                syncError = null
            )

            Result.success(record)
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(
                isSyncing = false,
                syncError = e.message ?: "Failed to upload project to cloud"
            )
            Result.failure(e)
        }
    }

    suspend fun fetchCloudProjects(): Result<List<CloudProjectRecord>> = withContext(Dispatchers.IO) {
        _syncState.value = _syncState.value.copy(isSyncing = true, syncError = null)
        try {
            val db = firestore
            val remoteProjects = mutableListOf<CloudProjectRecord>()

            if (db != null) {
                try {
                    val snapshot = db.collection(COLLECTION_PROJECTS).get().await()
                    for (doc in snapshot.documents) {
                        mapToCloudRecord(doc.data ?: emptyMap(), doc.id)?.let {
                            remoteProjects.add(it)
                        }
                    }
                    Log.d("CloudSync", "Fetched ${remoteProjects.size} projects from Firestore")
                } catch (e: Exception) {
                    Log.w("CloudSync", "Failed to fetch from Firestore, reading cache: ${e.message}")
                }
            }

            // Merge with local cache
            val localCache = loadCachedCloudProjects()
            val mergedMap = LinkedHashMap<String, CloudProjectRecord>()

            // Put local cache first
            for (p in localCache) {
                mergedMap[p.id] = p
            }
            // Overwrite or add remote
            for (p in remoteProjects) {
                val existing = mergedMap[p.id]
                if (existing == null || p.lastSyncedAt >= existing.lastSyncedAt) {
                    mergedMap[p.id] = p
                }
            }

            val finalProjects = mergedMap.values.sortedByDescending { it.lastSyncedAt }
            saveAllProjectsToCache(finalProjects)

            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_LAST_SYNC, now).apply()

            _syncState.value = _syncState.value.copy(
                isSyncing = false,
                lastSyncTime = now,
                cloudProjects = finalProjects,
                syncError = null
            )

            Result.success(finalProjects)
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(
                isSyncing = false,
                syncError = e.message ?: "Failed to fetch cloud projects"
            )
            Result.failure(e)
        }
    }

    fun deserializeCloudProject(record: CloudProjectRecord): Pair<AppProject, List<UiComponent>> {
        val project = AppProject(
            id = record.id,
            name = record.name,
            packageName = record.packageName,
            description = record.description,
            category = record.category,
            versionName = record.versionName,
            versionCode = record.versionCode,
            primaryColorHex = record.primaryColorHex,
            secondaryColorHex = record.secondaryColorHex,
            backgroundColorHex = record.backgroundColorHex,
            surfaceColorHex = record.surfaceColorHex,
            textColorHex = record.textColorHex,
            isDarkTheme = record.isDarkTheme,
            iconName = record.iconName,
            promptUsed = record.promptUsed,
            createdAt = record.createdAt,
            updatedAt = record.updatedAt
        )

        val components = deserializeComponentsFromJson(record.componentsJson, record.id)
        return Pair(project, components)
    }

    suspend fun deleteCloudProject(projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = firestore
            if (db != null) {
                try {
                    db.collection(COLLECTION_PROJECTS).document(projectId).delete().await()
                } catch (e: Exception) {
                    Log.w("CloudSync", "Could not delete from remote: ${e.message}")
                }
            }

            val current = loadCachedCloudProjects().filter { it.id != projectId }
            saveAllProjectsToCache(current)
            _syncState.value = _syncState.value.copy(cloudProjects = current)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // JSON Serialization Helpers
    // ==========================================

    fun serializeComponentsToJson(components: List<UiComponent>): String {
        val jsonArray = JSONArray()
        for (comp in components) {
            val obj = JSONObject().apply {
                put("id", comp.id)
                put("projectId", comp.projectId)
                put("type", comp.type.name)
                put("title", comp.title)
                put("subtitle", comp.subtitle)
                put("stateValue", comp.stateValue)
                put("placeholder", comp.placeholder)
                put("actionType", comp.actionType.name)
                put("actionPayload", comp.actionPayload)
                put("orderIndex", comp.orderIndex)
                put("colorHex", comp.colorHex ?: "")
                put("fontSizeSp", comp.fontSizeSp)
                put("cornerRadiusDp", comp.cornerRadiusDp)
                put("iconName", comp.iconName)
                put("isEnabled", comp.isEnabled)
                put("customDataJson", comp.customDataJson)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    fun deserializeComponentsFromJson(jsonStr: String, targetProjectId: String): List<UiComponent> {
        val list = mutableListOf<UiComponent>()
        if (jsonStr.isBlank()) return list
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val typeName = obj.optString("type", ComponentType.TEXT.name)
                val type = try { ComponentType.valueOf(typeName) } catch (_: Exception) { ComponentType.TEXT }

                val actionTypeName = obj.optString("actionType", ComponentAction.NONE.name)
                val actionType = try { ComponentAction.valueOf(actionTypeName) } catch (_: Exception) { ComponentAction.NONE }

                val comp = UiComponent(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    projectId = targetProjectId,
                    type = type,
                    title = obj.optString("title", "Component"),
                    subtitle = obj.optString("subtitle", ""),
                    stateValue = obj.optString("stateValue", ""),
                    placeholder = obj.optString("placeholder", ""),
                    actionType = actionType,
                    actionPayload = obj.optString("actionPayload", ""),
                    orderIndex = obj.optInt("orderIndex", i),
                    colorHex = obj.optString("colorHex", "").takeIf { it.isNotBlank() },
                    fontSizeSp = obj.optInt("fontSizeSp", 16),
                    cornerRadiusDp = obj.optInt("cornerRadiusDp", 12),
                    iconName = obj.optString("iconName", ""),
                    isEnabled = obj.optBoolean("isEnabled", true),
                    customDataJson = obj.optString("customDataJson", "{}")
                )
                list.add(comp)
            }
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to parse components JSON: ${e.message}")
        }
        return list
    }

    // ==========================================
    // Firestore Map Converters
    // ==========================================

    private fun cloudRecordToMap(record: CloudProjectRecord): Map<String, Any> {
        return mapOf(
            "id" to record.id,
            "name" to record.name,
            "packageName" to record.packageName,
            "description" to record.description,
            "category" to record.category,
            "versionName" to record.versionName,
            "versionCode" to record.versionCode,
            "primaryColorHex" to record.primaryColorHex,
            "secondaryColorHex" to record.secondaryColorHex,
            "backgroundColorHex" to record.backgroundColorHex,
            "surfaceColorHex" to record.surfaceColorHex,
            "textColorHex" to record.textColorHex,
            "isDarkTheme" to record.isDarkTheme,
            "iconName" to record.iconName,
            "promptUsed" to record.promptUsed,
            "createdAt" to record.createdAt,
            "updatedAt" to record.updatedAt,
            "lastSyncedAt" to record.lastSyncedAt,
            "lastSyncDeviceId" to record.lastSyncDeviceId,
            "lastSyncDeviceName" to record.lastSyncDeviceName,
            "authorEmail" to record.authorEmail,
            "authorName" to record.authorName,
            "componentCount" to record.componentCount,
            "componentsJson" to record.componentsJson
        )
    }

    private fun mapToCloudRecord(map: Map<String, Any>, docId: String): CloudProjectRecord? {
        return try {
            CloudProjectRecord(
                id = map["id"] as? String ?: docId,
                name = map["name"] as? String ?: "Untitled App",
                packageName = map["packageName"] as? String ?: "com.example.app",
                description = map["description"] as? String ?: "",
                category = map["category"] as? String ?: "Utility",
                versionName = map["versionName"] as? String ?: "1.0.0",
                versionCode = (map["versionCode"] as? Number)?.toInt() ?: 1,
                primaryColorHex = map["primaryColorHex"] as? String ?: "#6366F1",
                secondaryColorHex = map["secondaryColorHex"] as? String ?: "#06B6D4",
                backgroundColorHex = map["backgroundColorHex"] as? String ?: "#0F172A",
                surfaceColorHex = map["surfaceColorHex"] as? String ?: "#1E293B",
                textColorHex = map["textColorHex"] as? String ?: "#F8FAFC",
                isDarkTheme = map["isDarkTheme"] as? Boolean ?: true,
                iconName = map["iconName"] as? String ?: "ic_default_app",
                promptUsed = map["promptUsed"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                lastSyncedAt = (map["lastSyncedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                lastSyncDeviceId = map["lastSyncDeviceId"] as? String ?: "",
                lastSyncDeviceName = map["lastSyncDeviceName"] as? String ?: "Cloud Device",
                authorEmail = map["authorEmail"] as? String ?: "developer@aistudio.com",
                authorName = map["authorName"] as? String ?: "Developer",
                componentCount = (map["componentCount"] as? Number)?.toInt() ?: 0,
                componentsJson = map["componentsJson"] as? String ?: "[]",
                syncStatus = CloudSyncStatus.SYNCED
            )
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to deserialize cloud project doc: ${e.message}")
            null
        }
    }

    // ==========================================
    // Local Cache Persistence
    // ==========================================

    private fun saveProjectToCache(record: CloudProjectRecord) {
        val current = loadCachedCloudProjects().toMutableList()
        val index = current.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            current[index] = record
        } else {
            current.add(0, record)
        }
        saveAllProjectsToCache(current)
    }

    private fun saveAllProjectsToCache(projects: List<CloudProjectRecord>) {
        val jsonArray = JSONArray()
        for (p in projects) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("packageName", p.packageName)
                put("description", p.description)
                put("category", p.category)
                put("versionName", p.versionName)
                put("versionCode", p.versionCode)
                put("primaryColorHex", p.primaryColorHex)
                put("secondaryColorHex", p.secondaryColorHex)
                put("backgroundColorHex", p.backgroundColorHex)
                put("surfaceColorHex", p.surfaceColorHex)
                put("textColorHex", p.textColorHex)
                put("isDarkTheme", p.isDarkTheme)
                put("iconName", p.iconName)
                put("promptUsed", p.promptUsed)
                put("createdAt", p.createdAt)
                put("updatedAt", p.updatedAt)
                put("lastSyncedAt", p.lastSyncedAt)
                put("lastSyncDeviceId", p.lastSyncDeviceId)
                put("lastSyncDeviceName", p.lastSyncDeviceName)
                put("authorEmail", p.authorEmail)
                put("authorName", p.authorName)
                put("componentCount", p.componentCount)
                put("componentsJson", p.componentsJson)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_CLOUD_PROJECTS_CACHE, jsonArray.toString()).apply()
    }

    private fun loadCachedCloudProjects(): List<CloudProjectRecord> {
        val jsonStr = prefs.getString(KEY_CLOUD_PROJECTS_CACHE, null) ?: return getDefaultSeedProjects()
        val list = mutableListOf<CloudProjectRecord>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val record = CloudProjectRecord(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    name = obj.optString("name", "Synced Project"),
                    packageName = obj.optString("packageName", "com.example.cloud"),
                    description = obj.optString("description", ""),
                    category = obj.optString("category", "Utility"),
                    versionName = obj.optString("versionName", "1.0.0"),
                    versionCode = obj.optInt("versionCode", 1),
                    primaryColorHex = obj.optString("primaryColorHex", "#6366F1"),
                    secondaryColorHex = obj.optString("secondaryColorHex", "#06B6D4"),
                    backgroundColorHex = obj.optString("backgroundColorHex", "#0F172A"),
                    surfaceColorHex = obj.optString("surfaceColorHex", "#1E293B"),
                    textColorHex = obj.optString("textColorHex", "#F8FAFC"),
                    isDarkTheme = obj.optBoolean("isDarkTheme", true),
                    iconName = obj.optString("iconName", "ic_default_app"),
                    promptUsed = obj.optString("promptUsed", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                    lastSyncedAt = obj.optLong("lastSyncedAt", System.currentTimeMillis()),
                    lastSyncDeviceId = obj.optString("lastSyncDeviceId", "dev_remote"),
                    lastSyncDeviceName = obj.optString("lastSyncDeviceName", "Pixel 8 Pro"),
                    authorEmail = obj.optString("authorEmail", "developer@aistudio.com"),
                    authorName = obj.optString("authorName", "Android Dev"),
                    componentCount = obj.optInt("componentCount", 4),
                    componentsJson = obj.optString("componentsJson", "[]"),
                    syncStatus = CloudSyncStatus.SYNCED
                )
                list.add(record)
            }
        } catch (e: Exception) {
            Log.e("CloudSync", "Failed to load cached cloud projects: ${e.message}")
        }
        return if (list.isEmpty()) getDefaultSeedProjects() else list
    }

    private fun getDefaultSeedProjects(): List<CloudProjectRecord> {
        return listOf(
            CloudProjectRecord(
                id = "cloud_nexus_dashboard",
                name = "Nexus Crypt Tracker",
                packageName = "com.nexus.crypt",
                description = "Real-time cryptocurrency portfolio analyzer with live market tickers and profit calculators",
                category = "Finance",
                versionName = "2.1.0",
                primaryColorHex = "#10B981",
                secondaryColorHex = "#6366F1",
                lastSyncDeviceName = "Pixel Tablet",
                lastSyncedAt = System.currentTimeMillis() - 3600000L * 4,
                authorEmail = "marriottbeauden@gmail.com",
                authorName = "Beauden Marriott",
                componentCount = 6,
                componentsJson = """[
                    {"id":"c1","type":"HEADER","title":"Nexus Crypto Terminal","subtitle":"Live BTC & ETH Watchlist","orderIndex":0},
                    {"id":"c2","type":"METRIC_STAT","title":"Portfolio Value","stateValue":"$42,890.50 USD","orderIndex":1},
                    {"id":"c3","type":"BUTTON","title":"⚡ Quick Buy Bitcoin","actionType":"SHOW_TOAST","actionPayload":"Order placed for 0.05 BTC","orderIndex":2},
                    {"id":"c4","type":"SWITCH","title":"Real-time Push Alerts","stateValue":"true","actionType":"TOGGLE_STATE","orderIndex":3}
                ]""".trimIndent()
            ),
            CloudProjectRecord(
                id = "cloud_solaris_fitness",
                name = "Solaris Workout Log",
                packageName = "com.solaris.fitness",
                description = "High-intensity interval timer and weightlifting tracker with voice cues",
                category = "Health & Fitness",
                versionName = "1.4.0",
                primaryColorHex = "#F59E0B",
                secondaryColorHex = "#EF4444",
                lastSyncDeviceName = "Galaxy S24 Ultra",
                lastSyncedAt = System.currentTimeMillis() - 3600000L * 18,
                authorEmail = "marriottbeauden@gmail.com",
                authorName = "Beauden Marriott",
                componentCount = 5,
                componentsJson = """[
                    {"id":"s1","type":"HEADER","title":"Solaris Workout Studio","subtitle":"Leg Day & Core Split","orderIndex":0},
                    {"id":"s2","type":"COUNTER_WIDGET","title":"Squats Set Count","stateValue":"4","orderIndex":1},
                    {"id":"s3","type":"PROGRESS_BAR","title":"Rest Timer Remaining","stateValue":"45","orderIndex":2},
                    {"id":"s4","type":"BUTTON","title":"✓ Finish Workout & Log Reps","actionType":"SHOW_TOAST","actionPayload":"Workout saved to Cloud Vault!","orderIndex":3}
                ]""".trimIndent()
            )
        )
    }

    companion object {
        private const val COLLECTION_PROJECTS = "user_projects"
        private const val KEY_DEVICE_ID = "key_device_id"
        private const val KEY_DEVICE_NAME = "key_device_name"
        private const val KEY_AUTO_SYNC = "key_auto_sync"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_IS_SIGNED_IN = "key_is_signed_in"
        private const val KEY_IS_GUEST_MODE = "key_is_guest_mode"
        private const val KEY_LAST_SYNC = "key_last_sync"
        private const val KEY_CLOUD_PROJECTS_CACHE = "key_cloud_projects_cache"

        @Volatile
        private var instance: FirebaseCloudSyncEngine? = null

        fun getInstance(context: Context): FirebaseCloudSyncEngine {
            return instance ?: synchronized(this) {
                instance ?: FirebaseCloudSyncEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
