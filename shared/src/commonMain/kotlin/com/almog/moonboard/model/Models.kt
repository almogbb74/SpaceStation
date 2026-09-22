package com.almog.moonboard.model

const val BOARD_COLUMNS = 11 // labeled A..K on a real MoonBoard
const val BOARD_ROWS = 18    // labeled 1..18

data class GridPosition(val column: Int, val row: Int) {
    init {
        require(column in 1..BOARD_COLUMNS) { "column must be 1..$BOARD_COLUMNS, was $column" }
        require(row in 1..BOARD_ROWS) { "row must be 1..$BOARD_ROWS, was $row" }
    }
}

enum class HoldType { START, MID, END }

data class PlacedHold(val position: GridPosition, val type: HoldType)
