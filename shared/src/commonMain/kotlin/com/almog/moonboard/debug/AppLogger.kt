package com.almog.moonboard.debug

import com.almog.moonboard.ble.PlatformContext

/**
 * Persists a debug log so it survives being tested at a gym with no PC attached - the phone
 * itself has to be the source of truth, retrieved later via the Settings screen's share button.
 */
expect object AppLogger {
    fun init(context: PlatformContext)
    fun log(tag: String, message: String)
}
