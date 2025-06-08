package com.anhq.smartalarm.core.game

import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.GameDifficulty
import kotlin.random.Random

class MemoryCardGame(private val difficulty: GameDifficulty) : AlarmGame() {
    override val type = AlarmGameType.MEMORY_CARD
    override val title = "Lật thẻ ghi nhớ"
    override val description = "Tìm các cặp thẻ giống nhau để tắt báo thức"

    private val gridSize = when (difficulty) {
        GameDifficulty.EASY -> 2 // 2x2 grid = 4 thẻ (2 cặp)
        GameDifficulty.MEDIUM -> 3 // 3x3 grid = 9 thẻ (4 cặp + 1 thẻ đơn)
        GameDifficulty.HARD -> 4 // 4x4 grid = 16 thẻ (8 cặp)
    }

    private val showDuration = when (difficulty) {
        GameDifficulty.EASY -> 2000L // 2 giây
        GameDifficulty.MEDIUM -> 1500L // 1.5 giây
        GameDifficulty.HARD -> 1000L // 1 giây
    }

    private val totalPairs = (gridSize * gridSize) / 2
    private var matchedPairs = 0
    private var cards = mutableListOf<Card>()
    private var flippedCards = mutableListOf<Card>()

    data class Card(
        val id: Int,
        val pairId: Int,
        var isFlipped: Boolean = false,
        var isMatched: Boolean = false
    )

    var onCardFlip: ((Int) -> Unit)? = null
    var onPairMatched: (() -> Unit)? = null
    var onPairMismatched: (() -> Unit)? = null

    init {
        generateCards()
    }

    private fun generateCards() {
        cards.clear()
        flippedCards.clear()
        matchedPairs = 0

        // Tạo các cặp thẻ
        val pairs = (0 until totalPairs).flatMap { pairId ->
            List(2) { Card(cards.size + it, pairId) }
        }.toMutableList()

        // Nếu số ô lẻ, thêm một thẻ đơn
        if (gridSize * gridSize > pairs.size) {
            pairs.add(Card(pairs.size, totalPairs))
        }

        // Xáo trộn thẻ
        cards = pairs.shuffled().toMutableList()
    }

    fun flipCard(position: Int): Boolean {
        if (position !in cards.indices || 
            cards[position].isFlipped || 
            cards[position].isMatched || 
            flippedCards.size >= 2) {
            return false
        }

        // Lật thẻ
        cards[position].isFlipped = true
        flippedCards.add(cards[position])
        onCardFlip?.invoke(position)

        // Kiểm tra khi đã lật 2 thẻ
        if (flippedCards.size == 2) {
            if (checkMatch()) {
                matchedPairs++
                flippedCards.forEach { it.isMatched = true }
                onPairMatched?.invoke()
                
                // Kiểm tra chiến thắng
                if (matchedPairs == totalPairs) {
                    completeGame()
                }
            } else {
                onPairMismatched?.invoke()
                // Úp lại các thẻ không khớp sau một khoảng thời gian
                flippedCards.forEach { it.isFlipped = false }
            }
            flippedCards.clear()
        }

        return true
    }

    private fun checkMatch(): Boolean {
        if (flippedCards.size != 2) return false
        return flippedCards[0].pairId == flippedCards[1].pairId
    }

    override fun reset() {
        isCompleted = false
        generateCards()
    }

    // Getter methods for game state
    fun getCards(): List<Card> = cards
    fun getGridSize() = gridSize
    fun getShowDuration() = showDuration
    fun getMatchedPairs() = matchedPairs
    fun getTotalPairs() = totalPairs
} 