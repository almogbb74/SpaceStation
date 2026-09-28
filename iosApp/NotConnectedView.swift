import SwiftUI

struct NotConnectedView: View {
    var body: some View {
        VStack(spacing: 20) {
            RadarPulse()
            Text("NOT CONNECTED TO A MOONBOARD")
                .font(.ibmPlexMono(size: 13))
                .tracking(1)
                .foregroundColor(.moonBoardTextMuted)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(32)
    }
}

private struct RadarPulse: View {
    var body: some View {
        ZStack {
            ForEach(0..<3, id: \.self) { index in
                RadarRing(delay: Double(index) * 0.8)
            }
            Circle()
                .fill(Color.moonBoardSurfaceVariant)
                .frame(width: 30, height: 30)
                .overlay(
                    Image(systemName: "wave.3.right")
                        .font(.system(size: 12))
                        .foregroundColor(.moonBoardAccent)
                )
        }
        .frame(width: 96, height: 96)
    }
}

private struct RadarRing: View {
    let delay: Double
    @State private var animate = false

    var body: some View {
        Circle()
            .stroke(Color.moonBoardAccent, lineWidth: 1.5)
            .frame(width: 76, height: 76)
            .scaleEffect(animate ? 1.8 : 0.4)
            .opacity(animate ? 0 : 0.7)
            .onAppear {
                withAnimation(Animation.linear(duration: 2.4).repeatForever(autoreverses: false).delay(delay)) {
                    animate = true
                }
            }
    }
}
