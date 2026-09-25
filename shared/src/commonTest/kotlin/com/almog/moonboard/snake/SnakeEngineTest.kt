package com.almog.moonboard.snake

import com.almog.moonboard.model.GridPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SnakeEngineTest {
    @Test
    fun step_movesForwardWithoutEating() {
        val snake = listOf(GridPosition(5, 5), GridPosition(5, 4))
        val result = SnakeEngine.step(snake, Direction.UP, food = GridPosition(1, 1))
        val moved = assertIs<StepResult.Moved>(result)
        assertEquals(listOf(GridPosition(5, 6), GridPosition(5, 5)), moved.snake)
        assertEquals(false, moved.ateFood)
    }

    @Test
    fun step_growsAndScoresOnFood() {
        val snake = listOf(GridPosition(5, 5), GridPosition(5, 4))
        val result = SnakeEngine.step(snake, Direction.UP, food = GridPosition(5, 6))
        val moved = assertIs<StepResult.Moved>(result)
        assertEquals(listOf(GridPosition(5, 6), GridPosition(5, 5), GridPosition(5, 4)), moved.snake)
        assertEquals(true, moved.ateFood)
    }

    @Test
    fun step_dies_onEachWallDirection() {
        assertIs<StepResult.Died>(SnakeEngine.step(listOf(GridPosition(11, 9), GridPosition(10, 9)), Direction.RIGHT, GridPosition(1, 1)))
        assertIs<StepResult.Died>(SnakeEngine.step(listOf(GridPosition(1, 9), GridPosition(2, 9)), Direction.LEFT, GridPosition(1, 1)))
        assertIs<StepResult.Died>(SnakeEngine.step(listOf(GridPosition(6, 18), GridPosition(6, 17)), Direction.UP, GridPosition(1, 1)))
        assertIs<StepResult.Died>(SnakeEngine.step(listOf(GridPosition(6, 1), GridPosition(6, 2)), Direction.DOWN, GridPosition(1, 1)))
    }

    @Test
    fun step_dies_onSelfCollisionWithNonTailSegment() {
        // A short loop where moving RIGHT lands on (6,5) - a body segment, not the tail (5,6).
        val snake = listOf(GridPosition(5, 5), GridPosition(6, 5), GridPosition(6, 6), GridPosition(5, 6))
        assertIs<StepResult.Died>(SnakeEngine.step(snake, Direction.RIGHT, GridPosition(1, 1)))
    }

    @Test
    fun step_doesNotDie_landingOnCurrentTail_whenNotEating() {
        // 4-segment loop: moving DOWN from (5,6) would land on (5,5) - the current tail, about to vacate.
        val snake = listOf(GridPosition(5, 6), GridPosition(6, 6), GridPosition(6, 5), GridPosition(5, 5))
        val result = SnakeEngine.step(snake, Direction.DOWN, food = GridPosition(1, 1))
        assertIs<StepResult.Moved>(result)
    }

    @Test
    fun step_dies_landingOnCurrentTail_whenEating() {
        val snake = listOf(GridPosition(5, 6), GridPosition(6, 6), GridPosition(6, 5), GridPosition(5, 5))
        val result = SnakeEngine.step(snake, Direction.DOWN, food = GridPosition(5, 5))
        assertIs<StepResult.Died>(result)
    }

    @Test
    fun randomFood_neverLandsOnOccupiedCell() {
        val occupied = listOf(GridPosition(1, 1), GridPosition(1, 2), GridPosition(1, 3))
        repeat(50) {
            assertEquals(false, SnakeEngine.randomFood(occupied) in occupied)
        }
    }
}
