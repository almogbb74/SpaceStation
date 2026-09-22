package com.almog.moonboard.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoardSetupTest {
    @Test
    fun occupiedPositions_matchKnownHoldCountsFromMoonboardSite() {
        // Counts cross-checked against the community-extracted HoldSetup.json.
        assertEquals(198, BoardSetup.MOONBOARD_2024.occupiedPositions().size)
        assertEquals(90, BoardSetup.MOONBOARD_2016.occupiedPositions().size)
        assertEquals(166, BoardSetup.MASTERS_2017.occupiedPositions().size)
        assertEquals(174, BoardSetup.MASTERS_2019.occupiedPositions().size)
        assertEquals(96, BoardSetup.MINI_2020.occupiedPositions().size)
    }

    @Test
    fun occupiedPositions_stayWithinTheBoardGrid() {
        for (setup in BoardSetup.entries) {
            for (position in setup.occupiedPositions()) {
                assertTrue(position.column in 1..BOARD_COLUMNS, "$setup has out-of-range column $position")
                assertTrue(position.row in 1..BOARD_ROWS, "$setup has out-of-range row $position")
            }
        }
    }
}
