package com.example.sudoky.data

import kotlin.math.max

data class LevelProgress(
    val level: Int,
    val progress: Float,
    val xpIntoLevel: Int,
    val xpForNextLevel: Int
)

private fun xpNeededForLevel(level: Int): Int {
    return 60 + (level - 1) * 20
}

fun levelProgress(totalXp: Int): LevelProgress {
    var lvl = 1
    var remaining = max(0, totalXp)

    while (true) {
        val need = xpNeededForLevel(lvl)
        if (remaining < need) {
            val prog = if (need == 0) 0f else remaining.toFloat() / need.toFloat()
            return LevelProgress(
                level = lvl,
                progress = prog.coerceIn(0f, 1f),
                xpIntoLevel = remaining,
                xpForNextLevel = need
            )
        }
        remaining -= need
        lvl++
        if (lvl >= 99) {
            return LevelProgress(
                level = 99,
                progress = 1f,
                xpIntoLevel = 0,
                xpForNextLevel = 0
            )
        }
    }
}
