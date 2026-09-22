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
        assertEquals(1, LedMapper.ledIndex(GridPosition(1, 1)))
        assertEquals(18, LedMapper.ledIndex(GridPosition(1, 18)))
        assertEquals(19, LedMapper.ledIndex(GridPosition(2, 18)))
        assertEquals(36, LedMapper.ledIndex(GridPosition(2, 1)))
        assertEquals(198, LedMapper.ledIndex(GridPosition(11, 18)))
    }

    @Test
    fun payload_matchesDocumentedExampleFormat() {
        // column 1 has an identity row->index mapping, so this reproduces the community's
        // documented example payload exactly: "l#S5,P9,E18#"
        val holds = listOf(
            PlacedHold(GridPosition(1, 5), HoldType.START),
            PlacedHold(GridPosition(1, 9), HoldType.MID),
            PlacedHold(GridPosition(1, 18), HoldType.END),
        )
        assertEquals("l#S5,P9,E18#", PayloadBuilder.build(holds))
    }

    @Test
    fun chunk_staysUnder20BytesAndReassembles() {
        val payload = "l#" + (1..50).joinToString(",") { "P$it" } + "#"
        val chunks = MoonBoardProtocol.chunk(payload)
        assertEquals(payload, chunks.joinToString("") { it.decodeToString() })
        chunks.forEach { assertTrue(it.size <= 20) }
    }
}
