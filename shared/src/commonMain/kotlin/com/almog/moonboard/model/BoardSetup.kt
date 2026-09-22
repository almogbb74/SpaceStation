package com.almog.moonboard.model

/**
 * Which physical hold set is screwed onto the board - determines which of the 198 grid
 * positions actually have a hold (the LED strip covers all 198 either way).
 *
 * Position data for everything except 2024 is community-extracted from MoonBoard's own site:
 * https://github.com/e-sr/moonboard/blob/master/problems/HoldSetup.json
 * 2024 needs no data: it's a full high-density grid using every one of the 198 positions.
 */
enum class BoardSetup(val displayName: String) {
    MOONBOARD_2024("MoonBoard 2024"),
    MASTERS_2019("MoonBoard Masters 2019"),
    MASTERS_2017("MoonBoard Masters 2017"),
    MOONBOARD_2016("MoonBoard 2016"),
    MINI_2020("Mini MoonBoard 2020");

    fun occupiedPositions(): Set<GridPosition> = when (this) {
        MOONBOARD_2024 -> allPositions
        MASTERS_2019 -> masters2019Positions
        MASTERS_2017 -> masters2017Positions
        MOONBOARD_2016 -> moonboard2016Positions
        MINI_2020 -> mini2020Positions
    }
}

private fun parseLabels(labels: String): Set<GridPosition> =
    labels.split(",").map { label ->
        val column = label[0] - 'A' + 1
        val row = label.substring(1).toInt()
        GridPosition(column, row)
    }.toSet()

private val allPositions: Set<GridPosition> =
    (1..BOARD_COLUMNS).flatMap { column -> (1..BOARD_ROWS).map { row -> GridPosition(column, row) } }.toSet()

private val moonboard2016Positions: Set<GridPosition> = parseLabels(
    "A10,A11,A12,A13,A15,A16,A5,B11,B13,B16,B18,B3,B4,B7,B8,B9,C10,C11,C12,C14,C15,C18,C6,C7,C9,D10,D12,D13,D14," +
        "D16,D17,D3,D5,D6,D8,D9,E10,E11,E13,E15,E18,E7,F10,F12,F14,F15,F16,F5,F6,F7,F8,F9,G11,G12,G13,G16,G18,G2," +
        "G7,G8,H11,H14,H15,H18,H7,H9,I11,I12,I13,I16,I4,I5,I6,I8,J10,J11,J14,J2,J6,J8,J9,K10,K11,K12,K13,K18,K5,K6,K7,K8"
)

private val masters2017Positions: Set<GridPosition> = parseLabels(
    "A1,A11,A12,A13,A14,A15,A16,A17,A18,A2,A3,A4,A5,A6,A8,A9,B1,B10,B11,B13,B14,B16,B17,B18,B2,B3,B4,B6,B7,B8,B9," +
        "C1,C10,C12,C13,C14,C15,C16,C17,C2,C3,C4,C5,C7,C8,D1,D10,D11,D12,D14,D15,D17,D18,D2,D3,D4,D5,D6,D8,D9,E1," +
        "E10,E11,E13,E14,E16,E17,E18,E2,E3,E4,E6,E7,E8,F1,F10,F11,F12,F13,F14,F15,F16,F17,F18,F2,F3,F4,F5,F6,F7,F8," +
        "F9,G1,G10,G11,G13,G14,G16,G17,G18,G2,G3,G4,G6,G7,G8,H1,H10,H11,H12,H14,H15,H17,H18,H2,H3,H4,H5,H6,H8,H9," +
        "I1,I10,I12,I13,I14,I15,I16,I17,I2,I3,I4,I5,I7,I8,J1,J10,J11,J13,J14,J16,J17,J18,J2,J3,J4,J6,J7,J8,J9,K1," +
        "K11,K12,K13,K14,K15,K16,K17,K18,K2,K3,K4,K5,K6,K8,K9"
)

private val masters2019Positions: Set<GridPosition> = parseLabels(
    "A1,A10,A11,A12,A13,A14,A16,A17,A18,A2,A3,A4,A6,A7,A8,A9,B1,B10,B11,B12,B13,B15,B16,B17,B18,B2,B3,B4,B5,B6," +
        "B7,B8,B9,C1,C10,C11,C12,C13,C14,C16,C17,C18,C2,C4,C5,C6,C7,C8,C9,D1,D10,D12,D13,D14,D15,D16,D17,D18,D2," +
        "D3,D4,D5,D6,D7,D8,E1,E11,E12,E13,E14,E16,E17,E18,E2,E3,E4,E5,E6,E7,E9,F1,F10,F11,F13,F16,F17,F18,F2,F3," +
        "F4,F5,F6,F7,F8,F9,G1,G10,G11,G12,G14,G15,G16,G17,G18,G2,G3,G4,G6,G7,G8,G9,H1,H12,H14,H15,H16,H17,H18,H2," +
        "H3,H4,H5,H7,H8,H9,I1,I10,I11,I12,I13,I14,I15,I16,I18,I2,I3,I4,I5,I6,I7,I8,J1,J10,J11,J12,J13,J14,J15,J16," +
        "J17,J18,J2,J4,J6,J7,J8,K1,K10,K11,K12,K13,K14,K15,K16,K17,K18,K2,K3,K4,K5,K6,K7,K8,K9"
)

private val mini2020Positions: Set<GridPosition> = parseLabels(
    "A10,A11,A12,A2,A3,A4,A5,A7,A8,B10,B11,B12,B2,B3,B4,B5,B6,B7,B8,B9,C10,C11,C12,C2,C5,C6,C7,C8,C9,D10,D11,D12," +
        "D2,D4,D5,D7,D8,D9,E10,E12,E2,E3,E5,E6,E8,E9,F10,F11,F12,F2,F3,F5,F7,F8,G10,G11,G12,G3,G4,G5,G6,G8,G9,H11," +
        "H12,H2,H5,H6,H7,H8,I10,I11,I12,I2,I3,I5,I6,I7,I8,I9,J10,J11,J12,J2,J3,J4,J6,J7,J8,K11,K12,K2,K3,K4,K8,K9"
)
