package com.almog.moonboard.snake

import com.almog.moonboard.model.BOARD_COLUMNS
import com.almog.moonboard.model.BOARD_ROWS
import com.almog.moonboard.model.GridPosition

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, 1), DOWN(0, -1), LEFT(-1, 0), RIGHT(1, 0);

    fun isOpposite(other: Direction) = dx == -other.dx && dy == -other.dy
}

sealed class StepResult {
    data class Moved(val snake: List<GridPosition>, val ateFood: Boolean) : StepResult()
    data object Died : StepResult()
}

val INITIAL_SNAKE = listOf(GridPosition(6, 9), GridPosition(6, 8))
val INITIAL_DIRECTION = Direction.UP

// Pure snake-movement rules over the board; no BLE, no Compose, no coroutines.
object SnakeEngine {
    fun step(snake: List<GridPosition>, direction: Direction, food: GridPosition): StepResult {
        val head = snake.first()
        val nextColumn = head.column + direction.dx
        val nextRow = head.row + direction.dy
        if (nextColumn !in 1..BOARD_COLUMNS || nextRow !in 1..BOARD_ROWS) return StepResult.Died

        val nextHead = GridPosition(nextColumn, nextRow)
        val ateFood = nextHead == food
        val body = if (ateFood) snake else snake.dropLast(1)
        if (nextHead in body) return StepResult.Died

        return StepResult.Moved(listOf(nextHead) + body, ateFood)
    }

    fun randomFood(occupied: List<GridPosition>): GridPosition {
        val occupiedSet = occupied.toSet()
        while (true) {
            val candidate = GridPosition((1..BOARD_COLUMNS).random(), (1..BOARD_ROWS).random())
            if (candidate !in occupiedSet) return candidate
        }
    }
}
