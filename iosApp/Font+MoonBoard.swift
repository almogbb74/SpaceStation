import SwiftUI
import CoreText

extension Font {
    static func ibmPlexMono(size: CGFloat, weight: Font.Weight = .medium) -> Font {
        Font.custom(weight == .bold ? "IBMPlexMono-Bold" : "IBMPlexMono-Medium", size: size)
    }

    static func unboundedExtraBold(size: CGFloat) -> Font {
        Font(unboundedExtraBoldUIFont(size: size))
    }
}

private let wghtAxisIdentifier: UInt32 = 0x77676874 // 'wght', per CoreText's variation-axis tag convention

// The bundled Unbounded.ttf is a variable font; its default named instance is Regular, so getting
// the same weight 800 Android pins via FontVariation.weight(800) means setting the wght axis
// explicitly on the descriptor, the documented CoreText pattern for variable font weight.
private func unboundedExtraBoldUIFont(size: CGFloat) -> UIFont {
    let baseDescriptor = UIFontDescriptor(name: "Unbounded-Regular", size: size)
    let variedDescriptor = baseDescriptor.addingAttributes([
        UIFontDescriptor.AttributeName(kCTFontVariationAttribute as String): [wghtAxisIdentifier: 800]
    ])
    return UIFont(descriptor: variedDescriptor, size: size)
}
