import SwiftUI
import shared

@main
struct MoonBoardApp: App {
    @StateObject private var board: BoardObservable
    @StateObject private var snake: SnakeObservable

    init() {
        AppLogger.shared.doInit(context: PlatformContext())
        let client = MoonBoardBleClient(context: PlatformContext())
        _board = StateObject(wrappedValue: BoardObservable(viewModel: BoardViewModel(bleClient: client)))
        _snake = StateObject(wrappedValue: SnakeObservable(viewModel: SnakeViewModel(bleClient: client)))
    }

    var body: some Scene {
        WindowGroup {
            RootView(board: board, snake: snake)
        }
    }
}
