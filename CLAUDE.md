# Project rules

Non-obvious constraints for this codebase. Read before touching UI, the ViewModel, or the BLE layer.

## No hardcoded strings (Android)

Every user-facing string in `androidApp` goes in `strings.xml` and is pulled in via `stringResource(R.string.foo)` (or `context.getString(...)` outside Compose). No string literals in `Text(...)`, `contentDescription`, dialog titles, `Intent.createChooser`, etc.

- Exception: `BoardSetup.displayName` and other strings living in `shared/commonMain` stay as plain Kotlin strings — `strings.xml` is Android-only, and that code is shared with iOS too.
- A list built outside `@Composable` scope (like `drawerDestinations`) must store a `@StringRes Int`, not a resolved string — resolve it with `stringResource()` at render time.

## MVVM boundaries

- `shared/commonMain` (ViewModel, protocol, models) is the only place with app logic. `androidApp`/`iosApp` are render-only: they read `BoardUiState` and call ViewModel functions, never touch `MoonBoardBleClient` directly, never contain protocol/math logic.
- Platform BLE code (`BluetoothGatt`, `CoreBluetooth`) stays inside the `expect/actual MoonBoardBleClient` in `androidMain`/`iosMain`. It never leaks into commonMain, Compose, or SwiftUI.
- New behavior gets a new `BoardViewModel` function exposing `StateFlow` state — a screen never reaches past the ViewModel into the BLE client or model layer directly.

## BLE connection/scan state has one owner

`MoonBoardBleClient`'s `connectionState` and `scanResults` `StateFlow`s are the single source of truth, mutated only by GATT/scan callbacks. `BoardViewModel` only relays them into `BoardUiState` — it never keeps its own shadow copy of "are we connected" or second-guesses the client's state.

Any new BLE write path (a future `sendFrame()` for game rendering, etc.) must null-check the live `gatt`/characteristic before writing, the same way `sendProblem()` already does — `Connected` in the StateFlow doesn't guarantee the platform reference is still valid by the time a write actually happens (it can drop mid-frame).

## BLE writes stay sequential and paced

Chunks within one write are sent one at a time with `CHUNK_DELAY_MS` (30ms) between them. No future frame-rate optimization (e.g. Snake) should fire a new write, or a new game tick, before the previous one's chunks have all completed. Concurrent/overlapping writes risk corrupting the ASCII frame on the board's serial parser, not just reordering it — that pacing came from reverse-engineered community firmware, so don't shorten it without a real-hardware test confirming the board keeps up.

## Protocol changes require a real-hardware test, not just a unit test

`LedMapper`, `PayloadBuilder`, `MoonBoardProtocol` math changes aren't "done" on green unit tests alone — a unit test's expected values can encode the same wrong assumption as the code under test. (This happened: `ProtocolTest` self-confirmed a since-fixed off-by-one instead of catching it.) Any change to hold-to-LED mapping needs to be checked against a real board before being trusted.

## Comments explain the current code, not its history

A comment describes what the code does and why it's built that way now — never what an earlier version did, why that earlier version was wrong, or what bug used to exist there. No "previously this was X", "this used to cause Y", "fixed an off-by-one", "confirmed against real hardware" framing. If the current behavior needs justification, state the reason as a fact about the system (the protocol, the hardware, a constraint), not as a story about what changed. Git history is where "what changed and why" belongs, not comments.
