package com.example.sudoky.sudoku

data class MoveResult(
    val accepted: Boolean,
    val violatesRules: Boolean,
    val wrongAgainstSolution: Boolean
)

class SudokuEngine(
    private val strictCheck: Boolean
) {
    fun trySet(game: SudokuGame, r: Int, c: Int, v: Int): MoveResult {
        val size = game.size
        val idx = r * size + c

        if (game.fixed[idx]) {
            return MoveResult(accepted = false, violatesRules = false, wrongAgainstSolution = false)
        }

        if (v == 0) {
            game.user[idx] = 0
            return MoveResult(accepted = true, violatesRules = false, wrongAgainstSolution = false)
        }

        val prev = game.user[idx]
        game.user[idx] = 0
        val okByRules = SudokuRules.canPlace(game.user, size, game.boxSize, r, c, v)
        game.user[idx] = prev

        if (!okByRules) {
            return MoveResult(accepted = false, violatesRules = true, wrongAgainstSolution = false)
        }

        val wrong = strictCheck && (game.solution[idx] != v)
        if (wrong) {
            return MoveResult(accepted = false, violatesRules = false, wrongAgainstSolution = true)
        }

        game.user[idx] = v
        return MoveResult(accepted = true, violatesRules = false, wrongAgainstSolution = false)
    }
}
