package com.anhq.smartalarm.core.game

import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.GameDifficulty
import kotlin.random.Random

class MathProblemGame(private val difficulty: GameDifficulty) : AlarmGame() {
    override val type = AlarmGameType.MATH_PROBLEM
    override val title = "Math Problem"
    override val description = "Solve the math problem to stop the alarm"

    private var firstNumber: Int = 0
    private var secondNumber: Int = 0
    private var operation: MathOperation = MathOperation.ADDITION
    private var correctAnswer: Int = 0

    enum class MathOperation(val symbol: String) {
        ADDITION("+"),
        SUBTRACTION("-"),
        MULTIPLICATION("×")
    }

    init {
        generateNewProblem()
    }

    fun getProblem(): String {
        return "$firstNumber ${operation.symbol} $secondNumber = ?"
    }

    fun checkAnswer(answer: Int): Boolean {
        if (answer == correctAnswer) {
            completeGame()
            return true
        }
        return false
    }

    override fun reset() {
        isCompleted = false
        generateNewProblem()
    }

    private fun generateNewProblem() {
        // Chọn phép tính dựa trên độ khó
        operation = when (difficulty) {
            GameDifficulty.EASY -> MathOperation.entries[Random.nextInt(2)] // Chỉ + hoặc -
            GameDifficulty.MEDIUM -> MathOperation.entries.random() // Tất cả các phép tính
            GameDifficulty.HARD -> MathOperation.entries.random() // Tất cả nhưng số lớn hơn
        }

        when (operation) {
            MathOperation.ADDITION -> {
                firstNumber = generateNumberForAddition()
                secondNumber = generateNumberForAddition()
                correctAnswer = firstNumber + secondNumber
            }
            MathOperation.SUBTRACTION -> {
                // Đảm bảo kết quả luôn dương
                firstNumber = generateNumberForSubtraction()
                secondNumber = Random.nextInt(1, firstNumber)
                correctAnswer = firstNumber - secondNumber
            }
            MathOperation.MULTIPLICATION -> {
                firstNumber = generateNumberForMultiplication()
                secondNumber = generateNumberForMultiplication()
                correctAnswer = firstNumber * secondNumber
            }
        }
    }

    private fun generateNumberForAddition(): Int = when (difficulty) {
        GameDifficulty.EASY -> Random.nextInt(1, 10)     // 1-9
        GameDifficulty.MEDIUM -> Random.nextInt(10, 50)  // 10-49
        GameDifficulty.HARD -> Random.nextInt(50, 100)   // 50-99
    }

    private fun generateNumberForSubtraction(): Int = when (difficulty) {
        GameDifficulty.EASY -> Random.nextInt(5, 20)     // 5-19
        GameDifficulty.MEDIUM -> Random.nextInt(20, 50)  // 20-49
        GameDifficulty.HARD -> Random.nextInt(50, 100)   // 50-99
    }

    private fun generateNumberForMultiplication(): Int = when (difficulty) {
        GameDifficulty.EASY -> Random.nextInt(2, 5)      // 2-4
        GameDifficulty.MEDIUM -> Random.nextInt(5, 10)   // 5-9
        GameDifficulty.HARD -> Random.nextInt(10, 15)    // 10-14
    }
}
