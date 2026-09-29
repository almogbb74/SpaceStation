package com.almog.moonboard.snake

import android.content.Context
import com.almog.moonboard.ble.PlatformContext

private const val PREFS_NAME = "moonboard_snake"
private const val KEY_HIGH_SCORE = "high_score"

actual class HighScoreStore actual constructor(context: PlatformContext) {
    private val prefs = context.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun load(): Int = prefs.getInt(KEY_HIGH_SCORE, 0)

    actual fun save(score: Int) {
        prefs.edit().putInt(KEY_HIGH_SCORE, score).apply()
    }
}
