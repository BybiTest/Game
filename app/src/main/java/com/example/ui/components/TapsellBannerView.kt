package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.monetization.BannerAdListener
import com.example.monetization.TapsellAdManager

@Composable
fun TapsellStandardBanner(
    modifier: Modifier = Modifier,
    zoneId: String = TapsellAdManager.getInstance().getBannerZoneId()
) {
    val context = LocalContext.current
    var isLoaded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        val activity = context as? Activity
        if (activity != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                factory = { ctx ->
                    val frameLayout = FrameLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    TapsellAdManager.getInstance().requestStandardBanner(
                        activity = activity,
                        container = frameLayout,
                        zoneId = zoneId,
                        listener = object : BannerAdListener {
                            override fun onAdLoaded() {
                                isLoaded = true
                                errorMsg = null
                            }

                            override fun onAdFailedToLoad(error: String) {
                                isLoaded = false
                                errorMsg = error
                                Log.w("TapsellBanner", "Banner load failed: $error")
                            }
                        }
                    )
                    frameLayout
                }
            )
        }

        if (!isLoaded) {
            Text(
                text = errorMsg ?: "در حال بارگذاری تبلیغ تپسل...",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
