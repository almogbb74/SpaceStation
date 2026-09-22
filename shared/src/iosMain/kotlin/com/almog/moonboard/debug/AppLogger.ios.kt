package com.almog.moonboard.debug

import com.almog.moonboard.ble.PlatformContext
import platform.Foundation.NSLog

actual object AppLogger {
    actual fun init(context: PlatformContext) {
        // Not wired to a persisted file yet - iOS testing is deferred until there's a Mac to build with.
    }

    actual fun log(tag: String, message: String) {
        NSLog("[%s] %s", tag, message)
    }
}
