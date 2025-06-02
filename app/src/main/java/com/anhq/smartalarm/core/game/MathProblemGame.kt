package com.anhq.smartalarm.core.game

import com.anhq.smartalarm.core.model.AlarmGameType
import kotlin.random.Random

class MathProblemGame : AlarmGame() {
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
        operation = MathOperation.entries.toTypedArray()[Random.nextInt(MathOperation.entries.size)]
        
        when (operation) {
            MathOperation.ADDITION -> {
                firstNumber = Random.nextInt(1, 50)
                secondNumber = Random.nextInt(1, 50)
                correctAnswer = firstNumber + secondNumber
            }
            MathOperation.SUBTRACTION -> {
                firstNumber = Random.nextInt(10, 100)
                secondNumber = Random.nextInt(1, firstNumber)
                correctAnswer = firstNumber - secondNumber
            }
            MathOperation.MULTIPLICATION -> {
                firstNumber = Random.nextInt(2, 12)
                secondNumber = Random.nextInt(2, 12)
                correctAnswer = firstNumber * secondNumber
            }
        }
    }
} 