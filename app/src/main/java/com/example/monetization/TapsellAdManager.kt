package com.example.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.example.BuildConfig
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworkError
import ir.tapsell.plus.model.AdNetworks
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel

class TapsellAdManager private constructor() : AdManager {

    companion object {
        private const val TAG = "TapsellAdManager"
        const val REWARD_COINS = 50

        const val DEFAULT_APP_KEY = "mcqrarnosbkggjjikbfpboggaitidsoteapnsrrgnfcjgnopapqbljbhmnkcmdmhfodkes"
        const val DEFAULT_REWARDED_ZONE_ID = "6aa55a69cd33cd4ed6e43183"
        const val DEFAULT_BANNER_ZONE_ID = "6aa55a83cd33cd4ed6e43184"

        @Volatile
        private var instance: TapsellAdManager? = null

        fun getInstance(): TapsellAdManager {
            return instance ?: synchronized(this) {
                instance ?: TapsellAdManager().also {
                    instance = it
                }
            }
        }
    }

    private var appKey: String = BuildConfig.TAPSELL_APP_KEY.ifBlank { DEFAULT_APP_KEY }
    private var rewardedZoneId: String = BuildConfig.TAPSELL_REWARDED_ZONE_ID.ifBlank { DEFAULT_REWARDED_ZONE_ID }
    private var bannerZoneId: String = BuildConfig.TAPSELL_BANNER_ZONE_ID.ifBlank { DEFAULT_BANNER_ZONE_ID }
    private var initialized = false

    private var rewardedResponseId: String? = null
    private var rewardedResponseAlreadyDelivered: String? = null

    override fun initialize(context: Context, appKey: String) {
        val key = if (appKey.isNotBlank()) appKey else this.appKey
        this.appKey = key

        try {
            TapsellPlus.initialize(
                context.applicationContext,
                key,
                object : TapsellPlusInitListener {
                    override fun onInitializeSuccess(adNetworks: AdNetworks) {
                        initialized = true
                        Log.i(TAG, "Tapsell Plus initialized successfully: $adNetworks")
                    }

                    override fun onInitializeFailed(
                        adNetworks: AdNetworks,
                        adNetworkError: AdNetworkError
                    ) {
                        initialized = false
                        Log.e(TAG, "Tapsell Plus initialization failed: ${adNetworkError.errorMessage}")
                    }
                }
            )
            initialized = true
            Log.i(TAG, "Tapsell Plus init called.")
        } catch (e: Exception) {
            initialized = false
            Log.e(TAG, "Tapsell Plus initialization exception", e)
        }
    }

    fun isConfigured(): Boolean {
        return appKey.isNotBlank() && !appKey.contains("YOUR_TAPSELL_APP_KEY", ignoreCase = true)
    }

    override fun isInitialized(): Boolean = initialized

    override fun isRewardedAdReady(): Boolean = !rewardedResponseId.isNullOrBlank()

    fun getAppKey(): String = appKey
    fun getRewardedZoneId(): String = rewardedZoneId
    fun getBannerZoneId(): String = bannerZoneId

    fun updateZones(rewarded: String, banner: String) {
        if (rewarded.isNotBlank()) rewardedZoneId = rewarded
        if (banner.isNotBlank()) bannerZoneId = banner
    }

    override fun requestRewardedVideo(zoneId: String, listener: RewardedAdListener?) {
        listener?.onAdFailedToLoad("Please use requestRewardedVideoFromActivity(activity, zoneId, listener)")
    }

    fun requestRewardedVideoFromActivity(
        activity: Activity,
        zoneId: String = rewardedZoneId,
        listener: RewardedAdListener? = null
    ) {
        if (!isConfigured()) {
            val err = "Tapsell App Key is not configured."
            Log.e(TAG, err)
            listener?.onAdFailedToLoad(err)
            return
        }

        val targetZone = if (zoneId.isNotBlank()) zoneId else rewardedZoneId
        rewardedResponseId = null
        rewardedResponseAlreadyDelivered = null

        try {
            TapsellPlus.requestRewardedVideoAd(
                activity,
                targetZone,
                object : AdRequestCallback() {
                    override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                        val responseId = tapsellPlusAdModel.responseId
                        if (responseId.isNullOrBlank()) {
                            val err = "Tapsell returned empty response ID"
                            Log.e(TAG, err)
                            listener?.onAdFailedToLoad(err)
                            return
                        }
                        rewardedResponseId = responseId
                        Log.i(TAG, "Tapsell rewarded ad loaded. responseId: $responseId")
                        listener?.onAdLoaded()
                    }

                    override fun error(message: String) {
                        rewardedResponseId = null
                        Log.e(TAG, "Tapsell rewarded ad failed: $message")
                        listener?.onAdFailedToLoad(message)
                    }
                }
            )
        } catch (e: Exception) {
            rewardedResponseId = null
            Log.e(TAG, "Exception requesting rewarded ad", e)
            listener?.onAdFailedToLoad(e.message ?: "Unknown error")
        }
    }

    override fun showRewardedVideo(
        activity: Activity,
        zoneId: String,
        listener: RewardedAdListener
    ) {
        val responseId = rewardedResponseId
        if (responseId.isNullOrBlank()) {
            val err = "No rewarded ad is ready to show."
            Log.e(TAG, err)
            listener.onAdShowFailed(err)
            return
        }

        rewardedResponseId = null // consume response

        try {
            TapsellPlus.showRewardedVideoAd(
                activity,
                responseId,
                object : AdShowListener() {
                    override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                        Log.d(TAG, "Rewarded video opened")
                        listener.onAdOpened()
                    }

                    override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                        val rewardDelivered = rewardedResponseAlreadyDelivered == responseId
                        Log.d(TAG, "Rewarded video closed. rewardDelivered=$rewardDelivered")
                        listener.onAdClosed(rewardCompleted = rewardDelivered)
                    }

                    override fun onRewarded(tapsellPlusAdModel: TapsellPlusAdModel) {
                        if (rewardedResponseAlreadyDelivered == responseId) {
                            Log.w(TAG, "Duplicate reward callback ignored")
                            return
                        }
                        rewardedResponseAlreadyDelivered = responseId
                        Log.i(TAG, "REAL TAPSELL REWARD VERIFIED!")
                        listener.onRewardEarned(REWARD_COINS)
                    }

                    override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                        val err = tapsellPlusErrorModel.toString()
                        Log.e(TAG, "Rewarded video error: $err")
                        listener.onAdShowFailed(err)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception showing rewarded video", e)
            listener.onAdShowFailed(e.message ?: "Unknown error")
        }
    }

    fun requestStandardBanner(
        activity: Activity,
        container: ViewGroup,
        zoneId: String = bannerZoneId,
        listener: BannerAdListener? = null
    ) {
        val targetZone = if (zoneId.isNotBlank()) zoneId else bannerZoneId
        try {
            TapsellPlus.requestStandardBannerAd(
                activity,
                targetZone,
                TapsellPlusBannerType.BANNER_320x50,
                object : AdRequestCallback() {
                    override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                        val responseId = tapsellPlusAdModel.responseId
                        try {
                            TapsellPlus.showStandardBannerAd(
                                activity,
                                responseId,
                                container,
                                object : AdShowListener() {
                                    override fun onOpened(adModel: TapsellPlusAdModel) {
                                        Log.d(TAG, "Standard banner opened/displayed")
                                        listener?.onAdLoaded()
                                    }

                                    override fun onError(errorModel: TapsellPlusErrorModel) {
                                        Log.e(TAG, "Standard banner show error: $errorModel")
                                        listener?.onAdFailedToLoad(errorModel.toString())
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception showing standard banner", e)
                            listener?.onAdFailedToLoad(e.message ?: "Show error")
                        }
                    }

                    override fun error(message: String) {
                        Log.e(TAG, "Standard banner request error: $message")
                        listener?.onAdFailedToLoad(message)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting standard banner", e)
            listener?.onAdFailedToLoad(e.message ?: "Request error")
        }
    }
}
