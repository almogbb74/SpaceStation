package com.almog.moonboard.snake

import com.almog.moonboard.ble.PlatformContext
import platform.Foundation.NSUserDefaults

private const val KEY_HIGH_SCORE = "snake_high_score"

actual class HighScoreStore actual constructor(context: PlatformContext) {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun load(): Int = defaults.integerForKey(KEY_HIGH_SCORE).toInt()

    actual fun save(score: Int) {
        defaults.setInteger(score.toLong(), forKey = KEY_HIGH_SCORE)
    }
}
