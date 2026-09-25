package com.almog.moonboard.snake

import com.almog.moonboard.model.GridPosition

object SpiralOrder {
    fun forGrid(columns: Int, rows: Int): List<GridPosition> {
        val result = ArrayList<GridPosition>(columns * rows)
        var left = 1; var right = columns
        var bottom = 1; var top = rows
        while (left <= right && bottom <= top) {
            for (column in left..right) result.add(GridPosition(column, bottom))
            for (row in (bottom + 1)..top) result.add(GridPosition(right, row))
            if (bottom < top) for (column in (right - 1) downTo left) result.add(GridPosition(column, top))
            if (left < right) for (row in (top - 1) downTo (bottom + 1)) result.add(GridPosition(left, row))
            left++; right--; bottom++; top--
        }
        return result
    }
}
