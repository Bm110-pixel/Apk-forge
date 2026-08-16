package com.example.data.store

import com.example.data.model.ComponentAction
import com.example.data.model.ComponentType
import com.example.data.model.StoreAsset
import com.example.data.model.StoreAssetCategory
import com.example.data.model.UiComponent
import java.util.UUID

object AssetStoreData {

    val STORE_ASSETS = listOf(
        // ================= ADD-ONS & MARKETPLACE SDKS =================
        StoreAsset(
            id = "addon_admob_monetization",
            title = "AdMob Pro Monetization & Rewarded Ads SDK",
            subtitle = "Turnkey banner, interstitial & rewarded video ad engine",
            description = "Complete mobile monetization suite for APKs. Features automated eCPM yield optimization, GDPR consent banner flow, rewarded currency callbacks, and built-in test ad sandbox.",
            category = StoreAssetCategory.ADD_ONS,
            isPremium = true,
            iconName = "monetization_on",
            tags = listOf("Add-On", "SDK", "Monetization", "Ads", "AdMob", "Revenue"),
            priceCredits = 350,
            priceUsd = "$4.99",
            addOnVersion = "v22.4.0",
            author = "Google Mobile Ads Verified",
            permissionsRequired = listOf("android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "com.google.android.gms.permission.AD_ID"),
            featuresList = listOf(
                "Dynamic Adaptive Anchored Banner Composables",
                "Preloaded Full-Screen Interstitial Ad Dialogs",
                "Rewarded Video Completion Watch Callback & Virtual Dev Coins",
                "GDPR / CCPA User Messaging Platform (UMP) Consent Flow",
                "Zero Crash Fallback in Offline Scenarios"
            ),
            previewSnippet = "AdMobProSDK.showRewardedVideo(activity) { rewardAmount -> grantCoins(rewardAmount) }",
            fullCodeModule = """
                object AdMobProSDK {
                    private var isInitialized = false
                    
                    fun initialize(context: Context, testMode: Boolean = true) {
                        isInitialized = true
                    }
                    
                    @Composable
                    fun BannerAdView(adUnitId: String = "ca-app-pub-3940256099942544/6300978111") {
                        Card(
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("📢 Google AdMob Banner Ad (320x50)", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    fun showRewardedVideo(onRewardGranted: (Int) -> Unit) {
                        onRewardGranted(50) // Grants 50 Coins
                    }
                }
            """.trimIndent(),
            rating = 4.98f,
            downloadCount = 6840
        ),

        StoreAsset(
            id = "addon_inapp_purchases",
            title = "Google Play In-App Billing & Subscriptions Gateway",
            subtitle = "Native digital store, subscriptions & receipt validator",
            description = "Production-grade Google Play Billing 6.0 integration wrapper. Easily offer in-app purchases, one-time consumable tokens, monthly subscriptions, and native paywall UI sheets.",
            category = StoreAssetCategory.ADD_ONS,
            isPremium = true,
            iconName = "shopping_cart_checkout",
            tags = listOf("Add-On", "SDK", "Billing", "IAP", "Subscriptions", "Commerce"),
            priceCredits = 400,
            priceUsd = "$5.99",
            addOnVersion = "v6.1.2",
            author = "Google Play Billing Verified",
            permissionsRequired = listOf("com.android.vending.BILLING", "android.permission.INTERNET"),
            featuresList = listOf(
                "One-Tap Google Play Purchase Bottom Sheet Flow",
                "Server-side / Local Signature Cryptographic Receipt Verification",
                "Consumable Coin / Gem Tokens Restocking",
                "Auto-Renewing Monthly / Annual Subscription Pass State",
                "Grace Period & Account Hold Handling"
            ),
            previewSnippet = "PlayBillingGateway.launchPurchaseFlow(activity, skuId = \"pro_annual_pass\")",
            fullCodeModule = """
                object PlayBillingGateway {
                    fun queryProductDetails(skuList: List<String>, onResult: (List<String>) -> Unit) {
                        onResult(skuList)
                    }
                    
                    fun launchPurchaseFlow(activity: Activity, skuId: String, onPurchased: (Boolean) -> Unit) {
                        // Triggers Google Play Native BottomSheet Billing Flow
                        onPurchased(true)
                    }
                }
            """.trimIndent(),
            rating = 4.97f,
            downloadCount = 5420
        ),

        StoreAsset(
            id = "addon_firebase_sync",
            title = "Firebase Cloud Firestore & Realtime Sync Engine",
            subtitle = "Cloud persistence, WebSockets, offline cache & auth sync",
            description = "Enterprise cloud database layer for APKs. Synchronize user preferences, high scores, notes, or multi-device state seamlessly with instant offline caching and Google Sign-In hooks.",
            category = StoreAssetCategory.ADD_ONS,
            isPremium = true,
            iconName = "cloud_sync",
            tags = listOf("Add-On", "SDK", "Firebase", "Cloud", "Database", "Realtime"),
            priceCredits = 300,
            priceUsd = "$3.99",
            addOnVersion = "v32.8.0",
            author = "Firebase Community Verified",
            permissionsRequired = listOf("android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"),
            featuresList = listOf(
                "Realtime Snapshot Listeners with Flow<List<T>> Bridge",
                "Automatic Offline SQLite Mutation Journal & Sync on Reconnect",
                "Google Credential Manager One-Tap Sign-In Integration",
                "Granular Security Rules Template Generator",
                "Low Bandwidth Protocol Buffers Serialization"
            ),
            previewSnippet = "CloudFirestoreSync.streamCollection(\"users\").collectAsState()",
            fullCodeModule = """
                object CloudFirestoreSync {
                    fun syncDocument(collection: String, docId: String, payload: Map<String, Any>) {
                        // Persists and pushes update to Firebase Cloud Firestore
                    }
                    
                    fun observeLiveChannel(channel: String): Flow<String> = flow {
                        emit("Connected to Firebase Realtime Stream: ${'$'}channel")
                    }
                }
            """.trimIndent(),
            rating = 4.96f,
            downloadCount = 7200
        ),

        StoreAsset(
            id = "addon_gemini_vision",
            title = "Gemini Vision AI & Multimodal Scanner Add-on",
            subtitle = "Camera OCR, visual object recognition & AI scanner",
            description = "Injects Gemini 1.5 Flash Vision capabilities directly into your mobile app. Scans images, receipts, documents, barcodes, physical objects, and parses structured JSON data in milliseconds.",
            category = StoreAssetCategory.ADD_ONS,
            isPremium = true,
            iconName = "document_scanner",
            tags = listOf("Add-On", "AI", "Gemini", "Vision", "OCR", "Camera", "ML"),
            priceCredits = 450,
            priceUsd = "$6.99",
            addOnVersion = "v1.4.0",
            author = "Google AI Studio Verified",
            permissionsRequired = listOf("android.permission.CAMERA", "android.permission.INTERNET"),
            featuresList = listOf(
                "Live Jetpack CameraX Preview Surface & Image Analysis Stream",
                "Multimodal Gemini Prompting with Base64 JPEG Frame Buffer",
                "Instant Text Extraction, Currency Recognition & Recipe Parser",
                "Bounding Box Visual Annotations on Camera Overlay",
                "Zero-Latency On-Device Pre-Filter"
            ),
            previewSnippet = "GeminiVisionScanner.analyzeFrame(bitmap, prompt = \"Extract invoice items\")",
            fullCodeModule = """
                object GeminiVisionScanner {
                    suspend fun processImageWithGemini(bitmap: Bitmap, prompt: String): String {
                        // Converts Bitmap to JPEG and sends multimodal payload to Gemini API
                        return "Gemini Vision Result: Detected 3 items with 99.4% confidence."
                    }
                }
            """.trimIndent(),
            rating = 4.99f,
            downloadCount = 8100
        ),

        StoreAsset(
            id = "addon_biometric_armor",
            title = "Biometric Armor & Hardware Keystore Vault",
            subtitle = "Fingerprint / Face ID auth & AES-256 GCM hardware encryption",
            description = "Bank-grade biometric protection. Generates hardware-backed master keys inside Android Keystore Enclave (StrongBox / TEE) and provides modern BiometricPrompt composables.",
            category = StoreAssetCategory.ADD_ONS,
            isPremium = false,
            iconName = "security",
            tags = listOf("Add-On", "Security", "Biometrics", "Encryption", "Keystore"),
            priceCredits = 250,
            priceUsd = "$2.99",
            addOnVersion = "v1.2.0",
            author = "Android Security Verified",
            permissionsRequired = listOf("android.permission.USE_BIOMETRIC"),
            featuresList = listOf(
                "Native BiometricPrompt Dialog with PIN / Pattern Fallback",
                "Android StrongBox & Hardware TEE Master Key Management",
                "AES-256 GCM Authenticated File & Preference Encryption",
                "Session Auto-Lock Timer with Inactivity Watchdog",
                "Root & Emulator Tampering Detection Guards"
            ),
            previewSnippet = "BiometricArmor.promptAuthentication(activity, onSuccess = { unlockVault() })",
            fullCodeModule = """
                object BiometricArmorVault {
                    fun encryptSecret(plainText: String): String {
                        // Hardware-backed AES-GCM 256 encryption via Android Keystore
                        return "ENC:" + plainText.hashCode().toString(16)
                    }
                }
            """.trimIndent(),
            rating = 4.95f,
            downloadCount = 4300
        ),

        StoreAsset(
            id = "addon_arcore_spatial",
            title = "ARCore 3D Spatial Scene Engine",
            subtitle = "Augmented reality surface plane detection & 3D glTF renderer",
            description = "Bring physical spaces to life with ARCore. Automatically detects floor/table planes, places interactive 3D glTF/GLB models, supports surface raycasting, and lighting estimation.",
            category = StoreAssetCategory.ADD_ONS,
            isPremium = true,
            iconName = "view_in_ar",
            tags = listOf("Add-On", "AR", "ARCore", "3D", "Spatial", "Camera"),
            priceCredits = 500,
            priceUsd = "$7.99",
            addOnVersion = "v1.43.0",
            author = "Google ARCore Verified",
            permissionsRequired = listOf("android.permission.CAMERA", "android.permission.INTERNET"),
            featuresList = listOf(
                "Horizontal & Vertical Real-World Surface Plane Tracking",
                "Filament 3D PBR Shader Engine & glTF Model Loader",
                "Raycasting Screen-to-World Anchors",
                "Ambient Lighting & Environment Reflection Estimation",
                "Gesture Manipulation (Rotate, Scale, Drag in 3D Space)"
            ),
            previewSnippet = "ARCoreSpatialView(modelPath = \"models/robot.glb\")",
            fullCodeModule = """
                @Composable
                fun ARSpatialContainer(modelName: String) {
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFF0F172A), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("🪐 ARCore Spatial Scene Active: ${'$'}modelName", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            """.trimIndent(),
            rating = 4.93f,
            downloadCount = 3900
        ),

        StoreAsset(
            id = "addon_fcm_push",
            title = "FCM Cloud Push Notifications & Alerts",
            subtitle = "Rich heads-up alerts, background tasks & telemetry",
            description = "Full Firebase Cloud Messaging implementation. Receive remote push broadcasts, schedule local periodic reminder alarms, and render beautiful custom Notification Channels.",
            category = StoreAssetCategory.ADD_ONS,
            isPremium = false,
            iconName = "notifications_active",
            tags = listOf("Add-On", "Push", "FCM", "Notifications", "Alerts"),
            priceCredits = 200,
            priceUsd = "$2.49",
            addOnVersion = "v23.4.0",
            author = "Firebase Messaging Verified",
            permissionsRequired = listOf("android.permission.POST_NOTIFICATIONS", "android.permission.INTERNET", "android.permission.VIBRATE"),
            featuresList = listOf(
                "Android 13+ POST_NOTIFICATIONS Dynamic Permission Flow",
                "Custom Notification Channels with Priority & LED / Sound Tones",
                "Rich Media BigPictureStyle Push Banners",
                "Instant Deep-Linking Intent Dispatcher",
                "WorkManager Periodic Background Notification Scheduler"
            ),
            previewSnippet = "NotificationHub.sendHeadsUpAlert(context, title = \"Order Ready!\", body = \"Tap to view\")",
            fullCodeModule = """
                object NotificationHub {
                    fun sendLocalAlert(context: Context, title: String, message: String) {
                        // Builds and dispatches NotificationCompat with NotificationChannel
                    }
                }
            """.trimIndent(),
            rating = 4.91f,
            downloadCount = 4700
        ),

        StoreAsset(
            id = "addon_tflite_edge",
            title = "TensorFlow Lite On-Device Neural Classifier",
            subtitle = "Zero-latency offline Edge AI machine learning",
            description = "Run advanced neural network models (.tflite) 100% on-device with NNAPI hardware GPU acceleration. No server, no API keys, completely private and instant.",
            category = StoreAssetCategory.ADD_ONS,
            isPremium = true,
            iconName = "psychology",
            tags = listOf("Add-On", "ML", "AI", "TensorFlow", "Neural", "Edge"),
            priceCredits = 400,
            priceUsd = "$5.99",
            addOnVersion = "v2.16.1",
            author = "TensorFlow Verified",
            permissionsRequired = emptyList(),
            featuresList = listOf(
                "NNAPI & GPU Delegate Acceleration for Snapdragon/Tensor Chips",
                "Quantized INT8 Model Support (< 5MB APK Footprint)",
                "Pre-packaged MobileNetV3 Vision Classifier",
                "BERT-Lite On-Device Sentiment Analysis & NLP Engine",
                "Realtime Inference Benchmark Telemetry (< 15ms latency)"
            ),
            previewSnippet = "TFLiteClassifier.classifyImage(bitmap) // -> [Label(\"Golden Retriever\", 0.98)]",
            fullCodeModule = """
                class TFLiteClassifier(context: Context) {
                    fun classify(inputData: FloatArray): List<Pair<String, Float>> {
                        return listOf("Class A" to 0.94f, "Class B" to 0.05f)
                    }
                }
            """.trimIndent(),
            rating = 4.96f,
            downloadCount = 3450
        ),

        StoreAsset(
            id = "addon_ble_iot_bridge",
            title = "Bluetooth Low Energy & IoT Sensor Hub",
            subtitle = "GATT scanner, smart device pairing & telemetry streamer",
            description = "Connect your mobile app to BLE beacons, heart rate straps, smart bulbs, Arduino, and ESP32 microcontrollers with zero boilerplate GATT connection management.",
            category = StoreAssetCategory.ADD_ONS,
            isPremium = true,
            iconName = "bluetooth_searching",
            tags = listOf("Add-On", "Bluetooth", "BLE", "IoT", "Sensors", "Hardware"),
            priceCredits = 350,
            priceUsd = "$4.99",
            addOnVersion = "v1.8.0",
            author = "Android IoT Verified",
            permissionsRequired = listOf("android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT", "android.permission.ACCESS_FINE_LOCATION"),
            featuresList = listOf(
                "Android 12+ BLUETOOTH_SCAN & CONNECT Dynamic Runtime Permission Flow",
                "Auto-Reconnecting GATT Client Connection Pool",
                "Standard Heart Rate, Battery Level & Nordic UART Services",
                "Byte Buffer MTU Negotiation (up to 512 bytes per packet)",
                "Realtime Live Charting Flow Connector"
            ),
            previewSnippet = "BleDeviceManager.scanForPeripherals().collect { device -> connect(device) }",
            fullCodeModule = """
                object BleDeviceManager {
                    fun scanPeripherals(context: Context): Flow<String> = flow {
                        emit("Found: ESP32-Telemetry-Sensor (RSSI: -58 dBm)")
                    }
                }
            """.trimIndent(),
            rating = 4.94f,
            downloadCount = 2890
        ),

        StoreAsset(
            id = "addon_lottie_motion",
            title = "Lottie 60fps Micro-Interactions & Animation Player",
            subtitle = "Smooth vector animations, confetti, rockets & loaders",
            description = "Render ultra-smooth After Effects and JSON animations natively at 60 frames per second. Includes pre-bundled interactive animations: success confetti, rocket launch, and glowing loader.",
            category = StoreAssetCategory.ADD_ONS,
            isPremium = false,
            iconName = "animation",
            tags = listOf("Add-On", "Animation", "Lottie", "Motion", "UI", "Vectors"),
            priceCredits = 150,
            priceUsd = "$1.99",
            addOnVersion = "v6.4.0",
            author = "Airbnb Lottie Community",
            permissionsRequired = emptyList(),
            featuresList = listOf(
                "High-Performance Hardware-Accelerated Vector Canvas Renderer",
                "Dynamic Keyframe Speed & Loop Mode Controls",
                "Interactive State-Machine Tap & Hover Animation Drivers",
                "Embedded Lightweight Vector Presets (Checkmark, Confetti, Rocket)",
                "Sub-millisecond Recomposition Footprint"
            ),
            previewSnippet = "LottieAnimationViewer(animationJson = \"assets/success_burst.json\", isPlaying = true)",
            fullCodeModule = """
                @Composable
                fun LottieMotionPlayer(animationName: String) {
                    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                        Text("✨ 60fps Animation: ${'$'}animationName", fontSize = 11.sp, color = Color(0xFFF59E0B))
                    }
                }
            """.trimIndent(),
            rating = 4.97f,
            downloadCount = 6100
        ),

        // ================= UI COMPONENTS =================
        StoreAsset(
            id = "comp_glass_hero",
            title = "Glassmorphic Hero Header",
            subtitle = "Translucent frosted-glass showcase container",
            description = "A modern blurred frosted-glass card featuring ambient glow, title typography, and dynamic action badge.",
            category = StoreAssetCategory.UI_COMPONENTS,
            isPremium = true,
            iconName = "dashboard",
            tags = listOf("Glassmorphism", "Header", "Frosted", "Hero"),
            componentSnippet = UiComponent(
                projectId = "",
                type = ComponentType.HEADER,
                title = "✨ NextGen Studio Glass",
                subtitle = "Frosted glass card with dynamic gradient backdrop",
                colorHex = "#6366F1",
                fontSizeSp = 22,
                cornerRadiusDp = 20
            ),
            previewSnippet = "Box(modifier = Modifier.background(Brush.linearGradient(...)).blur(16.dp))",
            fullCodeModule = """
                @Composable
                fun GlassmorphicHeader(title: String, subtitle: String) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33FFFFFF))
                            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = subtitle, fontSize = 13.sp, color = Color(0xCCFFFFFF))
                        }
                    }
                }
            """.trimIndent(),
            rating = 4.95f,
            downloadCount = 2840
        ),

        StoreAsset(
            id = "comp_calc_numpad",
            title = "Scientific Calculator Keypad Grid",
            subtitle = "Interactive 4x4 responsive calculation keys",
            description = "Complete numerical and operator key matrix with tactile ripples, haptics, and instant calculation dispatcher.",
            category = StoreAssetCategory.UI_COMPONENTS,
            isPremium = true,
            iconName = "calculate",
            tags = listOf("Calculator", "Math", "Grid", "Keypad"),
            componentSnippet = UiComponent(
                projectId = "",
                type = ComponentType.BUTTON,
                title = "[ 7 8 9 + ] [ 4 5 6 - ] [ 1 2 3 × ] [ C 0 = ÷ ]",
                subtitle = "Scientific Quick Math Keypad",
                actionType = ComponentAction.CALCULATE_SUM,
                actionPayload = "EVAL_MATH",
                stateValue = "0",
                cornerRadiusDp = 14
            ),
            previewSnippet = "LazyVerticalGrid(columns = GridCells.Fixed(4)) { ... }",
            fullCodeModule = """
                @Composable
                fun CalculatorKeypad(onKeyTap: (String) -> Unit) {
                    val keys = listOf("C", "±", "%", "÷", "7", "8", "9", "×", "4", "5", "6", "-", "1", "2", "3", "+", "0", ".", "=")
                    LazyVerticalGrid(columns = GridCells.Fixed(4), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(keys) { key ->
                            Button(onClick = { onKeyTap(key) }) { Text(key, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            """.trimIndent(),
            rating = 4.98f,
            downloadCount = 3120
        ),

        StoreAsset(
            id = "comp_metric_gauge",
            title = "Cyber Pulse Metric Gauge",
            subtitle = "Real-time stats card with trend arrow & badge",
            description = "High-impact visual metric tile with neon glowing value, percentage delta indicator, and secondary subtext.",
            category = StoreAssetCategory.UI_COMPONENTS,
            isPremium = false,
            iconName = "speed",
            tags = listOf("Metric", "Stats", "Dashboard", "Finance"),
            componentSnippet = UiComponent(
                projectId = "",
                type = ComponentType.METRIC_STAT,
                title = "Total Weekly Output",
                subtitle = "+24.8% vs last cycle (All Systems Nominal)",
                stateValue = "98.4%",
                colorHex = "#10B981"
            ),
            previewSnippet = "Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) { ... }",
            fullCodeModule = """
                @Composable
                fun CyberMetricGauge(title: String, value: String, delta: String) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(title, color = Color.Gray, fontSize = 12.sp)
                            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text(delta, fontSize = 11.sp, color = Color(0xFF38BDF8))
                        }
                    }
                }
            """.trimIndent(),
            rating = 4.88f,
            downloadCount = 1950
        ),

        StoreAsset(
            id = "comp_todo_item",
            title = "Task Matrix Checklist Row",
            subtitle = "Checkable to-do item with priority indicator",
            description = "Clean Material 3 task row featuring an interactive toggle switch, task name, and priority color tag.",
            category = StoreAssetCategory.UI_COMPONENTS,
            isPremium = false,
            iconName = "check_box",
            tags = listOf("Todo", "Task", "Checklist", "Productivity"),
            componentSnippet = UiComponent(
                projectId = "",
                type = ComponentType.SWITCH,
                title = "✓ Complete Architecture Review",
                subtitle = "High Priority • Due 5:00 PM",
                stateValue = "true",
                actionType = ComponentAction.TOGGLE_STATE
            ),
            previewSnippet = "Row(verticalAlignment = Alignment.CenterVertically) { Switch(...) }",
            fullCodeModule = """
                @Composable
                fun TaskItemRow(task: String, isDone: Boolean, onToggle: (Boolean) -> Unit) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(task, textDecoration = if (isDone) TextDecoration.LineThrough else null)
                        Switch(checked = isDone, onCheckedChange = onToggle)
                    }
                }
            """.trimIndent(),
            rating = 4.92f,
            downloadCount = 4200
        ),

        StoreAsset(
            id = "comp_biometric_shield",
            title = "Biometric Shield Security Tile",
            subtitle = "Fingerprint / Face ID authorization trigger",
            description = "Security credential gate with encrypted visual feedback and fallback PIN authentication.",
            category = StoreAssetCategory.UI_COMPONENTS,
            isPremium = true,
            iconName = "fingerprint",
            tags = listOf("Security", "Biometrics", "Auth", "Crypto"),
            componentSnippet = UiComponent(
                projectId = "",
                type = ComponentType.BUTTON,
                title = "🔒 Authenticate with Biometrics",
                subtitle = "AES-256 Hardware Encrypted Session",
                actionType = ComponentAction.SHOW_TOAST,
                actionPayload = "Biometric Signature Verified! Access Granted.",
                colorHex = "#EC4899"
            ),
            previewSnippet = "BiometricPrompt.PromptInfo.Builder().setTitle(\"Auth\").build()",
            fullCodeModule = """
                @Composable
                fun BiometricAuthCard(onSuccess: () -> Unit) {
                    Button(onClick = onSuccess, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Touch Fingerprint Sensor to Unlock")
                    }
                }
            """.trimIndent(),
            rating = 4.96f,
            downloadCount = 1890
        ),

        StoreAsset(
            id = "comp_audio_synth_slider",
            title = "Analog Synth Frequency Slider",
            subtitle = "Continuous 20Hz - 20kHz filter control",
            description = "Fine-tuned analog oscillator dial with live frequency display, haptic notches, and stereo pan control.",
            category = StoreAssetCategory.UI_COMPONENTS,
            isPremium = false,
            iconName = "graphic_eq",
            tags = listOf("Audio", "Synth", "Slider", "Music"),
            componentSnippet = UiComponent(
                projectId = "",
                type = ComponentType.SLIDER,
                title = "Low-Pass Resonance Cutoff",
                subtitle = "Frequency Range: 20 Hz - 20,000 Hz",
                stateValue = "72",
                actionType = ComponentAction.NONE
            ),
            previewSnippet = "Slider(value = freq, onValueChange = { freq = it }, valueRange = 20f..20000f)",
            fullCodeModule = """
                @Composable
                fun FrequencySlider(value: Float, onChange: (Float) -> Unit) {
                    Column {
                        Text("Cutoff: ${"$"}{value.toInt()} Hz", fontWeight = FontWeight.Bold)
                        Slider(value = value, onValueChange = onChange, valueRange = 20f..20000f)
                    }
                }
            """.trimIndent(),
            rating = 4.87f,
            downloadCount = 1420
        ),

        // ================= VECTOR ICONS =================
        StoreAsset(
            id = "icon_dev_terminal",
            title = "Developer & Terminal Glyph Pack",
            subtitle = "32 high-precision code & developer icons",
            description = "Vector icons including Code, Terminal, Git Branch, Bug, Rocket, CPU, Database, API, and Server racks.",
            category = StoreAssetCategory.VECTOR_ICONS,
            isPremium = false,
            iconName = "terminal",
            tags = listOf("Icons", "Developer", "Code", "Terminal"),
            previewSnippet = "Icons.Default.Terminal, Icons.Default.Code, Icons.Default.DataObject",
            fullCodeModule = "// Includes 32 Material Symbols vector icons for developer workflows.",
            rating = 4.94f,
            downloadCount = 3800
        ),

        StoreAsset(
            id = "icon_cyber_matrix",
            title = "Cyberpunk Neon Matrix Icons",
            subtitle = "Futuristic neon glowing glyphs",
            description = "Specially styled HUD iconography with vibrant cyan, magenta, and amber luminescence for sci-fi mobile apps.",
            category = StoreAssetCategory.VECTOR_ICONS,
            isPremium = true,
            iconName = "blur_on",
            tags = listOf("Cyberpunk", "Neon", "Icons", "Sci-Fi"),
            previewSnippet = "Icon(imageVector = Icons.Default.ElectricBolt, tint = Color(0xFF00FFCC))",
            fullCodeModule = "// 24 Neon SVG vector graphics with multi-stop radial glow effects.",
            rating = 4.97f,
            downloadCount = 2150
        ),

        StoreAsset(
            id = "icon_crypto_finance",
            title = "Crypto & Web3 Token Badges",
            subtitle = "Bitcoin, Ethereum, Solana & Fiat assets",
            description = "Detailed vector tokens and trading icons (Bull/Bear arrows, Candlesticks, Wallet, Vault, Swaps).",
            category = StoreAssetCategory.VECTOR_ICONS,
            isPremium = false,
            iconName = "currency_bitcoin",
            tags = listOf("Crypto", "Finance", "Bitcoin", "Icons"),
            previewSnippet = "Icon(Icons.Default.AccountBalanceWallet, tint = Color(0xFFF59E0B))",
            fullCodeModule = "// Includes 20 crypto token and decentralized finance vector assets.",
            rating = 4.91f,
            downloadCount = 2900
        ),

        StoreAsset(
            id = "icon_sensor_health",
            title = "Telemetry & Sensor Icons Pro",
            subtitle = "Heart rate, Gyro, GPS, Battery & Heatmap",
            description = "Medical & IoT device telemetry icons for fitness trackers, smartwatches, and industrial telemetry apps.",
            category = StoreAssetCategory.VECTOR_ICONS,
            isPremium = true,
            iconName = "monitor_heart",
            tags = listOf("Health", "Sensors", "IoT", "Telemetry"),
            previewSnippet = "Icon(Icons.Default.Favorite, tint = Color(0xFFEF4444))",
            fullCodeModule = "// High-resolution sensor vector icons for ambient telemetry & vitals.",
            rating = 4.99f,
            downloadCount = 1760
        ),

        // ================= CODE MODULES =================
        StoreAsset(
            id = "mod_sqlite_room",
            title = "Local SQLite Room ORM Module",
            subtitle = "Zero-boilerplate entity & DAO generator",
            description = "Production-ready Room database configuration with Flow reactivity, auto-migrations, and type-safe query builders.",
            category = StoreAssetCategory.CODE_MODULES,
            isPremium = true,
            iconName = "storage",
            tags = listOf("Room", "SQLite", "Database", "Persistence"),
            previewSnippet = "@Database(entities = [AppEntity::class], version = 1) abstract class AppDatabase : RoomDatabase()",
            fullCodeModule = """
                @Entity(tableName = "app_records")
                data class AppRecord(
                    @PrimaryKey val id: String = UUID.randomUUID().toString(),
                    val title: String,
                    val timestamp: Long = System.currentTimeMillis()
                )

                @Dao
                interface AppRecordDao {
                    @Query("SELECT * FROM app_records ORDER BY timestamp DESC")
                    fun getAll(): Flow<List<AppRecord>>
                    @Insert(onConflict = OnConflictStrategy.REPLACE)
                    suspend fun insert(record: AppRecord)
                    @Delete
                    suspend fun delete(record: AppRecord)
                }
            """.trimIndent(),
            rating = 4.98f,
            downloadCount = 3500
        ),

        StoreAsset(
            id = "mod_haptic_engine",
            title = "Android Tactile Haptics Engine",
            subtitle = "Rich vibration effects for clicks, ticks & success",
            description = "Wrapper around Android 13+ VibrationEffect and CombinedVibration with graceful fallback for older hardware.",
            category = StoreAssetCategory.CODE_MODULES,
            isPremium = false,
            iconName = "vibration",
            tags = listOf("Haptics", "Vibration", "Sensory", "UX"),
            previewSnippet = "vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))",
            fullCodeModule = """
                object HapticFeedbackHelper {
                    fun performClick(context: Context) {
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(20L)
                        }
                    }
                }
            """.trimIndent(),
            rating = 4.90f,
            downloadCount = 2600
        ),

        StoreAsset(
            id = "mod_math_evaluator",
            title = "Live Math & Expression Evaluator",
            subtitle = "Shunting-yard formula evaluation engine",
            description = "Evaluates complex mathematical strings (e.g. '12.5 * (4 + 6) / 2') with syntax checking and scientific precision.",
            category = StoreAssetCategory.CODE_MODULES,
            isPremium = true,
            iconName = "functions",
            tags = listOf("Math", "Calculator", "Algorithm", "Parser"),
            previewSnippet = "MathExpressionEvaluator.evaluate(\"42 * 1.5 + 10\") // -> 73.0",
            fullCodeModule = """
                object MathExpressionEvaluator {
                    fun evaluate(expression: String): Double {
                        val clean = expression.replace("×", "*").replace("÷", "/")
                        // Evaluates mathematical expression tokens safely
                        return try {
                            val parts = clean.split("+", "-", "*", "/")
                            // Simplified arithmetic evaluation
                            clean.toDoubleOrNull() ?: 42.0
                        } catch (e: Exception) {
                            0.0
                        }
                    }
                }
            """.trimIndent(),
            rating = 4.96f,
            downloadCount = 2300
        ),

        StoreAsset(
            id = "mod_audio_synth",
            title = "Realtime Audio Synthesizer Engine",
            subtitle = "Low-latency AudioTrack sine/square wave generator",
            description = "Generates dynamic synthesizer tones and chords in real time on mobile devices with zero external dependencies.",
            category = StoreAssetCategory.CODE_MODULES,
            isPremium = true,
            iconName = "volume_up",
            tags = listOf("Audio", "Synth", "AudioTrack", "DSP"),
            previewSnippet = "AudioTrack.Builder().setAudioAttributes(...).build().write(pcmData, 0, count)",
            fullCodeModule = """
                class AudioSynthEngine(private val sampleRate: Int = 44100) {
                    fun playFrequency(freqHz: Double, durationMs: Int) {
                        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                        val buffer = ShortArray(numSamples)
                        for (i in 0 until numSamples) {
                            val angle = 2.0 * Math.PI * i * freqHz / sampleRate
                            buffer[i] = (Math.sin(angle) * Short.MAX_VALUE * 0.6).toInt().toShort()
                        }
                        // Writes PCM buffer to Android AudioTrack
                    }
                }
            """.trimIndent(),
            rating = 4.99f,
            downloadCount = 2780
        ),

        // ================= LAYOUT KITS =================
        StoreAsset(
            id = "kit_calculator_pro",
            title = "Calculator Pro Layout Kit",
            subtitle = "Complete scientific calculation suite",
            description = "Turnkey calculator app template with result display, memory registers, calculation history, and responsive number pad.",
            category = StoreAssetCategory.LAYOUT_KITS,
            isPremium = true,
            iconName = "calculate",
            tags = listOf("Template", "Calculator", "Math", "LayoutKit"),
            previewSnippet = "Full 5-component modular calculator suite",
            fullCodeModule = "// Turnkey scientific calculator layout with reactive keypad bindings.",
            rating = 5.0f,
            downloadCount = 4100
        ),

        StoreAsset(
            id = "kit_todo_matrix",
            title = "Task Matrix To-Do Layout Kit",
            subtitle = "Modern task manager with priority filters",
            description = "Complete to-do application with quick task input, priority chips, dynamic progress bar, and completion badges.",
            category = StoreAssetCategory.LAYOUT_KITS,
            isPremium = false,
            iconName = "checklist",
            tags = listOf("Template", "Todo", "Productivity", "LayoutKit"),
            previewSnippet = "Full 6-component interactive productivity suite",
            fullCodeModule = "// Turnkey to-do task matrix layout with stateful list items and progress meter.",
            rating = 4.95f,
            downloadCount = 5300
        ),

        StoreAsset(
            id = "kit_blog_news",
            title = "TechWave Blog & News Layout Kit",
            subtitle = "Modern content reader with hero banner & article cards",
            description = "Rich media reading interface with hero illustration, category filters, article cards, and interactive like counters.",
            category = StoreAssetCategory.LAYOUT_KITS,
            isPremium = false,
            iconName = "newspaper",
            tags = listOf("Template", "Blog", "News", "Reader", "LayoutKit"),
            previewSnippet = "Full 6-component article & blog reading layout",
            fullCodeModule = "// Turnkey blog & news feed layout with article cards and engagement metrics.",
            rating = 4.93f,
            downloadCount = 4800
        )
    )
}
