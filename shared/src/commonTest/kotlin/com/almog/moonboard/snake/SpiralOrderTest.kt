package com.almog.moonboard.snake

import com.almog.moonboard.model.BOARD_COLUMNS
import com.almog.moonboard.model.BOARD_ROWS
import kotlin.test.Test
import kotlin.test.assertEquals

class SpiralOrderTest {
    @Test
    fun forGrid_visitsEveryPositionExactlyOnce() {
        val order = SpiralOrder.forGrid(BOARD_COLUMNS, BOARD_ROWS)
        assertEquals(BOARD_COLUMNS * BOARD_ROWS, order.size)
        assertEquals(order.size, order.toSet().size)
    }
}
