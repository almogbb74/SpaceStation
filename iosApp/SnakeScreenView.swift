import SwiftUI
import shared

struct SnakeScreenView: View {
    @ObservedObject var snake: SnakeObservable

    var body: some View {
        Group {
            if snake.state.connectionState is ConnectionState.Connected {
                // GameStatus.name is Kotlin's built-in enum property (always "IDLE"/"RUNNING"/...
                // verbatim) - comparing against it sidesteps any ambiguity in how Kotlin/Native's
                // Swift export would rename a multi-word entry like GAME_OVER.
                let status = snake.state.status.name
                ZStack {
                    if status == "IDLE" {
                        SnakeIdleContent(onStart: { snake.start() })
                    } else {
                        SnakeGameContent(
                            score: Int(snake.state.score),
                            highScore: Int(snake.state.highScore),
                            interactive: status == "RUNNING",
                            onDirection: { snake.queueDirection($0) }
                        )
                    }
                    if status == "DYING" || status == "GAME_OVER" {
                        GameOverOverlay(score: Int(snake.state.score), highScore: Int(snake.state.highScore), onRestart: { snake.start() })
                    }
                }
            } else {
                NotConnectedView()
            }
        }
        .onDisappear { snake.stopGame() }
    }
}

private struct SnakeIdleContent: View {
    let onStart: () -> Void

    var body: some View {
        ZStack {
            ImmersiveBackground()
            VStack(spacing: 28) {
                Text("Snake")
                    .font(.unboundedExtraBold(size: 40))
                    .tracking(0.5)
                    .foregroundColor(.moonBoardTextPrimary)
                    .shadow(color: Color.snakeHead.opacity(0.6), radius: 24)
                Button(action: onStart) {
                    HStack(spacing: 8) {
                        Image(systemName: "play.fill").font(.system(size: 14))
                        Text("Start").fontWeight(.semibold)
                    }
                    .padding(.horizontal, 40)
                    .padding(.vertical, 14)
                    .background(Color.moonBoardAccent)
                    .foregroundColor(.moonBoardBackground)
                    .clipShape(Capsule())
                    .shadow(color: Color.moonBoardAccent.opacity(0.6), radius: 16)
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(32)
    }
}

private struct ImmersiveBackground: View {
    var body: some View {
        TimelineView(.animation) { context in
            Canvas { graphicsContext, size in
                drawDotGrid(in: &graphicsContext, size: size)
                drawTracePath(in: &graphicsContext, size: size, date: context.date)
            }
        }
    }

    private func drawDotGrid(in context: inout GraphicsContext, size: CGSize) {
        let spacing: CGFloat = 16
        let dotColor = Color.moonBoardTextPrimary.opacity(0.08)
        var y = spacing / 2
        while y < size.height {
            var x = spacing / 2
            while x < size.width {
                let rect = CGRect(x: x - 1.3, y: y - 1.3, width: 2.6, height: 2.6)
                context.fill(Path(ellipseIn: rect), with: .color(dotColor))
                x += spacing
            }
            y += spacing
        }
    }

    private func drawTracePath(in context: inout GraphicsContext, size: CGSize, date: Date) {
        var path = Path()
        path.move(to: CGPoint(x: size.width * 0.12, y: size.height * 0.18))
        path.addCurve(
            to: CGPoint(x: size.width * 0.75, y: size.height * 0.42),
            control1: CGPoint(x: size.width * 0.6, y: size.height * 0.18),
            control2: CGPoint(x: size.width * 0.3, y: size.height * 0.42)
        )
        path.addCurve(
            to: CGPoint(x: size.width * 0.5, y: size.height * 0.68),
            control1: CGPoint(x: size.width * 1.05, y: size.height * 0.42),
            control2: CGPoint(x: size.width * 0.85, y: size.height * 0.68)
        )
        path.addCurve(
            to: CGPoint(x: size.width * 0.68, y: size.height * 0.95),
            control1: CGPoint(x: size.width * 0.22, y: size.height * 0.68),
            control2: CGPoint(x: size.width * 0.3, y: size.height * 0.9)
        )

        // 2.4s loop, matching Compose's tween(2400) - dash phase cycles through one dash+gap period.
        let period = 2.4
        let progress = date.timeIntervalSinceReferenceDate.truncatingRemainder(dividingBy: period) / period
        let dashPhase = CGFloat(progress) * -32

        context.stroke(
            path,
            with: .color(Color.snakeBody.opacity(0.8)),
            style: StrokeStyle(lineWidth: 3, lineCap: .round, dash: [14, 18], dashPhase: dashPhase)
        )
    }
}

private struct SnakeGameContent: View {
    let score: Int
    let highScore: Int
    let interactive: Bool
    let onDirection: (Direction) -> Void

    var body: some View {
        VStack {
            HStack(spacing: 24) {
                Text("Score: \(score)").font(.title3)
                Text("Best: \(highScore)").font(.title3)
            }
            Spacer()
            DirectionPad(enabled: interactive, onDirection: onDirection)
            Spacer()
        }
        .padding(16)
    }
}

private struct DirectionPad: View {
    let enabled: Bool
    let onDirection: (Direction) -> Void

    var body: some View {
        VStack(spacing: 16) {
            DirectionButton(icon: "chevron.up", enabled: enabled) { onDirection(.up) }
            HStack(spacing: 56) {
                DirectionButton(icon: "chevron.left", enabled: enabled) { onDirection(.left) }
                DirectionButton(icon: "chevron.right", enabled: enabled) { onDirection(.right) }
            }
            DirectionButton(icon: "chevron.down", enabled: enabled) { onDirection(.down) }
        }
    }
}

private struct DirectionButton: View {
    let icon: String
    let enabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 64, weight: .regular))
                .frame(width: 130, height: 130)
                .background(Color.moonBoardSurfaceVariant)
                .clipShape(Circle())
        }
        .disabled(!enabled)
    }
}

private struct GameOverOverlay: View {
    let score: Int
    let highScore: Int
    let onRestart: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            Text("Game over").font(.title).foregroundColor(.white)
            Text("Score: \(score)").foregroundColor(.white)
            Text("Best: \(highScore)").foregroundColor(.white)
            Button("Restart", action: onRestart)
                .padding(.top, 8)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.black.opacity(0.7))
    }
}
