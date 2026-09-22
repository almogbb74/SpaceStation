# MoonBoard BLE controller

Kotlin Multiplatform app that connects to a MoonBoard v1 LED box over BLE and lights up holds.

## Protocol (reverse-engineered, no official spec)

Sources: [willslawrence/moonboard](https://github.com/willslawrence/moonboard), [e-sr/moonboard](https://github.com/e-sr/moonboard/blob/master/ble/README.md).

- Standard **Nordic UART Service**, no auth/handshake. Device advertises as `Moonboard A`, one connection at a time.
  - Service: `6e400001-b5a3-f393-e0a9-e50e24dcca9e`
  - Write (RX): `6e400002-b5a3-f393-e0a9-e50e24dcca9e`
  - Notify (TX): `6e400003-b5a3-f393-e0a9-e50e24dcca9e`
- Payload is ASCII: `l#S5,P9,P13,E18#` (`S`=start, `P`=middle, `E`=end, numbers are LED index 1-198).
- LED index = column-major serpentine over an 11x18 grid. **Not confirmed against real hardware** — see the `ponytail:` comment in `LedMapper.kt`; flip `COLUMN_ONE_GOES_UP` if holds light up mirrored.
- Writes are chunked to 20 bytes (default BLE MTU) with a 30ms gap between chunks.

**Control box hardware (V1 vs V4/V5/2024):** the protocol above was reverse-engineered against the original MoonBoard v1 control box. MoonBoard's newer boxes (V4/V5, which is what ships with the 2024 hold set) additionally support multiple simultaneous phone connections. There's no public packet capture confirming V4/V5 uses the exact same GATT service — but `FabianRig/ArduinoMoonBoardLED`, a DIY replacement box explicitly tested against "the updated MoonBoard app," uses this same Nordic UART Service, which is strong circumstantial evidence the app-side protocol hasn't changed across box generations (the multi-connection support most likely comes from the box's BLE chip accepting more than one central connection, not a different protocol). **Unconfirmed until tested against real V4/V5 hardware.**

## Board hold layout (`BoardSetup.kt`)

Which of the 198 grid positions actually have a hold depends on which hold set is screwed onto the board. This is separate from the LED protocol above — the LED strip covers all 198 positions regardless — and only affects which positions the app shows/allows tapping.

- **MoonBoard 2024** (what a V4/V5 box with the 2024 hold set uses): full grid, all 198 positions — needs no data.
- **2016 / Masters 2017 / Masters 2019 / Mini 2020**: exact per-position data extracted from MoonBoard's own site by the community, via [e-sr/moonboard's `HoldSetup.json`](https://github.com/e-sr/moonboard/blob/master/problems/HoldSetup.json). Hold counts (90/166/174/96) are cross-checked in `BoardSetupTest.kt`.
- Adding another setup (e.g. a future 2025 release) means adding one more comma-separated position string — same shape as the existing four.

## Architecture

MVVM, no database — there's nothing to persist, so the Model layer is just in-memory UI state plus the BLE client (Repository role), instead of a Repository over Room/SQLite. That's a normal MVVM shape, not a compromise.

- `shared/` — Kotlin Multiplatform module: protocol/mapping logic, domain model, `BoardViewModel`, and `MoonBoardBleClient` (expect/actual: Android `BluetoothGatt`, iOS `CoreBluetooth`).
- `androidApp/` — Jetpack Compose UI, native Android.
- `iosApp/` — SwiftUI source files, native iOS.

Business logic (protocol, mapping, ViewModel) is shared; each platform keeps its own native UI framework (Compose / SwiftUI) rather than sharing UI too — simpler and each platform's BLE stack is verified independently.

## Status

- **Android**: builds and passes tests. Verified with `gradle :shared:testDebugUnitTest` and `gradle :androidApp:assembleDebug` — APK output at `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. Not yet run on a device (no phone attached here), so the BLE flow itself (scan/connect/light) is unverified end-to-end.
- **iOS**: unverified — this machine has no Xcode/Mac. `iosApp/` only has Swift source, not an Xcode project. On a Mac:
  1. Create a new Xcode iOS App project named `iosApp` (SwiftUI), and replace its generated Swift files with the ones here.
  2. Add a "Run Script" build phase before "Compile Sources" that runs `shared`'s Kotlin/Native framework export, e.g. `cd "$SRCROOT/.." && ./gradlew :shared:embedAndSignAppleFrameworkForXcode`, with the usual `SDK_NAME`/`CONFIGURATION`/`ARCHS` env vars Xcode sets.
  3. Add the produced `shared.framework` to the app's framework search path.
  - The CoreBluetooth delegate code in `MoonBoardBleClient.ios.kt` is written against the documented Kotlin/Native CoreBluetooth API but never compiled — expect small interop naming fixes on first build.

## Self-check

- `ProtocolTest.kt` verifies the LED index math and reproduces the community's documented example payload (`l#S5,P9,E18#`) exactly.
- `BoardSetupTest.kt` verifies each setup's occupied-hold count matches the source data and that every position stays inside the 11x18 grid.

## Local toolchain (this machine)

No Android Studio here, so Gradle and the Android SDK were installed standalone rather than via the IDE:
- Gradle 8.9: `C:\Users\almog-bb\dev-tools\gradle-8.9`
- Android SDK: `C:\Users\almog-bb\AppData\Local\Android\Sdk` (the default Android Studio location, so a future Android Studio install will just reuse it) — referenced by `local.properties` (gitignored, machine-specific).

To rebuild from a fresh shell:
```
export JAVA_HOME="/c/Program Files/Java/jdk-21.0.12.1"
./gradlew :androidApp:assembleDebug
```
`gradlew`/`gradlew.bat` are committed, pinned to Gradle 8.9 — Android Studio will use this exact version on sync instead of guessing, which avoids Gradle/AGP version-mismatch errors like `Unable to load class 'org.gradle.api.internal.plugins.DefaultArtifactPublicationSet'`.
