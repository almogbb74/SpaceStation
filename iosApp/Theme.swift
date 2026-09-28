import SwiftUI

extension Color {
    static let moonBoardBackground = Color(red: 0x16 / 255, green: 0x1D / 255, blue: 0x28 / 255)
    static let moonBoardSurfaceVariant = Color(red: 0x23 / 255, green: 0x2D / 255, blue: 0x3E / 255)
    static let moonBoardAccent = Color(red: 0x7F / 255, green: 0x68 / 255, blue: 0xB7 / 255)
    static let moonBoardTextPrimary = Color(red: 0xC6 / 255, green: 0xC0 / 255, blue: 0xA1 / 255)
    static let moonBoardTextMuted = Color(red: 0xB2 / 255, green: 0xB1 / 255, blue: 0xAB / 255)
    static let moonBoardSuccess = Color(red: 0x6F / 255, green: 0xCF / 255, blue: 0x97 / 255)
    // Material3 darkColorScheme's default `error` - Theme.kt never overrides it, so this mirrors that default exactly.
    static let moonBoardError = Color(red: 0xCF / 255, green: 0x66 / 255, blue: 0x79 / 255)

    static let snakeHead = Color(red: 0x2E / 255, green: 0x7D / 255, blue: 0x32 / 255)
    static let snakeBody = Color(red: 0x15 / 255, green: 0x65 / 255, blue: 0xC0 / 255)
    static let snakeFood = Color(red: 0xC6 / 255, green: 0x28 / 255, blue: 0x28 / 255)
}
