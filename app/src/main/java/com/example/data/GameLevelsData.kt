package com.example.data

data class WordLevel(
    val id: Int,
    val title: String,
    val theme: String,
    val letters: List<Char>,
    val targetWords: List<String>,
    val bonusWords: List<String> = emptyList(),
    val coinReward: Int = 25
)

data class CrosswordCell(
    val row: Int,
    val col: Int,
    val correctChar: Char,
    val clueNumber: Int? = null
)

data class CrosswordClue(
    val number: Int,
    val isAcross: Boolean,
    val clueText: String,
    val answer: String,
    val startRow: Int,
    val startCol: Int
)

data class CrosswordLevel(
    val id: Int,
    val title: String,
    val rows: Int,
    val cols: Int,
    val clues: List<CrosswordClue>,
    val cells: List<CrosswordCell>,
    val coinReward: Int = 40
)

object GameLevelsData {
    val wordLevels = listOf(
        WordLevel(
            id = 1,
            title = "مرحله ۱",
            theme = "طبیعت",
            letters = listOf('س', 'ب', 'ز', 'د'),
            targetWords = listOf("سبز", "سبد", "دست"),
            bonusWords = listOf("زد"),
            coinReward = 20
        ),
        WordLevel(
            id = 2,
            title = "مرحله ۲",
            theme = "آسمان",
            letters = listOf('م', 'ا', 'ه', 'ر'),
            targetWords = listOf("ماه", "مهر", "راه"),
            bonusWords = listOf("مار"),
            coinReward = 25
        ),
        WordLevel(
            id = 3,
            title = "مرحله ۳",
            theme = "دریا",
            letters = listOf('م', 'ا', 'ه', 'ی'),
            targetWords = listOf("ماهی", "ماه", "هم"),
            bonusWords = listOf("می"),
            coinReward = 25
        ),
        WordLevel(
            id = 4,
            title = "مرحله ۴",
            theme = "باغبان",
            letters = listOf('گ', 'ل', 'د', 'ا', 'ن'),
            targetWords = listOf("گلدان", "گل", "لگد", "لگن"),
            bonusWords = listOf("گند"),
            coinReward = 30
        )
    )

    val crosswordLevels = listOf(
        CrosswordLevel(
            id = 1,
            title = "جدول مرحله ۱",
            rows = 3,
            cols = 3,
            clues = listOf(
                CrosswordClue(1, true, "شب در آسمان می‌درخشد", "ماه", 0, 0),
                CrosswordClue(2, false, "وسیله ماهیگیری", "تور", 0, 2)
            ),
            cells = listOf(
                CrosswordCell(0, 0, 'م', 1),
                CrosswordCell(0, 1, 'ا'),
                CrosswordCell(0, 2, 'ه'),
                CrosswordCell(1, 2, 'و'),
                CrosswordCell(2, 2, 'ر', 2)
            ),
            coinReward = 45
        )
    )
}
