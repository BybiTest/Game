package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserEntity
import com.example.monetization.BazaarBillingManager
import com.example.ui.AdStatusState
import com.example.ui.components.TapsellStandardBanner

@Composable
fun StoreScreen(
    user: UserEntity?,
    adStatus: AdStatusState,
    onWatchAd: (Activity) -> Unit,
    onDismissAdStatus: () -> Unit,
    onPurchaseBazaarSku: (Activity, String, Boolean) -> Unit = { _, _, _ -> },
    onPurchaseVipPlan: (Activity, String, Int) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // VIP Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFFB300), Color(0xFFFF8F00), Color(0xFFF57C00))
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (user?.isVip == true) "عضو طلایی VIP (فعال)" else "فروشگاه و خرید اشتراک",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = "${user?.coins ?: 0} سکه",
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // CAFE BAZAAR IN-APP BILLING SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "پرداخت درون‌برنامه‌ای کافه‌بازار:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Text(
                    text = "Bazaar In-App Billing",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bazaar Products List
        val bazaarProducts = listOf(
            Triple(BazaarBillingManager.SKU_VIP_MONTHLY, "اشتراک ۱ ماهه VIP کافه‌بازار", "۳۹,۰۰۰ تومان"),
            Triple(BazaarBillingManager.SKU_VIP_SEASONAL, "اشتراک ۳ ماهه VIP (پیشنهاد ویژه)", "۸۹,۰۰۰ تومان"),
            Triple(BazaarBillingManager.SKU_VIP_LIFETIME, "اشتراک دائمی طلایی VIP", "۱۷۹,۰۰۰ تومان"),
            Triple(BazaarBillingManager.SKU_COINS_PACK_500, "بسته ۵۰۰ سکه طلایی", "۱۹,۰۰۰ تومان")
        )

        bazaarProducts.forEachIndexed { index, (sku, title, priceLabel) ->
            val isSub = sku.startsWith("vip_")
            val isFeatured = index == 1

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    if (isFeatured) 1.5.dp else 1.dp,
                    if (isFeatured) Color(0xFF22C55E) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = priceLabel,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF16A34A),
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (activity != null) {
                                onPurchaseBazaarSku(activity, sku, isSub)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFeatured) Color(0xFF16A34A) else Color(0xFF0F766E)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("خرید با کافه‌بازار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // REAL TAPSELL REWARDED VIDEO SECTION
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF86EFAC)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مشاهده ویدیوی جایزه‌دار تپسل",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            text = "با تماشای کامل ویدیو، ۵۰ سکه رایگان دریافت کنید!",
                            fontSize = 12.sp,
                            color = Color(0xFF166534)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (activity != null) {
                            onWatchAd(activity)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تماشای ویدیو (+۵۰ سکه واقعی)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // REAL TAPSELL STANDARD BANNER IN STORE
        Text(
            text = "تبلیغات استاندارد تپسل:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )
        TapsellStandardBanner()

        // Ad Status Dialog
        if (adStatus.isWatching) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("درگاه پرداخت و تبلیغات") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(adStatus.statusMessage)
                    }
                },
                confirmButton = {}
            )
        }

        adStatus.error?.let { err ->
            AlertDialog(
                onDismissRequest = onDismissAdStatus,
                title = { Text("وضعیت عملیات") },
                text = { Text(err) },
                confirmButton = {
                    Button(onClick = onDismissAdStatus) {
                        Text("متوجه شدم")
                    }
                }
            )
        }
    }
}
