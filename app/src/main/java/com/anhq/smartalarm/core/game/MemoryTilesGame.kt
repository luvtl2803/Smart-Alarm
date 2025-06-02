package com.anhq.smartalarm.core.game

import com.anhq.smartalarm.core.model.AlarmGameType
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MemoryTilesGame : AlarmGame() {
    override val type = AlarmGameType.MEMORY_TILES
    override val title = "Memory Tiles"
    override val description = "Remember and repeat the pattern"

    private val gridSize = 3 // 3x3 grid
    private val sequenceLength = 3 // Reduced from 4 to 3 for easier gameplay
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
                delay(800) // Hiển thị trong 800ms
                
                // Ẩn ô đi
                onSequenceShow?.invoke(emptyList())
                delay(400) // Đợi 400ms trước khi hiển thị ô tiếp theo
            }

            // Hiển thị toàn bộ chuỗi một lần cuối
            onSequenceShow?.invoke(sequence)
            delay(1500) // Hiển thị toàn bộ chuỗi trong 1.5 giây
            
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