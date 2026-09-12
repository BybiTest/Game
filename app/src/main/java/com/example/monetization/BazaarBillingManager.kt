package com.example.monetization

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.android.vending.billing.IInAppBillingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed interface BazaarPurchaseResult {
    data class Success(
        val sku: String,
        val orderId: String,
        val purchaseToken: String,
        val isSubscription: Boolean
    ) : BazaarPurchaseResult
    data class Canceled(val message: String = "کاربر از پرداخت انصراف داد.") : BazaarPurchaseResult
    data class Error(val message: String) : BazaarPurchaseResult
}

class BazaarBillingManager private constructor() {

    companion object {
        private const val TAG = "BazaarBilling"
        const val RC_BAZAAR_PURCHASE = 10001
        private const val BAZAAR_PACKAGE = "com.farsitel.bazaar"
        private const val BAZAAR_BILLING_ACTION = "ir.cafebazaar.pardakht.InAppBillingService.BIND"

        // Cafe Bazaar Billing Response Codes
        const val BILLING_RESPONSE_RESULT_OK = 0
        const val BILLING_RESPONSE_RESULT_USER_CANCELED = 1
        const val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3
        const val BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4
        const val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5
        const val BILLING_RESPONSE_RESULT_ERROR = 6
        const val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7
        const val BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8

        // Standard SKUs for Kalame Pich in Cafe Bazaar
        const val SKU_VIP_MONTHLY = "vip_monthly"
        const val SKU_VIP_SEASONAL = "vip_seasonal"
        const val SKU_VIP_LIFETIME = "vip_lifetime"
        const val SKU_COINS_PACK_500 = "coins_pack_500"

        @Volatile
        private var instance: BazaarBillingManager? = null

        fun getInstance(): BazaarBillingManager {
            return instance ?: synchronized(this) {
                instance ?: BazaarBillingManager().also { instance = it }
            }
        }
    }

    private var billingService: IInAppBillingService? = null
    private var isBound = false
    private var isConnecting = false

    /**
     * Your Cafe Bazaar RSA Public Key from Cafe Bazaar Developer Console
     * (پیشخان کافه‌بازار -> برنامه‌ها -> برنامه -> پرداخت درون‌برنامه‌ای -> کلید RSA)
     */
    var rsaPublicKey: String = "YOUR_BAZAAR_RSA_PUBLIC_KEY"

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i(TAG, "Connected to Cafe Bazaar billing service.")
            billingService = IInAppBillingService.Stub.asInterface(service)
            isBound = true
            isConnecting = false
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "Disconnected from Cafe Bazaar billing service.")
            billingService = null
            isBound = false
            isConnecting = false
        }
    }

    /**
     * Connect to Cafe Bazaar In-App Billing Service
     */
    fun connect(context: Context, onReady: ((Boolean) -> Unit)? = null) {
        if (isBound && billingService != null) {
            onReady?.invoke(true)
            return
        }

        if (isConnecting) return
        isConnecting = true

        try {
            val serviceIntent = Intent(BAZAAR_BILLING_ACTION).apply {
                setPackage(BAZAAR_PACKAGE)
            }

            val packageManager = context.packageManager
            val resolveInfos = packageManager.queryIntentServices(serviceIntent, 0)

            if (resolveInfos.isNotEmpty()) {
                val bound = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                Log.i(TAG, "Binding to Bazaar service result: $bound")
                onReady?.invoke(bound)
            } else {
                Log.w(TAG, "Cafe Bazaar application is not installed on this device.")
                isConnecting = false
                onReady?.invoke(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to Bazaar billing", e)
            isConnecting = false
            onReady?.invoke(false)
        }
    }

    fun disconnect(context: Context) {
        if (isBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding from Bazaar service", e)
            }
            isBound = false
            billingService = null
        }
    }

    fun isServiceReady(): Boolean = isBound && billingService != null

    /**
     * Launches Cafe Bazaar native purchase flow
     */
    fun launchPurchase(
        activity: Activity,
        sku: String,
        isSubscription: Boolean = false,
        developerPayload: String = "kalame_pich_user"
    ): Boolean {
        val service = billingService
        if (service == null) {
            Log.e(TAG, "Billing service not bound. Reconnecting...")
            connect(activity.applicationContext) { ready ->
                if (ready) {
                    launchPurchase(activity, sku, isSubscription, developerPayload)
                } else {
                    promptInstallBazaar(activity)
                }
            }
            return false
        }

        return try {
            val itemType = if (isSubscription) "subs" else "inapp"
            val buyIntentBundle: Bundle? = service.getBuyIntent(
                3,
                activity.packageName,
                sku,
                itemType,
                developerPayload
            )

            val responseCode = buyIntentBundle?.getInt("RESPONSE_CODE", BILLING_RESPONSE_RESULT_ERROR)
                ?: BILLING_RESPONSE_RESULT_ERROR

            if (responseCode == BILLING_RESPONSE_RESULT_OK) {
                @Suppress("DEPRECATION")
                val pendingIntent: PendingIntent? = buyIntentBundle?.getParcelable("BUY_INTENT")
                if (pendingIntent != null) {
                    activity.startIntentSenderForResult(
                        pendingIntent.intentSender,
                        RC_BAZAAR_PURCHASE,
                        Intent(),
                        0,
                        0,
                        0
                    )
                    true
                } else {
                    Log.e(TAG, "BUY_INTENT PendingIntent is null")
                    false
                }
            } else {
                Log.e(TAG, "Bazaar getBuyIntent response error code: $responseCode")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Bazaar purchase flow", e)
            false
        }
    }

    /**
     * Handle onActivityResult in Activity
     */
    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onResult: (BazaarPurchaseResult) -> Unit
    ): Boolean {
        if (requestCode != RC_BAZAAR_PURCHASE) {
            return false
        }

        if (data == null) {
            onResult(BazaarPurchaseResult.Error("اطلاعات خرید دریافت نشد."))
            return true
        }

        val responseCode = data.getIntExtra("RESPONSE_CODE", BILLING_RESPONSE_RESULT_OK)
        val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")
        val dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE")

        if (resultCode == Activity.RESULT_OK && responseCode == BILLING_RESPONSE_RESULT_OK) {
            if (purchaseData == null || dataSignature == null) {
                onResult(BazaarPurchaseResult.Error("امضای دیجیتال خرید نامعتبر است."))
                return true
            }

            // Verify with RSA key (if developer provided key, verify; otherwise log warning in debug)
            val isValid = if (rsaPublicKey.isNotBlank() && !rsaPublicKey.contains("YOUR_BAZAAR_RSA_PUBLIC_KEY")) {
                BazaarSecurity.verifyPurchase(rsaPublicKey, purchaseData, dataSignature)
            } else {
                Log.w(TAG, "RSA Public Key not set; accepting purchase in dev mode.")
                true
            }

            if (!isValid) {
                onResult(BazaarPurchaseResult.Error("اعتبارسنجی امنیتی خرید در بازار ناموفق بود."))
                return true
            }

            try {
                val json = JSONObject(purchaseData)
                val sku = json.getString("productId")
                val orderId = json.optString("orderId", "")
                val purchaseToken = json.getString("purchaseToken")
                val isSub = sku.startsWith("vip_")

                onResult(BazaarPurchaseResult.Success(sku, orderId, purchaseToken, isSub))
            } catch (e: Exception) {
                Log.e(TAG, "JSON parsing error for purchase data", e)
                onResult(BazaarPurchaseResult.Error("خطا در پردازش اطلاعات خرید."))
            }
        } else if (resultCode == Activity.RESULT_CANCELED || responseCode == BILLING_RESPONSE_RESULT_USER_CANCELED) {
            onResult(BazaarPurchaseResult.Canceled())
        } else {
            onResult(BazaarPurchaseResult.Error("خطا در پرداخت کافه‌بازار (کد خطا: $responseCode)"))
        }

        return true
    }

    /**
     * Consumes a consumable item (e.g. coin pack) so user can buy it again
     */
    fun consumePurchase(context: Context, purchaseToken: String, onConsumed: ((Boolean) -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            val service = billingService
            if (service == null) {
                withContext(Dispatchers.Main) { onConsumed?.invoke(false) }
                return@launch
            }

            try {
                val response = service.consumePurchase(3, context.packageName, purchaseToken)
                val success = (response == BILLING_RESPONSE_RESULT_OK)
                withContext(Dispatchers.Main) {
                    onConsumed?.invoke(success)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error consuming purchase", e)
                withContext(Dispatchers.Main) { onConsumed?.invoke(false) }
            }
        }
    }

    /**
     * Restore already purchased items or subscriptions on app start
     */
    suspend fun queryActivePurchases(context: Context): List<String> = withContext(Dispatchers.IO) {
        val ownedSkus = mutableListOf<String>()
        val service = billingService ?: return@withContext emptyList()

        try {
            val ownedBundle = service.getPurchases(3, context.packageName, "inapp", null)
            val responseCode = ownedBundle?.getInt("RESPONSE_CODE", -1) ?: -1
            if (responseCode == BILLING_RESPONSE_RESULT_OK) {
                val ownedItemList = ownedBundle?.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")
                if (ownedItemList != null) {
                    ownedSkus.addAll(ownedItemList)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query purchases from Bazaar", e)
        }
        ownedSkus
    }

    /**
     * Open Cafe Bazaar install page if Bazaar is not installed
     */
    fun promptInstallBazaar(activity: Activity) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cafebazaar.ir/download")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open browser to install Bazaar", e)
        }
    }
}
