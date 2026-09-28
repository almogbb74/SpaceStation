import SwiftUI
import shared

struct RootView: View {
    @ObservedObject var board: BoardObservable
    @ObservedObject var snake: SnakeObservable

    var body: some View {
        TabView {
            NavigationStack { GridScreenView(board: board) }
                .moonBoardToolbar(board: board)
                .tabItem { Label("Grid", systemImage: "square.grid.2x2") }

            NavigationStack { SnakeScreenView(snake: snake) }
                .moonBoardToolbar(board: board)
                .tabItem { Label("Snake", systemImage: "gamecontroller") }

            NavigationStack { SettingsScreenView(board: board) }
                .moonBoardToolbar(board: board)
                .tabItem { Label("Settings", systemImage: "gearshape") }
        }
        .tint(.moonBoardAccent)
        .sheet(isPresented: Binding(
            get: { board.state.isConnectDialogOpen },
            set: { isOpen in if !isOpen { board.closeConnectDialog() } }
        )) {
            ConnectDeviceDialogView(board: board)
        }
    }
}

private struct MoonBoardToolbar: ViewModifier {
    @ObservedObject var board: BoardObservable

    func body(content: Content) -> some View {
        content
            .toolbarBackground(Color.moonBoardBackground, for: .navigationBar)
            .toolbarBackground(.visible, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    Text("SPACESTATION")
                        .font(.ibmPlexMono(size: 16, weight: .medium))
                        .tracking(1.5)
                        .foregroundColor(.moonBoardTextMuted)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { board.openConnectDialog() }) {
                        // SF Symbols has no official Bluetooth glyph (Apple reserves it as a
                        // trademarked logo) - "wave.3.right" stands in everywhere a Bluetooth
                        // icon appears in the Android app, distinguished by tint instead of a
                        // connected/disconnected symbol swap.
                        Image(systemName: "wave.3.right")
                            .foregroundColor(board.state.connectionState is ConnectionState.Connected ? .moonBoardSuccess : .moonBoardTextMuted)
                    }
                    .accessibilityLabel("Connect to MoonBoard")
                }
            }
    }
}

extension View {
    func moonBoardToolbar(board: BoardObservable) -> some View {
        modifier(MoonBoardToolbar(board: board))
    }
}
