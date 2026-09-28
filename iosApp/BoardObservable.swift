import Foundation
import shared

@MainActor
final class BoardObservable: ObservableObject {
    private let viewModel: BoardViewModel
    @Published var state: BoardUiState

    init(viewModel: BoardViewModel) {
        self.viewModel = viewModel
        self.state = viewModel.uiState.value
        viewModel.observeState { [weak self] newState in
            DispatchQueue.main.async { self?.state = newState }
        }
    }

    func openConnectDialog() { viewModel.openConnectDialog() }
    func closeConnectDialog() { viewModel.closeConnectDialog() }
    func connect(device: BleDevice) { viewModel.connect(device: device) }
    func disconnect() { viewModel.disconnect() }
    func selectSetup(_ setup: BoardSetup) { viewModel.selectSetup(setup: setup) }
    func tapHold(_ position: GridPosition) { viewModel.tapHold(position: position) }
    func clearBoard() { viewModel.clearBoard() }
    func sendToBoard() { viewModel.sendToBoard() }
    func onCleared() { viewModel.onCleared() }
}
