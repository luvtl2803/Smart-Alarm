package com.anhq.smartalarm.core.game

import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.GameDifficulty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MemoryTilesGame(difficulty: GameDifficulty) : AlarmGame() {
    override val type = AlarmGameType.MEMORY_TILES
    override val title = "Memory Tiles"
    override val description = "Remember and repeat the pattern"
    private val gridSize = 3 // 3x3 grid
    
    private val sequenceLength = when (difficulty) {
        GameDifficulty.EASY -> 3    // 3 ô để ghi nhớ
        GameDifficulty.MEDIUM -> 4  // 4 ô để ghi nhớ
        GameDifficulty.HARD -> 5    // 5 ô để ghi nhớ
    }
    
    private val showDuration = when (difficulty) {
        GameDifficulty.EASY -> 1000L    // 1 giây
        GameDifficulty.MEDIUM -> 800L   // 0.8 giây
        GameDifficulty.HARD -> 500L     // 0.5 giây
    }
    
    private val pauseDuration = when (difficulty) {
        GameDifficulty.EASY -> 500L     // 0.5 giây
        GameDifficulty.MEDIUM -> 400L   // 0.4 giây
        GameDifficulty.HARD -> 300L     // 0.3 giây
    }
    
    private val finalShowDuration = when (difficulty) {
        GameDifficulty.EASY -> 2000L    // 2 giây
        GameDifficulty.MEDIUM -> 1500L  // 1.5 giây
        GameDifficulty.HARD -> 1000L    // 1 giây
    }

    private var sequence = mutableListOf<Int>() // Chuỗi cần ghi nhớ
    private var playerSequence = mutableListOf<Int>() // Chuỗi người chơi đã chọn

    var onSequenceShow: ((List<Int>) -> Unit)? = null // Callback để hiển thị chuỗi
    var onSequenceComplete: (() -> Unit)? = null // Callback khi hoàn thành hiển thị chuỗi
    var onWrongTile: ((Int) -> Unit)? = null // Callback khi chọn sai, truyền vào index của ô sai
    var onResetProgress: (() -> Unit)? = null // Callback khi cần reset tiến trình người chơi

    init {
        generateNewSequence()
    }

    fun showSequence() {
        CoroutineScope(Dispatchers.Main).launch {
            // Bắt đầu với màn hình trống
            onSequenceShow?.invoke(emptyList())
            delay(500) // Đợi một chút trước khi bắt đầu

            // Hiển thị từng ô một
            for (tile in sequence) {
                // Hiển thị ô hiện tại
                onSequenceShow?.invoke(listOf(tile))
                delay(showDuration)

                // Ẩn ô đi
                onSequenceShow?.invoke(emptyList())
                delay(pauseDuration)
            }

            // Hiển thị toàn bộ chuỗi một lần cuối
            onSequenceShow?.invoke(sequence)
            delay(finalShowDuration)

            // Kết thúc hiển thị
            onSequenceShow?.invoke(emptyList())
            onSequenceComplete?.invoke()
        }
    }

    fun selectTile(tileIndex: Int) {
        if (isCompleted) return

        playerSequence.add(tileIndex)

        // Kiểm tra xem người chơi đã chọn đúng không
        val currentIndex = playerSequence.size - 1
        if (playerSequence[currentIndex] != sequence[currentIndex]) {
            onWrongTile?.invoke(tileIndex)
            // Chỉ xóa tiến trình hiện tại, không tạo chuỗi mới
            playerSequence.clear()
            onResetProgress?.invoke()
            return
        }

        // Nếu đã chọn đủ số lượng và tất cả đều đúng
        if (playerSequence.size == sequence.size) {
            completeGame()
        }
    }

    override fun reset() {
        isCompleted = false
        playerSequence.clear()
        // Do NOT generate new sequence here to keep the same pattern on reset
    }

    fun startNewGame() {
        isCompleted = false
        playerSequence.clear()
        generateNewSequence()
    }

    private fun generateNewSequence() {
        sequence.clear()
        repeat(sequenceLength) {
            sequence.add(Random.nextInt(gridSize * gridSize))
        }
    }

    // Lấy kích thước lưới
    fun getGridSize() = gridSize

    // Lấy số ô đã chọn đúng
    fun getCorrectTiles() = playerSequence.size

    // Lấy tổng số ô cần chọn
    fun getTotalTiles() = sequence.size
}
