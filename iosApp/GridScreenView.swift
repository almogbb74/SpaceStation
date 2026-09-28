import SwiftUI
import shared

struct GridScreenView: View {
    @ObservedObject var board: BoardObservable

    var body: some View {
        if board.state.connectionState is ConnectionState.Connected {
            VStack {
                BoardGridView(setup: board.state.selectedSetup, holds: board.state.holds, onTap: board.tapHold)
                HStack {
                    Button("Clear") { board.clearBoard() }
                    Button("Light it up") { board.sendToBoard() }
                }
                .padding(.top, 12)
            }
            .padding(16)
        } else {
            NotConnectedView()
        }
    }
}
