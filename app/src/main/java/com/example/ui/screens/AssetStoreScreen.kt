package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppProject
import com.example.data.model.StoreAsset
import com.example.data.model.StoreAssetCategory
import com.example.data.store.AssetStoreData
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetStoreScreen(
    viewModel: StudioViewModel,
    activeProjectId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAssetAddedToProject: () -> Unit = {}
) {
    val context = LocalContext.current
    val subStatus by viewModel.subscriptionStatus.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(StoreAssetCategory.ALL) }
    var filterTier by remember { mutableStateOf("ALL") } // ALL, ADD_ONS, FREE, PRO, PURCHASED
    var selectedAssetForDetail by remember { mutableStateOf<StoreAsset?>(null) }
    var showProjectPickerDialog by remember { mutableStateOf<StoreAsset?>(null) }
    var showUpgradePromptDialog by remember { mutableStateOf(false) }
    var showCreditsTopUpDialog by remember { mutableStateOf(false) }

    val filteredAssets = remember(searchQuery, selectedCategory, filterTier, subStatus) {
        AssetStoreData.STORE_ASSETS.filter { asset ->
            val matchesCategory = selectedCategory == StoreAssetCategory.ALL || asset.category == selectedCategory
            val matchesTier = when (filterTier) {
                "ADD_ONS" -> asset.category == StoreAssetCategory.ADD_ONS
                "FREE" -> !asset.isPremium && asset.priceCredits == 0
                "PRO" -> asset.isPremium
                "PURCHASED" -> subStatus.isPremium || subStatus.purchasedAddOnIds.contains(asset.id)
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    asset.title.contains(searchQuery, ignoreCase = true) ||
                    asset.subtitle.contains(searchQuery, ignoreCase = true) ||
                    asset.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesTier && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Marketplace & Add-Ons",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (subStatus.isPremium) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SleekWarning.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SleekWarning)
                                ) {
                                    Text(
                                        text = "PRO VIP",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekWarning,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Buy Add-Ons, SDKs, Widgets & Code Modules",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("store_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SleekTextPrimary
                        )
                    }
                },
                actions = {
                    // Dev Credits Pill Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showCreditsTopUpDialog = true }
                            .padding(end = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${subStatus.devCredits}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Top Up",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("store_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & Codes",
                            tint = SleekTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SleekSurface)
            )
        },
        containerColor = SleekBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            // ================= MARKETPLACE WALLET & PRO HERO CARD =================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF6366F1).copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = Color(0xFF818CF8),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Developer Marketplace & SDKs",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Purchase & integrate verified native modules",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Balance Pill
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🪙", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${subStatus.devCredits} Credits",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFBBF24)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.claimDevCreditsGrant(500)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Claim +500 Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showCreditsTopUpDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFBBF24)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Top Up Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ================= SEARCH & TIER FILTER BAR =================
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("store_search_input"),
                    placeholder = { Text("Search Add-Ons, SDKs, UI widgets, icons...", fontSize = 13.sp, color = SleekTextMuted) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SleekTextMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = SleekTextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekCardBorder,
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    )
                )
            }

            // ================= CATEGORY TABS =================
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    val categories = listOf(
                        StoreAssetCategory.ALL to "All Assets",
                        StoreAssetCategory.ADD_ONS to "⚡ Add-Ons & SDKs",
                        StoreAssetCategory.UI_COMPONENTS to "UI Widgets",
                        StoreAssetCategory.VECTOR_ICONS to "Icon Packs",
                        StoreAssetCategory.CODE_MODULES to "Code Modules",
                        StoreAssetCategory.LAYOUT_KITS to "Layout Kits"
                    )
                    items(categories) { (cat, label) ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (cat == StoreAssetCategory.ADD_ONS) Color(0xFF6366F1) else SleekPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = SleekSurface,
                                labelColor = SleekTextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) (if (cat == StoreAssetCategory.ADD_ONS) Color(0xFF6366F1) else SleekPrimary) else SleekCardBorder
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // ================= TIER FILTER PILLS =================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "All Items",
                        "ADD_ONS" to "Add-Ons ⚡",
                        "FREE" to "Free",
                        "PRO" to "Pro ★",
                        "PURCHASED" to "My Unlocked ✓"
                    ).forEach { (tier, label) ->
                        val isSelected = filterTier == tier
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { filterTier = tier },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SleekSurfaceContainer else SleekSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SleekPrimary else SleekCardBorder
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SleekPrimary else SleekTextSecondary,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // ================= ASSET LIST =================
            if (filteredAssets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = SleekTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No store items match your filter", color = SleekTextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(filteredAssets, key = { it.id }) { asset ->
                    val isPurchased = subStatus.isPremium || subStatus.purchasedAddOnIds.contains(asset.id)
                    val isInstalled = subStatus.installedAddOnIds.contains(asset.id)

                    StoreAssetCard(
                        asset = asset,
                        isUserPro = subStatus.isPremium,
                        isPurchased = isPurchased,
                        isInstalled = isInstalled,
                        onInspect = { selectedAssetForDetail = asset },
                        onBuy = {
                            if (subStatus.isPremium || asset.priceCredits == 0) {
                                viewModel.buyStoreAddOn(asset)
                            } else if (subStatus.devCredits >= asset.priceCredits) {
                                viewModel.buyStoreAddOn(asset)
                            } else {
                                showCreditsTopUpDialog = true
                            }
                        },
                        onToggleInstall = {
                            viewModel.toggleAddOnInstallation(asset.id)
                        },
                        onAddToProject = {
                            val isUnlocked = !asset.isPremium || isPurchased || subStatus.isPremium
                            if (!isUnlocked) {
                                selectedAssetForDetail = asset
                            } else {
                                if (activeProjectId != null) {
                                    viewModel.installStoreAssetToProject(asset, activeProjectId) {
                                        onAssetAddedToProject()
                                    }
                                } else {
                                    showProjectPickerDialog = asset
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // ================= DETAIL & CHECKOUT MODAL =================
    selectedAssetForDetail?.let { asset ->
        val isPurchased = subStatus.isPremium || subStatus.purchasedAddOnIds.contains(asset.id)
        val isInstalled = subStatus.installedAddOnIds.contains(asset.id)

        AddOnDetailAndCheckoutDialog(
            asset = asset,
            subStatus = subStatus,
            isPurchased = isPurchased,
            isInstalled = isInstalled,
            onDismiss = { selectedAssetForDetail = null },
            onBuy = {
                if (subStatus.isPremium || asset.priceCredits == 0 || subStatus.devCredits >= asset.priceCredits) {
                    viewModel.buyStoreAddOn(asset)
                } else {
                    showCreditsTopUpDialog = true
                }
            },
            onToggleInstall = {
                viewModel.toggleAddOnInstallation(asset.id)
            },
            onAddToProject = {
                selectedAssetForDetail = null
                if (activeProjectId != null) {
                    viewModel.installStoreAssetToProject(asset, activeProjectId) {
                        onAssetAddedToProject()
                    }
                } else {
                    showProjectPickerDialog = asset
                }
            },
            onCopyCode = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Compose Code", asset.fullCodeModule))
                Toast.makeText(context, "Kotlin code copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ================= TOP UP CREDITS & WALLET MODAL =================
    if (showCreditsTopUpDialog) {
        TopUpCreditsDialog(
            currentCredits = subStatus.devCredits,
            isPro = subStatus.isPremium,
            onDismiss = { showCreditsTopUpDialog = false },
            onClaimFreeGrant = { amount ->
                viewModel.claimDevCreditsGrant(amount)
                showCreditsTopUpDialog = false
            },
            onPurchasePack = { amount ->
                viewModel.claimDevCreditsGrant(amount)
                showCreditsTopUpDialog = false
            },
            onRedeemCode = { code ->
                viewModel.redeemPromoCode(code)
                showCreditsTopUpDialog = false
            }
        )
    }

    // ================= PROJECT PICKER MODAL =================
    showProjectPickerDialog?.let { asset ->
        AlertDialog(
            onDismissRequest = { showProjectPickerDialog = null },
            title = {
                Text(
                    text = "Add to Project",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Select an existing project to insert '${asset.title}':",
                        fontSize = 13.sp,
                        color = SleekTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (allProjects.isEmpty()) {
                        Text("No projects available. Create a project first!", color = SleekTextMuted, fontSize = 12.sp)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                            items(allProjects) { proj ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.installStoreAssetToProject(asset, proj.id) {
                                                showProjectPickerDialog = null
                                                onAssetAddedToProject()
                                            }
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = SleekSurfaceContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Android, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(proj.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekTextPrimary)
                                            Text(proj.category, fontSize = 11.sp, color = SleekTextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProjectPickerDialog = null }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface
        )
    }

    // ================= UPGRADE PROMPT DIALOG =================
    if (showUpgradePromptDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradePromptDialog = false },
            icon = {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = SleekWarning.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SleekWarning)
                    }
                }
            },
            title = {
                Text("Pro Asset Locked", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SleekTextPrimary)
            },
            text = {
                Text(
                    "This is a Pro Developer Asset. You can unlock all store items for free by heading to Settings and entering the promo code 'dev15'!",
                    fontSize = 13.sp,
                    color = SleekTextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpgradePromptDialog = false
                        onNavigateToSettings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Redeem 'dev15' in Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradePromptDialog = false }) {
                    Text("Close", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface
        )
    }
}

@Composable
fun StoreAssetCard(
    asset: StoreAsset,
    isUserPro: Boolean,
    isPurchased: Boolean,
    isInstalled: Boolean,
    onInspect: () -> Unit,
    onBuy: () -> Unit,
    onToggleInstall: () -> Unit,
    onAddToProject: () -> Unit
) {
    val isAddOn = asset.category == StoreAssetCategory.ADD_ONS
    val isUnlocked = !asset.isPremium || isPurchased || isUserPro

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isAddOn) Color(0xFF6366F1).copy(alpha = 0.35f) else SleekCardBorder,
                RoundedCornerShape(18.dp)
            )
            .testTag("store_asset_${asset.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAddOn) Color(0xFF0F172A) else SleekSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAddOn) Color(0xFF6366F1).copy(alpha = 0.25f)
                        else if (asset.isPremium) SleekWarning.copy(alpha = 0.15f)
                        else SleekPrimaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (asset.category) {
                                    StoreAssetCategory.ADD_ONS -> Icons.Default.Extension
                                    StoreAssetCategory.UI_COMPONENTS -> Icons.Default.Widgets
                                    StoreAssetCategory.VECTOR_ICONS -> Icons.Default.Category
                                    StoreAssetCategory.CODE_MODULES -> Icons.Default.Code
                                    StoreAssetCategory.LAYOUT_KITS -> Icons.Default.Dashboard
                                    else -> Icons.Default.Extension
                                },
                                contentDescription = null,
                                tint = if (isAddOn) Color(0xFF818CF8) else if (asset.isPremium) SleekWarning else SleekPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = asset.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAddOn) Color.White else SleekTextPrimary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isAddOn) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Text(
                                        text = asset.addOnVersion,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "★ ${asset.rating} • ${asset.downloadCount}+ uses",
                                fontSize = 11.sp,
                                color = if (isAddOn) Color(0xFF94A3B8) else SleekTextSecondary
                            )
                        }
                    }
                }

                // Price / Tier Badge
                if (isAddOn) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPurchased) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isPurchased) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    ) {
                        Text(
                            text = if (isPurchased) "OWNED ✓" else if (asset.priceCredits > 0) "🪙 ${asset.priceCredits}" else "FREE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPurchased) Color(0xFF10B981) else Color(0xFFFBBF24),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (asset.isPremium) SleekWarning.copy(alpha = 0.2f) else SleekSurfaceLow,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (asset.isPremium) SleekWarning else SleekCardBorder
                        )
                    ) {
                        Text(
                            text = if (asset.isPremium) "PRO ★" else "FREE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (asset.isPremium) SleekWarning else SleekPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = asset.description,
                fontSize = 12.sp,
                color = if (isAddOn) Color(0xFFCBD5E1) else SleekTextSecondary,
                lineHeight = 16.sp,
                maxLines = 2
            )

            // Permissions or Tags
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (asset.permissionsRequired.isNotEmpty()) {
                    items(asset.permissionsRequired) { perm ->
                        val shortPerm = perm.substringAfterLast(".")
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = "🔒 $shortPerm",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                items(asset.tags) { tag ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isAddOn) Color(0xFF1E293B) else SleekSurfaceLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isAddOn) Color(0xFF334155) else SleekCardBorder)
                    ) {
                        Text(
                            text = "#$tag",
                            fontSize = 10.sp,
                            color = if (isAddOn) Color(0xFF94A3B8) else SleekTextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onInspect,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isAddOn) Color.White else SleekTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAddOn) Color(0xFF334155) else SleekCardBorder
                    )
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Details", fontSize = 12.sp)
                }

                if (isAddOn) {
                    if (!isPurchased) {
                        Button(
                            onClick = onBuy,
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1),
                                contentColor = Color.White
                            )
                        ) {
                            Text("🪙", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buy for ${asset.priceCredits}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onAddToProject,
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add to App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onAddToProject,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUnlocked) SleekPrimary else SleekSurfaceContainer,
                            contentColor = if (isUnlocked) Color.White else SleekTextMuted
                        )
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.Add else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isUnlocked) Color.White else SleekWarning
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUnlocked) "Add to Project" else "Unlock Pro",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddOnDetailAndCheckoutDialog(
    asset: StoreAsset,
    subStatus: com.example.data.model.SubscriptionStatus,
    isPurchased: Boolean,
    isInstalled: Boolean,
    onDismiss: () -> Unit,
    onBuy: () -> Unit,
    onToggleInstall: () -> Unit,
    onAddToProject: () -> Unit,
    onCopyCode: () -> Unit
) {
    val isAddOn = asset.category == StoreAssetCategory.ADD_ONS
    val isUnlocked = !asset.isPremium || isPurchased || subStatus.isPremium
    val hasEnoughCredits = subStatus.devCredits >= asset.priceCredits || subStatus.isPremium || asset.priceCredits == 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = asset.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "By ${asset.author} • ${asset.addOnVersion}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price & Wallet summary box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Marketplace Price", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = if (isPurchased) "Already Purchased ✓" else if (asset.priceCredits > 0) "🪙 ${asset.priceCredits} Dev Credits (${asset.priceUsd})" else "Free Open-Source",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPurchased) Color(0xFF10B981) else Color(0xFFFBBF24)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Your Wallet Balance", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "🪙 ${subStatus.devCredits} Credits",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = asset.description,
                            fontSize = 13.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 18.sp
                        )
                    }

                    if (asset.featuresList.isNotEmpty()) {
                        item {
                            Text(
                                text = "Verified SDK Capabilities:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                asset.featuresList.forEach { feat ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(feat, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                            }
                        }
                    }

                    if (asset.permissionsRequired.isNotEmpty()) {
                        item {
                            Text(
                                text = "Required Android Manifest Permissions:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                asset.permissionsRequired.forEach { perm ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF182234),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                                    ) {
                                        Text(
                                            text = "⚙ $perm",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF38BDF8),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kotlin Compose Integration Module:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(onClick = onCopyCode, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF030712),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = asset.fullCodeModule,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color(0xFF93C5FD),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopyCode,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Code", fontSize = 12.sp)
                    }

                    if (!isPurchased && isAddOn) {
                        Button(
                            onClick = {
                                onBuy()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1.4f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasEnoughCredits) Color(0xFF6366F1) else Color(0xFFF59E0B),
                                contentColor = Color.White
                            )
                        ) {
                            Text("🪙", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasEnoughCredits) "Buy & Unlock (${asset.priceCredits} C)" else "Top Up Credits",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                onAddToProject()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1.4f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add to Project", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopUpCreditsDialog(
    currentCredits: Int,
    isPro: Boolean,
    onDismiss: () -> Unit,
    onClaimFreeGrant: (Int) -> Unit,
    onPurchasePack: (Int) -> Unit,
    onRedeemCode: (String) -> Unit
) {
    var promoCodeInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Dev Credits Wallet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Current Balance: $currentCredits Credits", fontSize = 12.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Available Dev Grants & Credit Packs:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(modifier = Modifier.height(8.dp))

                // Daily Grant Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClaimFreeGrant(500) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎁", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Daily Dev Grant", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                Text("+500 Free Dev Credits", fontSize = 11.sp, color = Color(0xFF10B981))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981)
                        ) {
                            Text("CLAIM FREE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Starter Pack
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPurchasePack(1200) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Pro Hacker Pack", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                Text("+1,200 Dev Credits", fontSize = 11.sp, color = Color(0xFFFBBF24))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF6366F1)
                        ) {
                            Text("GET $2.99", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Enterprise Stack
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPurchasePack(3500) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Master Studio Stack", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                Text("+3,500 Dev Credits", fontSize = 11.sp, color = Color(0xFFFBBF24))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF8B5CF6)
                        ) {
                            Text("GET $6.99", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Promo code redemption
                Text("Redeem Promo Code (e.g. 'dev15'):", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = promoCodeInput,
                        onValueChange = { promoCodeInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter dev15", fontSize = 12.sp, color = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Button(
                        onClick = {
                            if (promoCodeInput.isNotBlank()) {
                                onRedeemCode(promoCodeInput.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
