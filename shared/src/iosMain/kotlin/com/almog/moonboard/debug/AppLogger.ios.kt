package com.almog.moonboard.debug

import com.almog.moonboard.ble.PlatformContext
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDate
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSLog
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.appendingPathComponent
import platform.Foundation.dataUsingEncoding

actual object AppLogger {
    private var logPath: String? = null
    private val timeFormat = NSDateFormatter().apply { dateFormat = "HH:mm:ss.SSS" }

    actual fun init(context: PlatformContext) {
        val docsDir = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String
        val path = (docsDir as NSString).appendingPathComponent("moonboard.log")
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) {
            NSFileManager.defaultManager.createFileAtPath(path, contents = null, attributes = null)
        }
        logPath = path
    }

    actual fun log(tag: String, message: String) {
        NSLog("[%s] %s", tag, message)
        val path = logPath ?: return
        try {
            val line = "${timeFormat.stringFromDate(NSDate())} [$tag] $message\n"
            val handle = NSFileHandle.fileHandleForWritingAtPath(path) ?: return
            handle.seekToEndOfFile()
            handle.writeData((line as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!)
            handle.closeFile()
        } catch (_: Exception) {
            // Logging must never crash the app it's meant to debug.
        }
    }

    /** iOS-only: used by the Settings screen to build a UIActivityViewController. Not part of the expect API. */
    fun currentLogFilePath(): String? = logPath
}
