import Foundation
import shared

@MainActor
final class SnakeObservable: ObservableObject {
    private let viewModel: SnakeViewModel
    @Published var state: SnakeUiState

    init(viewModel: SnakeViewModel) {
        self.viewModel = viewModel
        self.state = viewModel.uiState.value
        viewModel.observeState { [weak self] newState in
            DispatchQueue.main.async { self?.state = newState }
        }
    }

    func start() { viewModel.start() }
    func queueDirection(_ direction: Direction) { viewModel.queueDirection(direction: direction) }
    func stopGame() { viewModel.stopGame() }
    func onCleared() { viewModel.onCleared() }
}
