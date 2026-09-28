import SwiftUI
import shared

struct SettingsScreenView: View {
    @ObservedObject var board: BoardObservable
    @State private var isSharingLog = false

    var body: some View {
        Form {
            // Android keeps this pinned to the drawer's footer; a bottom tab bar has no drawer to
            // pin it to, so it lives here instead - same three states, same colored-dot pattern.
            Section {
                ConnectionStatusRow(connectionState: board.state.connectionState)
            }

            Section("Board setup") {
                ForEach(Array(BoardSetup.values()), id: \.self) { setup in
                    Button(action: { board.selectSetup(setup) }) {
                        HStack {
                            Text(setup.displayName).foregroundColor(.moonBoardTextPrimary)
                            Spacer()
                            if setup == board.state.selectedSetup {
                                Image(systemName: "checkmark").foregroundColor(.moonBoardAccent)
                            }
                        }
                    }
                }
            }

            Section("Debug log") {
                Text("Scan/connect/write events are saved on-device so they can be shared after testing away from a PC.")
                    .font(.footnote)
                    .foregroundColor(.moonBoardTextMuted)
                HStack {
                    Button("Share log") { isSharingLog = true }
                    Spacer()
                    Button("Clear log", role: .destructive) { clearLog() }
                }
            }
        }
        .sheet(isPresented: $isSharingLog) {
            if let path = AppLogger.shared.currentLogFilePath() {
                ShareSheet(activityItems: [URL(fileURLWithPath: path)])
            }
        }
    }

    private func clearLog() {
        guard let path = AppLogger.shared.currentLogFilePath() else { return }
        try? "".write(toFile: path, atomically: true, encoding: .utf8)
    }
}

private struct ConnectionStatusRow: View {
    let connectionState: ConnectionState

    var body: some View {
        HStack(spacing: 10) {
            Circle().fill(dotColor).frame(width: 8, height: 8)
            Text(statusText).font(.ibmPlexMono(size: 12)).foregroundColor(.moonBoardTextMuted)
        }
    }

    private var dotColor: Color {
        if connectionState is ConnectionState.Connected { return .moonBoardSuccess }
        if connectionState is ConnectionState.BluetoothOff { return .moonBoardError }
        return .moonBoardTextMuted
    }

    private var statusText: String {
        if let connected = connectionState as? ConnectionState.Connected {
            return "Connected to \(connected.deviceName)"
        }
        if connectionState is ConnectionState.BluetoothOff {
            return "Bluetooth is turned off"
        }
        return "Not connected to a MoonBoard"
    }
}

private struct ShareSheet: UIViewControllerRepresentable {
    let activityItems: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
