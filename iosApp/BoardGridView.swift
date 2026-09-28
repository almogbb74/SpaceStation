import SwiftUI
import shared

struct BoardGridView: View {
    let setup: BoardSetup
    let holds: [GridPosition: HoldType]
    let onTap: (GridPosition) -> Void

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 2), count: Int(BOARD_COLUMNS))

    private var occupied: Set<GridPosition> { setup.occupiedPositions() }

    var body: some View {
        LazyVGrid(columns: columns, spacing: 2) {
            ForEach(0..<Int(BOARD_COLUMNS * BOARD_ROWS), id: \.self) { index in
                let column = Int32(index % Int(BOARD_COLUMNS)) + 1
                let row = BOARD_ROWS - Int32(index / Int(BOARD_COLUMNS))
                let position = GridPosition(column: column, row: row)
                if occupied.contains(position) {
                    Circle()
                        .fill(color(for: holds[position]))
                        .frame(width: 24, height: 24)
                        .onTapGesture { onTap(position) }
                } else {
                    Color.clear.frame(width: 24, height: 24)
                }
            }
        }
    }

    private func color(for type: HoldType?) -> Color {
        switch type {
        case .start: return .snakeHead
        case .mid: return .snakeBody
        case .end: return .snakeFood
        case .none: return Color(white: 0.88)
        }
    }
}
