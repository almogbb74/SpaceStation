package com.almog.moonboard.ble

import com.almog.moonboard.model.HoldType
import com.almog.moonboard.model.PlacedHold

/** Builds the ASCII payload the box expects, e.g. "l#S5,P9,P13,E18#". */
object PayloadBuilder {
    fun build(holds: List<PlacedHold>): String {
        val parts = holds
            .sortedBy { LedMapper.ledIndex(it.position) }
            .map { hold ->
                val code = when (hold.type) {
                    HoldType.START -> 'S'
                    HoldType.MID -> 'P'
                    HoldType.END -> 'E'
                }
                "$code${LedMapper.ledIndex(hold.position)}"
            }
        return "l#${parts.joinToString(",")}#"
    }
}
