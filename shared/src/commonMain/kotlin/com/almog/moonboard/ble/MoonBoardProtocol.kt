package com.almog.moonboard.ble

/**
 * Reverse-engineered MoonBoard v1 LED-box protocol (no official spec exists).
 * Source: community BLE probes (willslawrence/moonboard, e-sr/moonboard) that sniffed
 * the official app talking to the box over a plain Nordic UART Service - no auth/handshake.
 */
object MoonBoardProtocol {
    const val SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
    const val WRITE_CHARACTERISTIC_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
    const val NOTIFY_CHARACTERISTIC_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
    const val DEVICE_NAME_PREFIX = "Moonboard"

    // ponytail: default BLE ATT MTU (23 bytes - 3 byte header) with no MTU negotiation.
    // Raise this only after adding an explicit MTU request/negotiation on both platforms.
    private const val CHUNK_SIZE = 20
    const val CHUNK_DELAY_MS = 30L

    fun chunk(payload: String): List<ByteArray> =
        payload.encodeToByteArray().toList().chunked(CHUNK_SIZE).map { it.toByteArray() }
}
