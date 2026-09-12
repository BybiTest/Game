package com.example.monetization

import android.app.Activity
import android.content.Context
import android.view.ViewGroup

interface RewardedAdListener {
    fun onAdLoaded()
    fun onAdFailedToLoad(error: String)
    fun onAdOpened()
    fun onRewardEarned(rewardAmount: Int)
    fun onAdClosed(rewardCompleted: Boolean)
    fun onAdShowFailed(error: String)
}

interface BannerAdListener {
    fun onAdLoaded()
    fun onAdFailedToLoad(error: String)
}

interface AdManager {
    fun initialize(context: Context, appKey: String)
    fun isInitialized(): Boolean
    fun isRewardedAdReady(): Boolean
    fun requestRewardedVideo(zoneId: String, listener: RewardedAdListener? = null)
    fun showRewardedVideo(activity: Activity, zoneId: String, listener: RewardedAdListener)
}
