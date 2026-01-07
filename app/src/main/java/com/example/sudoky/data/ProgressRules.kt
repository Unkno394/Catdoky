package com.example.sudoky.data

import com.example.sudoky.sudoku.Difficulty
fun requiredLevelForDifficulty(diff: Difficulty): Int {
    return when (diff) {
        Difficulty.NOVICE_9 -> 1
        Difficulty.STANDARD_9 -> 3
        Difficulty.HARD_9 -> 6
        Difficulty.STANDARD_16 -> 10
    }
}

fun pawsForTimeWinOnly(diff: Difficulty, elapsedSeconds: Int, isWin: Boolean): Int {
    if (!isWin) return 0

    val (t3, t2) = when (diff) {
        Difficulty.NOVICE_9 -> 360 to 600
        Difficulty.STANDARD_9 -> 480 to 840
        Difficulty.HARD_9 -> 720 to 1200
        Difficulty.STANDARD_16 -> 1800 to 2700
    }

    return when {
        elapsedSeconds <= t3 -> 3
        elapsedSeconds <= t2 -> 2
        else -> 1
    }
}

fun xpForResult(
    diff: Difficulty,
    elapsedSeconds: Int,
    isWin: Boolean,
    hintsUsed: Int
): Int {
    val base = if (isWin) 100 else 20
    val paws = pawsForTimeWinOnly(diff, elapsedSeconds, isWin)
    val pawsXp = when (paws) {
        3 -> 60
        2 -> 40
        1 -> 20
        else -> 0
    }
    val noHintsXp = if (hintsUsed == 0) 30 else 0
    return base + pawsXp + noHintsXp
}
