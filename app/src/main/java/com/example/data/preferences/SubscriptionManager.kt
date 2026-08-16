package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.StoreAsset
import com.example.data.model.SubscriptionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubscriptionManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_apk_builder_prefs", Context.MODE_PRIVATE)

    private val _subscriptionStatus = MutableStateFlow(loadSubscriptionStatus())
    val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    private fun loadSubscriptionStatus(): SubscriptionStatus {
        val isPremium = prefs.getBoolean(KEY_IS_PREMIUM, false)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val code = prefs.getString(KEY_REDEEMED_CODE, null)
        val credits = prefs.getInt(KEY_DEV_CREDITS, DEFAULT_INITIAL_CREDITS)
        val purchased = prefs.getStringSet(KEY_PURCHASED_ADDONS, emptySet()) ?: emptySet()
        val installed = prefs.getStringSet(KEY_INSTALLED_ADDONS, emptySet()) ?: emptySet()

        val now = System.currentTimeMillis()
        val currentlyActive = isPremium && (expiresAt == 0L || expiresAt > now)

        return if (currentlyActive) {
            SubscriptionStatus(
                isPremium = true,
                tierName = "Developer Pro (3 Months Free)",
                expiresAtTimestamp = expiresAt,
                redeemedCode = code ?: "dev15",
                devCredits = credits,
                purchasedAddOnIds = purchased,
                installedAddOnIds = installed,
                unlockedPerks = listOf(
                    "All In-App Store Assets & Unlimited Add-On SDKs",
                    "Advanced AI Models (Claude 3.5 Sonnet, DeepSeek Coder V2, Gemini 1.5 Pro)",
                    "Custom Datasets & Knowledge Base Schema Ingestion",
                    "Unlimited High-Speed Signed APK Compilation",
                    "Full Jetpack Compose Source Code Export & Offline Testing"
                )
            )
        } else {
            SubscriptionStatus(
                isPremium = false,
                tierName = "Free Starter",
                expiresAtTimestamp = 0L,
                redeemedCode = null,
                devCredits = credits,
                purchasedAddOnIds = purchased,
                installedAddOnIds = installed,
                unlockedPerks = listOf(
                    "Standard UI Component Library",
                    "Gemini 1.5 Flash Model",
                    "Local APK Compilation",
                    "Interactive Smartphone Simulator"
                )
            )
        }
    }

    fun redeemCode(inputCode: String): Result<SubscriptionStatus> {
        val clean = inputCode.trim()
        if (clean.equals("dev15", ignoreCase = true)) {
            val threeMonthsMs = 90L * 24L * 60L * 60L * 1000L
            val expiry = System.currentTimeMillis() + threeMonthsMs
            val currentCredits = prefs.getInt(KEY_DEV_CREDITS, DEFAULT_INITIAL_CREDITS)
            val bonusCredits = currentCredits + 2500 // Bonus 2,500 credits on Pro promo unlock!

            prefs.edit()
                .putBoolean(KEY_IS_PREMIUM, true)
                .putLong(KEY_EXPIRES_AT, expiry)
                .putString(KEY_REDEEMED_CODE, "dev15")
                .putInt(KEY_DEV_CREDITS, bonusCredits)
                .apply()

            val updated = loadSubscriptionStatus()
            _subscriptionStatus.value = updated
            return Result.success(updated)
        } else if (clean.equals("BONUS500", ignoreCase = true) || clean.equals("CREDITS500", ignoreCase = true)) {
            val currentCredits = prefs.getInt(KEY_DEV_CREDITS, DEFAULT_INITIAL_CREDITS)
            val newCredits = currentCredits + 500
            prefs.edit().putInt(KEY_DEV_CREDITS, newCredits).apply()
            val updated = loadSubscriptionStatus()
            _subscriptionStatus.value = updated
            return Result.success(updated)
        } else {
            return Result.failure(IllegalArgumentException("Invalid promo code. Use 'dev15' for 3 Months Free Pro + 2,500 Credits!"))
        }
    }

    fun topUpCredits(amount: Int): Int {
        val currentCredits = prefs.getInt(KEY_DEV_CREDITS, DEFAULT_INITIAL_CREDITS)
        val newCredits = currentCredits + amount
        prefs.edit().putInt(KEY_DEV_CREDITS, newCredits).apply()
        _subscriptionStatus.value = loadSubscriptionStatus()
        return newCredits
    }

    fun buyAddOn(asset: StoreAsset): Result<String> {
        val currentStatus = _subscriptionStatus.value
        val isPurchased = currentStatus.isPremium || currentStatus.purchasedAddOnIds.contains(asset.id)
        if (isPurchased) {
            // Already unlocked, ensure installed
            toggleInstallAddOn(asset.id, forceInstall = true)
            return Result.success("Add-On already in library. Installed and enabled for projects!")
        }

        val price = asset.priceCredits
        if (currentStatus.devCredits < price) {
            return Result.failure(
                IllegalStateException(
                    "Insufficient Dev Credits (${currentStatus.devCredits} available, $price required). Top up credits or redeem code 'dev15'!"
                )
            )
        }

        // Deduct credits and record purchase
        val remainingCredits = currentStatus.devCredits - price
        val newPurchased = currentStatus.purchasedAddOnIds.toMutableSet().apply { add(asset.id) }
        val newInstalled = currentStatus.installedAddOnIds.toMutableSet().apply { add(asset.id) }

        prefs.edit()
            .putInt(KEY_DEV_CREDITS, remainingCredits)
            .putStringSet(KEY_PURCHASED_ADDONS, newPurchased)
            .putStringSet(KEY_INSTALLED_ADDONS, newInstalled)
            .apply()

        _subscriptionStatus.value = loadSubscriptionStatus()
        return Result.success("Successfully purchased ${asset.title} for $price credits! Auto-installed.")
    }

    fun toggleInstallAddOn(assetId: String, forceInstall: Boolean? = null): Boolean {
        val currentStatus = _subscriptionStatus.value
        val currentInstalled = currentStatus.installedAddOnIds.toMutableSet()
        val willInstall = forceInstall ?: !currentInstalled.contains(assetId)

        if (willInstall) {
            currentInstalled.add(assetId)
        } else {
            currentInstalled.remove(assetId)
        }

        prefs.edit().putStringSet(KEY_INSTALLED_ADDONS, currentInstalled).apply()
        _subscriptionStatus.value = loadSubscriptionStatus()
        return willInstall
    }

    fun isAddOnUnlocked(assetId: String, isAssetPremium: Boolean): Boolean {
        val status = _subscriptionStatus.value
        if (status.isPremium) return true
        if (!isAssetPremium) return true
        return status.purchasedAddOnIds.contains(assetId)
    }

    fun isAddOnInstalled(assetId: String): Boolean {
        return _subscriptionStatus.value.installedAddOnIds.contains(assetId)
    }

    fun isFeatureUnlocked(isPremiumRequired: Boolean): Boolean {
        if (!isPremiumRequired) return true
        val status = _subscriptionStatus.value
        val now = System.currentTimeMillis()
        return status.isPremium && (status.expiresAtTimestamp == 0L || status.expiresAtTimestamp > now)
    }

    fun formatExpiryDate(timestamp: Long): String {
        if (timestamp <= 0L) return "Lifetime / Unlimited"
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun resetSubscription() {
        prefs.edit()
            .putBoolean(KEY_IS_PREMIUM, false)
            .putLong(KEY_EXPIRES_AT, 0L)
            .putInt(KEY_DEV_CREDITS, DEFAULT_INITIAL_CREDITS)
            .remove(KEY_REDEEMED_CODE)
            .remove(KEY_PURCHASED_ADDONS)
            .remove(KEY_INSTALLED_ADDONS)
            .apply()
        _subscriptionStatus.value = loadSubscriptionStatus()
    }

    companion object {
        private const val KEY_IS_PREMIUM = "key_is_premium"
        private const val KEY_EXPIRES_AT = "key_expires_at"
        private const val KEY_REDEEMED_CODE = "key_redeemed_code"
        private const val KEY_DEV_CREDITS = "key_dev_credits"
        private const val KEY_PURCHASED_ADDONS = "key_purchased_addons"
        private const val KEY_INSTALLED_ADDONS = "key_installed_addons"

        private const val DEFAULT_INITIAL_CREDITS = 1500

        @Volatile
        private var instance: SubscriptionManager? = null

        fun getInstance(context: Context): SubscriptionManager {
            return instance ?: synchronized(this) {
                instance ?: SubscriptionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
