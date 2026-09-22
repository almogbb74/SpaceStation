package com.almog.moonboard.ble

import com.almog.moonboard.model.BOARD_ROWS
import com.almog.moonboard.model.GridPosition

/**
 * Converts a board grid position into the LED strip index (1..198) the MoonBoard box expects.
 * The strip runs column-major serpentine: column 1 bottom-to-top, column 2 top-to-bottom, etc.
 *
 * ponytail: the starting direction (COLUMN_ONE_GOES_UP) is inferred from reverse-engineered
 * community code, not confirmed against real hardware. If holds light up vertically mirrored,
 * flip this one flag rather than rewriting the mapping.
 */
object LedMapper {
    private const val COLUMN_ONE_GOES_UP = true

    fun ledIndex(position: GridPosition): Int {
        val columnGoesUp = if (COLUMN_ONE_GOES_UP) position.column % 2 == 1 else position.column % 2 == 0
        val base = (position.column - 1) * BOARD_ROWS
        return base + if (columnGoesUp) position.row else (BOARD_ROWS + 1 - position.row)
    }
}
