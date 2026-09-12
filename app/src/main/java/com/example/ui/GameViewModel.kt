package com.example.ui

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CrosswordLevel
import com.example.data.GameLevelsData
import com.example.data.GameRepository
import com.example.data.LevelProgressEntity
import com.example.data.UserEntity
import com.example.data.WordLevel
import com.example.monetization.BazaarBillingManager
import com.example.monetization.BazaarPurchaseResult
import com.example.monetization.PaymentResult
import com.example.monetization.RewardedAdListener
import com.example.monetization.TapsellAdManager
import com.example.monetization.ZarinPalPaymentManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WordGameState(
    val currentLevelIndex: Int = 0,
    val selectedLetters: List<Char> = emptyList(),
    val shuffledLetters: List<Char> = emptyList(),
    val foundWords: Set<String> = emptySet(),
    val bonusWordsFound: Set<String> = emptySet(),
    val feedbackMessage: String? = null,
    val isLevelCompleted: Boolean = false,
    val showWinDialog: Boolean = false
)

data class CrosswordGameState(
    val currentLevelIndex: Int = 0,
    val enteredGrid: Map<Pair<Int, Int>, Char> = emptyMap(),
    val selectedCell: Pair<Int, Int>? = null,
    val isCompleted: Boolean = false,
    val showWinDialog: Boolean = false
)

data class AdStatusState(
    val isWatching: Boolean = false,
    val statusMessage: String = "",
    val error: String? = null
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    companion object {
        private const val TAG = "GameViewModel"
        const val REWARD_COINS = 50
    }

    val userProfile: StateFlow<UserEntity?> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserEntity()
        )

    private val _wordState = MutableStateFlow(WordGameState())
    val wordState: StateFlow<WordGameState> = _wordState.asStateFlow()

    private val _crosswordState = MutableStateFlow(CrosswordGameState())
    val crosswordState: StateFlow<CrosswordGameState> = _crosswordState.asStateFlow()

    private val _adStatus = MutableStateFlow(AdStatusState())
    val adStatus: StateFlow<AdStatusState> = _adStatus.asStateFlow()

    private val _showConfetti = MutableStateFlow(false)
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()

    val tapsellAdManager = TapsellAdManager.getInstance()

    init {
        viewModelScope.launch {
            try {
                repository.checkAndInitUser()
            } catch (e: Exception) {
                Log.e(TAG, "Database init error", e)
            }
        }
        initWordLevel(0)
        initCrosswordLevel(0)
    }

    fun initWordLevel(index: Int) {
        val level = GameLevelsData.wordLevels.getOrNull(index) ?: GameLevelsData.wordLevels.first()
        _wordState.value = WordGameState(
            currentLevelIndex = index,
            shuffledLetters = level.letters.shuffled(),
            foundWords = emptySet(),
            bonusWordsFound = emptySet(),
            feedbackMessage = null,
            isLevelCompleted = false,
            showWinDialog = false
        )
    }

    val currentWordLevel: WordLevel
        get() = GameLevelsData.wordLevels.getOrElse(_wordState.value.currentLevelIndex) {
            GameLevelsData.wordLevels.first()
        }

    fun selectLetter(char: Char) {
        val current = _wordState.value.selectedLetters
        _wordState.value = _wordState.value.copy(
            selectedLetters = current + char,
            feedbackMessage = null
        )
    }

    fun removeLastLetter() {
        val current = _wordState.value.selectedLetters
        if (current.isNotEmpty()) {
            _wordState.value = _wordState.value.copy(
                selectedLetters = current.dropLast(1),
                feedbackMessage = null
            )
        }
    }

    fun clearLetters() {
        _wordState.value = _wordState.value.copy(
            selectedLetters = emptyList(),
            feedbackMessage = null
        )
    }

    fun shuffleLetters() {
        _wordState.value = _wordState.value.copy(
            shuffledLetters = currentWordLevel.letters.shuffled()
        )
    }

    fun submitWord() {
        val word = _wordState.value.selectedLetters.joinToString("")
        val level = currentWordLevel
        if (word.isEmpty()) return

        when {
            _wordState.value.foundWords.contains(word) -> {
                _wordState.value = _wordState.value.copy(
                    selectedLetters = emptyList(),
                    feedbackMessage = "این کلمه قبلاً پیدا شده!"
                )
            }
            level.targetWords.contains(word) -> {
                val updatedFound = _wordState.value.foundWords + word
                val isCompleted = updatedFound.containsAll(level.targetWords)
                _wordState.value = _wordState.value.copy(
                    selectedLetters = emptyList(),
                    foundWords = updatedFound,
                    feedbackMessage = "آفرین! کلمه $word درست بود.",
                    isLevelCompleted = isCompleted,
                    showWinDialog = isCompleted
                )
                if (isCompleted) {
                    _showConfetti.value = true
                    viewModelScope.launch {
                        repository.addCoins(level.coinReward)
                        repository.addXp(25)
                        repository.incrementLevelsCompleted()
                        repository.saveProgress(
                            LevelProgressEntity(
                                levelId = level.id,
                                gameType = "WORD_CONNECT",
                                isCompleted = true,
                                stars = 3,
                                foundWords = updatedFound.joinToString(",")
                            )
                        )
                    }
                }
            }
            else -> {
                _wordState.value = _wordState.value.copy(
                    selectedLetters = emptyList(),
                    feedbackMessage = "کلمه $word در این مرحله نیست."
                )
            }
        }
    }

    fun nextWordLevel() {
        val nextIndex = (_wordState.value.currentLevelIndex + 1) % GameLevelsData.wordLevels.size
        initWordLevel(nextIndex)
    }

    fun dismissWinDialog() {
        _wordState.value = _wordState.value.copy(showWinDialog = false)
        _showConfetti.value = false
    }

    // Crossword
    fun initCrosswordLevel(index: Int) {
        val level = GameLevelsData.crosswordLevels.getOrNull(index) ?: GameLevelsData.crosswordLevels.first()
        _crosswordState.value = CrosswordGameState(
            currentLevelIndex = index,
            enteredGrid = emptyMap(),
            selectedCell = level.cells.firstOrNull()?.let { it.row to it.col },
            isCompleted = false,
            showWinDialog = false
        )
    }

    val currentCrosswordLevel: CrosswordLevel
        get() = GameLevelsData.crosswordLevels.getOrElse(_crosswordState.value.currentLevelIndex) {
            GameLevelsData.crosswordLevels.first()
        }

    fun selectCrosswordCell(row: Int, col: Int) {
        val level = currentCrosswordLevel
        if (level.cells.any { it.row == row && it.col == col }) {
            _crosswordState.value = _crosswordState.value.copy(selectedCell = row to col)
        }
    }

    fun inputCrosswordChar(char: Char) {
        val cell = _crosswordState.value.selectedCell ?: return
        val currentGrid = _crosswordState.value.enteredGrid.toMutableMap()
        currentGrid[cell] = char
        val level = currentCrosswordLevel
        val allCorrect = level.cells.all { c -> currentGrid[c.row to c.col] == c.correctChar }

        _crosswordState.value = _crosswordState.value.copy(
            enteredGrid = currentGrid,
            isCompleted = allCorrect,
            showWinDialog = allCorrect
        )

        if (allCorrect) {
            _showConfetti.value = true
            viewModelScope.launch {
                repository.addCoins(level.coinReward)
                repository.addXp(30)
                repository.incrementCrosswordsCompleted()
            }
        }
    }

    fun clearCrosswordCell() {
        val cell = _crosswordState.value.selectedCell ?: return
        val currentGrid = _crosswordState.value.enteredGrid.toMutableMap()
        currentGrid.remove(cell)
        _crosswordState.value = _crosswordState.value.copy(enteredGrid = currentGrid)
    }

    fun nextCrosswordLevel() {
        val nextIndex = (_crosswordState.value.currentLevelIndex + 1) % GameLevelsData.crosswordLevels.size
        initCrosswordLevel(nextIndex)
    }

    fun dismissCrosswordWinDialog() {
        _crosswordState.value = _crosswordState.value.copy(showWinDialog = false)
        _showConfetti.value = false
    }

    // REAL TAPSELL REWARDED VIDEO
    fun watchTapsellRewardedAd(activity: Activity) {
        _adStatus.value = AdStatusState(
            isWatching = true,
            statusMessage = "در حال ارتباط با تپسل و دریافت ویدیو..."
        )

        tapsellAdManager.requestRewardedVideoFromActivity(
            activity = activity,
            zoneId = tapsellAdManager.getRewardedZoneId(),
            listener = object : RewardedAdListener {
                override fun onAdLoaded() {
                    _adStatus.value = AdStatusState(
                        isWatching = true,
                        statusMessage = "تبلیغ آماده شد. در حال نمایش..."
                    )
                    tapsellAdManager.showRewardedVideo(
                        activity = activity,
                        zoneId = tapsellAdManager.getRewardedZoneId(),
                        listener = this
                    )
                }

                override fun onAdFailedToLoad(error: String) {
                    Log.e(TAG, "Tapsell rewarded failed to load: $error")
                    _adStatus.value = AdStatusState(
                        isWatching = false,
                        statusMessage = "",
                        error = "خطا در دریافت تبلیغ تپسل: $error"
                    )
                }

                override fun onAdOpened() {
                    _adStatus.value = AdStatusState(
                        isWatching = true,
                        statusMessage = "در حال پخش تبلیغ جایزه‌ای تپسل..."
                    )
                }

                override fun onRewardEarned(rewardAmount: Int) {
                    Log.i(TAG, "User completed Tapsell rewarded video! Awarding coins.")
                    viewModelScope.launch {
                        repository.recordAdWatched(REWARD_COINS)
                        _showConfetti.value = true
                    }
                    _adStatus.value = AdStatusState(
                        isWatching = false,
                        statusMessage = "پاداش $REWARD_COINS سکه با موفقیت دریافت شد!"
                    )
                }

                override fun onAdClosed(rewardCompleted: Boolean) {
                    _adStatus.value = AdStatusState(isWatching = false)
                }

                override fun onAdShowFailed(error: String) {
                    Log.e(TAG, "Tapsell show failed: $error")
                    _adStatus.value = AdStatusState(
                        isWatching = false,
                        error = "خطا در نمایش ویدیو: $error"
                    )
                }
            }
        )
    }

    fun dismissAdStatus() {
        _adStatus.value = AdStatusState(isWatching = false)
    }

    // ==========================================
    // 1. CAFE BAZAAR IN-APP BILLING METHODS
    // ==========================================
    val bazaarBillingManager = BazaarBillingManager.getInstance()

    fun buyBazaarSku(activity: Activity, sku: String, isSubscription: Boolean = false) {
        _adStatus.value = AdStatusState(
            isWatching = true,
            statusMessage = "در حال اتصال به پرداخت درون‌برنامه‌ای کافه‌بازار..."
        )

        val launched = bazaarBillingManager.launchPurchase(
            activity = activity,
            sku = sku,
            isSubscription = isSubscription
        )

        if (!launched) {
            _adStatus.value = AdStatusState(
                isWatching = false,
                error = "کافه‌بازار بر روی دستگاه شما اجرا نشد. لطفاً نصب بودن کافه‌بازار را بررسی کنید."
            )
        }
    }

    fun onBazaarPurchaseSuccess(
        sku: String,
        purchaseToken: String,
        isSubscription: Boolean,
        activity: Activity
    ) {
        viewModelScope.launch {
            when (sku) {
                BazaarBillingManager.SKU_VIP_MONTHLY -> {
                    repository.activateVip("اشتراک ۱ ماهه بازار")
                    repository.addCoins(100)
                    _showConfetti.value = true
                    _adStatus.value = AdStatusState(
                        isWatching = false,
                        statusMessage = "اشتراک ۱ ماهه VIP کافه‌بازار با موفقیت فعال شد!"
                    )
                }
                BazaarBillingManager.SKU_VIP_SEASONAL -> {
                    repository.activateVip("اشتراک ۳ ماهه بازار")
                    repository.addCoins(300)
                    _showConfetti.value = true
                    _adStatus.value = AdStatusState(
                        isWatching = false,
                        statusMessage = "اشتراک ۳ ماهه VIP کافه‌بازار با موفقیت فعال شد!"
                    )
                }
                BazaarBillingManager.SKU_VIP_LIFETIME -> {
                    repository.activateVip("اشتراک دائمی بازار")
                    repository.addCoins(1000)
                    _showConfetti.value = true
                    _adStatus.value = AdStatusState(
                        isWatching = false,
                        statusMessage = "اشتراک طلایی دائمی VIP کافه‌بازار فعال شد!"
                    )
                }
                BazaarBillingManager.SKU_COINS_PACK_500 -> {
                    repository.addCoins(500)
                    _showConfetti.value = true
                    bazaarBillingManager.consumePurchase(activity.applicationContext, purchaseToken)
                    _adStatus.value = AdStatusState(
                        isWatching = false,
                        statusMessage = "۵۰۰ سکه طلایی با موفقیت به حسابتان اضافه شد!"
                    )
                }
                else -> {
                    repository.activateVip("VIP کافه‌بازار")
                    _showConfetti.value = true
                    _adStatus.value = AdStatusState(
                        isWatching = false,
                        statusMessage = "خرید شما با موفقیت ثبت و فعال شد!"
                    )
                }
            }
        }
    }

    fun onBazaarPurchaseError(errorMessage: String) {
        _adStatus.value = AdStatusState(
            isWatching = false,
            error = errorMessage
        )
    }

    // ==========================================
    // 2. REAL ZARINPAL SHAPARAK PAYMENT FLOW
    // ==========================================
    val zarinPalManager = ZarinPalPaymentManager.getInstance()

    fun startVipPurchase(activity: Activity, planTitle: String, amountTomans: Int) {
        _adStatus.value = AdStatusState(
            isWatching = true,
            statusMessage = "در حال اتصال به درگاه پرداخت شاپرک..."
        )

        viewModelScope.launch {
            val result = zarinPalManager.requestPayment(
                context = activity,
                amountTomans = amountTomans,
                planTitle = planTitle
            )

            when (result) {
                is PaymentResult.RedirectToGateway -> {
                    _adStatus.value = AdStatusState(isWatching = false)
                    zarinPalManager.openGatewayUrl(activity, result.paymentUrl)
                }
                is PaymentResult.Failure -> {
                    _adStatus.value = AdStatusState(
                        isWatching = false,
                        error = result.errorMessage
                    )
                }
                is PaymentResult.Success -> {
                    repository.activateVip(result.planName)
                    repository.addCoins(200)
                    _showConfetti.value = true
                }
            }
        }
    }
}
