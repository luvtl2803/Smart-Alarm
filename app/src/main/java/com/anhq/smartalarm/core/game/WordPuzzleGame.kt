package com.anhq.smartalarm.core.game

import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.GameDifficulty

class WordPuzzleGame(private val difficulty: GameDifficulty) : AlarmGame() {
    override val type = AlarmGameType.WORD_PUZZLE
    override val title = "Giải câu đố chữ"
    override val description = "Giải mã từ bị xáo trộn để tắt báo thức"

    private var currentWord = ""
    private var scrambledWord = ""
    private var attemptsLeft = when (difficulty) {
        GameDifficulty.EASY -> 5
        GameDifficulty.MEDIUM -> 4
        GameDifficulty.HARD -> 3
    }
    private val maxAttempts = attemptsLeft

    private val easyWords = listOf(
        "mèo", "chó", "gà", "vịt", "heo",
        "bàn", "ghế", "cửa", "nhà", "xe",
        "cây", "hoa", "lá", "quả", "rau"
    )

    private val mediumWords = listOf(
        "bánh mì", "cà phê", "nước mắm",
        "bóng đá", "võ thuật", "âm nhạc",
        "máy tính", "điện thoại", "internet",
        "trường học", "bệnh viện", "công viên"
    )

    private val hardWords = listOf(
        "kỹ thuật số", "trí tuệ nhân tạo",
        "phát triển bền vững", "năng lượng tái tạo",
        "biến đổi khí hậu", "công nghệ thông tin",
        "kinh tế số", "thương mại điện tử"
    )

    init {
        generateNewWord()
    }

    private fun generateNewWord() {
        val wordList = when (difficulty) {
            GameDifficulty.EASY -> easyWords
            GameDifficulty.MEDIUM -> mediumWords
            GameDifficulty.HARD -> hardWords
        }
        currentWord = wordList.random()
        scrambledWord = scrambleWord(currentWord)
        attemptsLeft = maxAttempts
    }

    private fun scrambleWord(word: String): String {
        val parts = word.split(" ")
        return parts.joinToString(" ") { part ->
            part.toCharArray()
                .toMutableList()
                .apply { shuffle() }
                .joinToString("")
        }
    }

    fun checkAnswer(answer: String): Boolean {
        if (answer.trim().equals(currentWord.trim(), ignoreCase = true)) {
            completeGame()
            return true
        }
        attemptsLeft--
        return false
    }

    fun getCurrentScrambledWord(): String = scrambledWord

    fun getAttemptsLeft(): Int = attemptsLeft

    fun getMaxAttempts(): Int = maxAttempts

    fun getHint(): String {
        return currentWord.split(" ").joinToString(" ") { word ->
            word.first() + "..." + (if (word.length > 1) word.last() else "")
        }
    }

    override fun reset() {
        isCompleted = false
        generateNewWord()
    }
} 