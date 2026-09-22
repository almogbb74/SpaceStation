import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var board = BoardObservable()

    var body: some View {
        VStack(spacing: 12) {
            connectionBar
            setupSelector
            BoardGridView(setup: board.state.selectedSetup, holds: board.state.holds, onTap: board.tapHold)
            HStack {
                Button("Clear") { board.clearBoard() }
                Button("Light it up") { board.sendToBoard() }
                    .disabled(!(board.state.connectionState is ConnectionState.Connected))
            }
        }
        .padding()
    }

    private var connectionBar: some View {
        VStack(alignment: .leading) {
            Text(connectionLabel)
            if board.state.connectionState is ConnectionState.Connected {
                Button("Disconnect") { board.disconnect() }
            } else {
                Button("Scan for MoonBoard") { board.startScan() }
                ForEach(board.state.availableDevices, id: \.id) { device in
                    Button(device.name) { board.connect(device: device) }
                }
            }
        }
    }

    private var setupSelector: some View {
        HStack {
            ForEach(Array(BoardSetup.values()), id: \.self) { setup in
                Button(setup == board.state.selectedSetup ? "[\(setup.displayName)]" : setup.displayName) {
                    board.selectSetup(setup: setup)
                }
            }
        }
    }

    private var connectionLabel: String {
        let s = board.state.connectionState
        if s is ConnectionState.Disconnected {
            return "Disconnected"
        } else if s is ConnectionState.Scanning {
            return "Scanning..."
        } else if let connecting = s as? ConnectionState.Connecting {
            return "Connecting to \(connecting.deviceName)..."
        } else if let connected = s as? ConnectionState.Connected {
            return "Connected: \(connected.deviceName)"
        } else if let error = s as? ConnectionState.Error {
            return "Error: \(error.message)"
        }
        return "Unknown"
    }
}
