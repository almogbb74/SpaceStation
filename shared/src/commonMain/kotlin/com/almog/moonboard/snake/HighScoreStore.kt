package com.almog.moonboard.snake

import com.almog.moonboard.ble.PlatformContext

/** Persists Snake's best score across app launches (Android SharedPreferences, iOS NSUserDefaults). */
expect class HighScoreStore(context: PlatformContext) {
    fun load(): Int
    fun save(score: Int)
}
