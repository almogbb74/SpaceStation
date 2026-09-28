import SwiftUI
import shared

struct ConnectDeviceDialogView: View {
    @ObservedObject var board: BoardObservable

    private let leadingColumnWidth: CGFloat = 22

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                statusSection
                Spacer()
            }
            .padding(20)
            .background(Color.moonBoardBackground)
            .navigationTitle("")
            .toolbar {
                ToolbarItem(placement: .principal) {
                    HStack(spacing: 10) {
                        Image(systemName: "wave.3.right")
                            .foregroundColor(.moonBoardAccent)
                            .frame(width: leadingColumnWidth)
                        Text("Connect to MoonBoard")
                            .font(.system(size: 20, weight: .bold))
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    if board.state.connectionState is ConnectionState.Connected {
                        Button("Disconnect") {
                            board.disconnect()
                            board.closeConnectDialog()
                        }
                    } else {
                        Button("Close") { board.closeConnectDialog() }
                    }
                }
            }
        }
    }

    // Kotlin sealed subclasses of ConnectionState bridge to Swift as plain classes (no vanilla
    // Kotlin/Native export turns them into a Swift enum), so branching uses `as?`/`is`, the same
    // pattern the original ContentView.swift established for this type.
    @ViewBuilder
    private var statusSection: some View {
        if let connected = board.state.connectionState as? ConnectionState.Connected {
            statusRow(icon: "checkmark.circle.fill", tint: .moonBoardSuccess, text: "Connected to \(connected.deviceName)")
        } else if let connecting = board.state.connectionState as? ConnectionState.Connecting {
            statusRow(spinner: true, tint: .moonBoardTextMuted, text: "Connecting to \(connecting.deviceName)…")
        } else if let error = board.state.connectionState as? ConnectionState.Error {
            statusRow(icon: "exclamationmark.triangle.fill", tint: .moonBoardError, text: "Error: \(error.message)")
        } else if board.state.connectionState is ConnectionState.BluetoothOff {
            statusRow(icon: "xmark.circle.fill", tint: .moonBoardError, text: "Bluetooth is turned off")
        } else {
            VStack(alignment: .leading, spacing: 12) {
                statusRow(spinner: true, tint: .moonBoardTextMuted, text: "Scanning for nearby MoonBoards…")
                if !board.state.availableDevices.isEmpty {
                    VStack(spacing: 0) {
                        ForEach(Array(board.state.availableDevices.enumerated()), id: \.element.id) { index, device in
                            if index > 0 { Divider().background(Color.moonBoardBackground) }
                            deviceRow(device)
                        }
                    }
                    .background(Color.moonBoardSurfaceVariant)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
        }
    }

    private func statusRow(icon: String? = nil, spinner: Bool = false, tint: Color, text: String) -> some View {
        HStack(spacing: 10) {
            Group {
                if spinner {
                    ProgressView().tint(tint)
                } else if let icon {
                    Image(systemName: icon).foregroundColor(tint)
                }
            }
            .frame(width: leadingColumnWidth)
            Text(text).foregroundColor(tint).font(.system(size: 14))
        }
    }

    private func deviceRow(_ device: BleDevice) -> some View {
        Button(action: { board.connect(device: device) }) {
            HStack {
                Image(systemName: "wave.3.right").foregroundColor(.moonBoardAccent)
                Text(device.name).foregroundColor(.moonBoardTextPrimary)
                Spacer()
                Image(systemName: "chevron.right").foregroundColor(.moonBoardTextMuted)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
        }
    }
}
