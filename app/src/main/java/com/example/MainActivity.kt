package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.monetization.BazaarBillingManager
import com.example.monetization.BazaarPurchaseResult
import com.example.monetization.TapsellAdManager
import com.example.ui.GameViewModel
import com.example.ui.screens.CrosswordGameScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.screens.WordGameScreen
import com.example.ui.theme.GameThemes
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var activeViewModel: GameViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Tapsell Plus SDK
        try {
            TapsellAdManager.getInstance().initialize(
                applicationContext,
                BuildConfig.TAPSELL_APP_KEY
            )
            Log.i("MainActivity", "Tapsell initialized with App Key.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Tapsell init error", e)
        }

        // 2. Connect to Cafe Bazaar In-App Billing Service
        try {
            BazaarBillingManager.getInstance().connect(applicationContext) { connected ->
                Log.i("MainActivity", "Cafe Bazaar Billing connected: $connected")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error connecting to Cafe Bazaar Billing", e)
        }

        // 3. Database and ViewModel
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = GameRepository(database.gameDao())
        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GameViewModel(repository) as T
            }
        }

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val gameViewModel: GameViewModel = viewModel(factory = viewModelFactory)
                    activeViewModel = gameViewModel
                    MainGameApp(viewModel = gameViewModel, activity = this@MainActivity)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val handled = BazaarBillingManager.getInstance().handleActivityResult(
            requestCode,
            resultCode,
            data
        ) { result ->
            when (result) {
                is BazaarPurchaseResult.Success -> {
                    activeViewModel?.onBazaarPurchaseSuccess(
                        sku = result.sku,
                        purchaseToken = result.purchaseToken,
                        isSubscription = result.isSubscription,
                        activity = this@MainActivity
                    )
                }
                is BazaarPurchaseResult.Canceled -> {
                    activeViewModel?.onBazaarPurchaseError("عملیات پرداخت در کافه‌بازار لغو شد.")
                }
                is BazaarPurchaseResult.Error -> {
                    activeViewModel?.onBazaarPurchaseError(result.message)
                }
            }
        }

        if (!handled) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BazaarBillingManager.getInstance().disconnect(applicationContext)
    }
}

@Composable
fun MainGameApp(
    viewModel: GameViewModel,
    activity: Activity
) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val wordState by viewModel.wordState.collectAsStateWithLifecycle()
    val crosswordState by viewModel.crosswordState.collectAsStateWithLifecycle()
    val adStatus by viewModel.adStatus.collectAsStateWithLifecycle()

    val currentTheme = GameThemes.TURQUOISE
    var currentTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(containerColor = currentTheme.cardBackground) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Extension, contentDescription = "بازی کلمات") },
                    label = { Text("کلمات") }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.GridOn, contentDescription = "جدول") },
                    label = { Text("جدول") }
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "فروشگاه و ویدیو") },
                    label = { Text("فروشگاه و بازار") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> WordGameScreen(
                    state = wordState,
                    user = user,
                    theme = currentTheme,
                    onSelectLetter = { viewModel.selectLetter(it) },
                    onRemoveLastLetter = { viewModel.removeLastLetter() },
                    onClearLetters = { viewModel.clearLetters() },
                    onSubmitWord = { viewModel.submitWord() },
                    onShuffleLetters = { viewModel.shuffleLetters() },
                    onNextLevel = { viewModel.nextWordLevel() },
                    onDismissWinDialog = { viewModel.dismissWinDialog() }
                )
                1 -> CrosswordGameScreen(
                    state = crosswordState,
                    user = user,
                    theme = currentTheme,
                    onSelectCell = { r, c -> viewModel.selectCrosswordCell(r, c) },
                    onInputChar = { viewModel.inputCrosswordChar(it) },
                    onClearCell = { viewModel.clearCrosswordCell() },
                    onNextLevel = { viewModel.nextCrosswordLevel() },
                    onDismissWinDialog = { viewModel.dismissCrosswordWinDialog() }
                )
                2 -> StoreScreen(
                    user = user,
                    adStatus = adStatus,
                    onWatchAd = { act -> viewModel.watchTapsellRewardedAd(act) },
                    onDismissAdStatus = { viewModel.dismissAdStatus() },
                    onPurchaseBazaarSku = { act, sku, isSub -> viewModel.buyBazaarSku(act, sku, isSub) },
                    onPurchaseVipPlan = { act, title, amt -> viewModel.startVipPurchase(act, title, amt) }
                )
            }
        }
    }
}
