import Foundation
import shared

@MainActor
final class BoardObservable: ObservableObject {
    @Published var state: BoardUiState
    private let viewModel: BoardViewModel

    init() {
        let client = MoonBoardBleClient(context: PlatformContext())
        let vm = BoardViewModel(bleClient: client)
        self.viewModel = vm
        self.state = vm.uiState.value
        vm.observeState { [weak self] newState in
            DispatchQueue.main.async {
                self?.state = newState
            }
        }
    }

    func startScan() { viewModel.startScan() }
    func connect(device: BleDevice) { viewModel.connect(device: device) }
    func disconnect() { viewModel.disconnect() }
    func tapHold(position: GridPosition) { viewModel.tapHold(position: position) }
    func selectSetup(setup: BoardSetup) { viewModel.selectSetup(setup: setup) }
    func clearBoard() { viewModel.clearBoard() }
    func sendToBoard() { viewModel.sendToBoard() }
}
