package com.almog.moonboard.debug

import com.almog.moonboard.ble.PlatformContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual object AppLogger {
    private var logFile: File? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    actual fun init(context: PlatformContext) {
        val dir = File(context.context.filesDir, "logs").apply { mkdirs() }
        logFile = File(dir, "moonboard.log")
    }

    actual fun log(tag: String, message: String) {
        android.util.Log.d("MoonBoard-$tag", message)
        val file = logFile ?: return
        try {
            file.appendText("${timeFormat.format(Date())} [$tag] $message\n")
        } catch (_: Exception) {
            // Logging must never crash the app it's meant to debug.
        }
    }

    /** Android-only: used by the Settings screen to build a share Intent. Not part of the expect API. */
    fun currentLogFile(): File? = logFile
}
