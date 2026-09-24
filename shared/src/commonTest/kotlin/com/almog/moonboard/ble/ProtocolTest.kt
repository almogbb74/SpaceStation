package com.almog.moonboard.ble

import com.almog.moonboard.model.GridPosition
import com.almog.moonboard.model.HoldType
import com.almog.moonboard.model.PlacedHold
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProtocolTest {
    @Test
    fun ledIndex_coversFullRangeAcrossColumnFlips() {
        // 0-indexed (0..197), matching the community HoldSetup numbering (holdId 1/12/198 -> LED 0/1/197).
        assertEquals(0, LedMapper.ledIndex(GridPosition(1, 1)))
        assertEquals(1, LedMapper.ledIndex(GridPosition(1, 2)))
        assertEquals(17, LedMapper.ledIndex(GridPosition(1, 18)))
        assertEquals(18, LedMapper.ledIndex(GridPosition(2, 18)))
        assertEquals(35, LedMapper.ledIndex(GridPosition(2, 1)))
        assertEquals(197, LedMapper.ledIndex(GridPosition(11, 18)))
    }

    @Test
    fun payload_matchesDocumentedExampleFormat() {
        val holds = listOf(
            PlacedHold(GridPosition(1, 6), HoldType.START),
            PlacedHold(GridPosition(1, 10), HoldType.MID),
            PlacedHold(GridPosition(1, 18), HoldType.END),
        )
        assertEquals("l#S5,P9,E17#", PayloadBuilder.build(holds))
    }

    @Test
    fun chunk_staysUnder20BytesAndReassembles() {
        val payload = "l#" + (1..50).joinToString(",") { "P$it" } + "#"
        val chunks = MoonBoardProtocol.chunk(payload)
        assertEquals(payload, chunks.joinToString("") { it.decodeToString() })
        chunks.forEach { assertTrue(it.size <= 20) }
    }
}
