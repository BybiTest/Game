package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class GameTheme(
    val id: String,
    val name: String,
    val subtitle: String,
    val icon: String,
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val accent: Color,
    val backgroundGradient: Brush,
    val cardBackground: Color,
    val cardBorder: Color,
    val textColor: Color,
    val textSecondary: Color,
    val letterWheelCenter: Color,
    val letterButtonGradient: List<Color>,
    val isDark: Boolean = false
)

object GameThemes {
    val TURQUOISE = GameTheme(
        id = "turquoise",
        name = "فیروزه‌ای",
        subtitle = "کلاسیک ایرانی",
        icon = "💠",
        primary = Color(0xFF00838F),
        primaryVariant = Color(0xFF00ACC1),
        secondary = Color(0xFF004D40),
        accent = Color(0xFFFFB300),
        backgroundGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFFE8F5E9))
        ),
        cardBackground = Color(0xFFFFFFFF),
        cardBorder = Color(0xFF80DEEA),
        textColor = Color(0xFF004D40),
        textSecondary = Color(0xFF00695C),
        letterWheelCenter = Color(0xFFE0F2F1),
        letterButtonGradient = listOf(Color(0xFF0097A7), Color(0xFF00838F))
    )

    val DESERT = GameTheme(
        id = "desert",
        name = "کویر طلایی",
        subtitle = "گرم و دلنشین",
        icon = "🏜️",
        primary = Color(0xFFD84315),
        primaryVariant = Color(0xFFEF6C00),
        secondary = Color(0xFFFF8F00),
        accent = Color(0xFFFFB300),
        backgroundGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFF8E1), Color(0xFFFFE0B2), Color(0xFFFFCCBC))
        ),
        cardBackground = Color(0xFFFFFFFF),
        cardBorder = Color(0xFFFFCC80),
        textColor = Color(0xFF4E342E),
        textSecondary = Color(0xFF8D6E63),
        letterWheelCenter = Color(0xFFFFF3E0),
        letterButtonGradient = listOf(Color(0xFFE64A19), Color(0xFFD84315))
    )

    val allThemes = listOf(TURQUOISE, DESERT)

    fun getThemeById(id: String?): GameTheme {
        return allThemes.find { it.id == id } ?: TURQUOISE
    }
}
